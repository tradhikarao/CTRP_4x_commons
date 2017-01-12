package gov.nih.nci.coppa.webservices.security;

import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.authenticator.BasicAuthenticator;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.connector.Request;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.util.buf.CharChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.jboss.logging.Logger;

import gov.nih.nci.coppa.services.ldap.LDAPAuthenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

/**
 * This authenticator will take credential provided via BASIC authentication
 * headers and authenticate against Dorian.
 * 
 * Credentials are parsed using {@link UsernamePasswordProvider} instances. This
 * class will iterate through the hardwired set of providers (currently there
 * are two - one for typical Basic auth via HTTP headers, and one to mimic
 * WS-Security UsernameToken auth).
 * 
 * @see UsernamePasswordProvider
 * @see BasicUsernamePasswordProvider
 * @see WsSecurityUsernamePasswordProvider
 * 
 * @author Jason Aliyetti <jason.aliyetti@semanticbits.com>
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class UsernameTokenAuthenticator extends AuthenticatorBase {

    private static final Logger LOG = Logger
            .getLogger(UsernameTokenAuthenticator.class);

    private static final String CTRP_CI = "ctrp.env.ci";

    private List<UsernamePasswordProvider> usernamePasswordProviderList;

    /**
     * {@inheritDoc}
     * 
     * This implementation will iterate through the registered credential
     * providers. The first provider that returns a non-null value is utilized.
     */
    @Override
    protected boolean authenticate(Request request, // NOPMD
            HttpServletResponse response, LoginConfig loginConfig)
            throws IOException {

        if (usernamePasswordProviderList == null) {
            usernamePasswordProviderList = new ArrayList<UsernamePasswordProvider>();
            usernamePasswordProviderList
                    .add(new BasicUsernamePasswordProvider());
            usernamePasswordProviderList
                    .add(new WsSecurityUsernamePasswordProvider());
        }

        // already authenticated via session or sso?
        if (isAlreadyAuthenticated(request) || isSSOAuthenticated(request)) {
            return true;
        }

        // do grid auth if needed
        BasicAuthInfo basicAuthInfo = null;
        for (UsernamePasswordProvider usernamePasswordProvider : usernamePasswordProviderList) {
            try {
                basicAuthInfo = usernamePasswordProvider
                        .getBasicAuthInfo(request);
            } catch (Exception e) {
                LOG.error(e, e);
            }
            if (basicAuthInfo != null) {
                break;
            }
        }

        // at this point, the username and password are available if possible
        if (!isValid(basicAuthInfo)) {
            sendChallenge(request, response, loginConfig);
            return false;
        }

        Principal principal = doAuthentication(basicAuthInfo);

        if (principal != null) {
            register(request, response, principal,
                    HttpServletRequest.BASIC_AUTH, basicAuthInfo.getUsername(),
                    basicAuthInfo.getPassword());

            return true;
        }

        // send challenge if needed
        sendChallenge(request, response, loginConfig);
        return false;
    }

    /**
     * Validates whether authentication credentials have been provided.
     * 
     * @param basicAuthInfo
     * @return True if credentials are present, false otherwise.
     */
    private boolean isValid(BasicAuthInfo basicAuthInfo) {
        return basicAuthInfo != null
                && StringUtils.isNotBlank(basicAuthInfo.getUsername())
                && StringUtils.isNotBlank(basicAuthInfo.getPassword());
    }

    /**
     * Sends a Basic authentication challenge.
     * 
     * @param request
     * @param response
     * @param loginConfig
     * @throws IOException
     */
    private void sendChallenge(Request request, HttpServletResponse response,
            LoginConfig loginConfig) throws IOException {
        MessageBytes authenticate = request
                .getResponse()
                .getCoyoteResponse()
                .getMimeHeaders()
                .addValue(BasicAuthenticator.AUTHENTICATE_BYTES, 0,
                        BasicAuthenticator.AUTHENTICATE_BYTES.length);

        CharChunk authenticateCC = authenticate.getCharChunk();
        authenticateCC.append("Basic realm=\"");
        if (loginConfig.getRealmName() == null) {
            authenticateCC.append("Realm");
        } else {
            authenticateCC.append(loginConfig.getRealmName());
        }
        authenticateCC.append('\"');
        authenticate.toChars();
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);

    }

    /**
     * Attempts to reauthenticate via SSO mechanism.
     * 
     * @param request
     * @return True if SSO authentication is successful. False otherwise.
     */
    private boolean isSSOAuthenticated(Request request) {
        boolean result = false;

        String ssoId = (String) request.getNote(Constants.REQ_SSOID_NOTE);
        if (ssoId != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("SSO Id " + ssoId + " set; attempting "
                        + "reauthentication");
            }

            /*
             * Try to reauthenticate using data cached by SSO. If this fails,
             * either the original SSO logon was of DIGEST or SSL (which we
             * can't reauthenticate ourselves because there is no cached
             * username and password), or the realm denied the user's
             * reauthentication for some reason. In either case we have to
             * prompt the user for a logon
             */
            if (reauthenticateFromSSO(ssoId, request)) {
                result = true;
            }
        }

        return result;
    }

    /**
     * Attempts to associate previously authenticated identity via SSO headers.
     * 
     * @param request
     * @return True if the request is using an already authenticated identity.
     */
    private boolean isAlreadyAuthenticated(Request request) {
        boolean result = false;

        Principal principal = request.getUserPrincipal();
        String ssoId = (String) request.getNote(Constants.REQ_SSOID_NOTE);
        if (principal != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Already authenticated '" + principal.getName() + "'");
            }

            // Associate the session with any existing SSO session
            if (ssoId != null) {
                associate(ssoId, request.getSessionInternal(true));
            }

            result = true;
        }

        return result;
    }

    /**
     * Authenticate against Dorian using {@link DorianService}.
     * 
     * The JAAS login chain is invoked after Dorian, and the resulting principal
     * is returned.
     * 
     * @param basicAuthInfo
     *            The credentials to use.
     * @return The principal resulting from authenticating against the realm.
     * @throws IOException
     */
    private Principal doAuthentication(BasicAuthInfo basicAuthInfo)
            throws IOException {
        Principal principal = null;

        final String password = basicAuthInfo.getPassword();
        final String username = basicAuthInfo.getUsername();

        if (Boolean.valueOf(System.getProperty(CTRP_CI))
                && "pass".equals(password)) {
            LOG.warn(CTRP_CI
                    + " runtime property is set to true: we are running in a CI environment. "
                    + "Skipping LDAP authentication and going directly to CSM.");
            principal = new GenericPrincipal(context.getRealm(), username,
                    password);
        } else {
            // do LDAP auth
            principal = new LDAPAuthenticator().authenticateAndCreateCsmUser(
                    username, password);
        }
        if (principal != null) {
            principal = context.getRealm().authenticate(principal.getName(),
                    password);
        }

        return principal;
    }

}
