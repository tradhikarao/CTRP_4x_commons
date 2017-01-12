/**
 * 
 */
package gov.nih.nci.coppa.services.ldap;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Realm;
import org.apache.catalina.Session;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.authenticator.SavedRequest;
import org.apache.catalina.connector.Request;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.buf.CharChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.jboss.as.web.security.ExtendedFormAuthenticator;

/**
 * An <b>Authenticator</b> and <b>Valve</b> implementation of LDAP-based
 * Authentication. Based on former CSM CGMM implementation.
 * 
 * @author Denis G. Krylov
 * 
 */
public class LDAPFormAuthenticatorValve extends ExtendedFormAuthenticator { // NOPMD

    private static final Logger LOG = Logger
            .getLogger(LDAPFormAuthenticatorValve.class);

    private static final String CTRP_CI = "ctrp.env.ci";

    /**
     * Authenticate the user making this request, based on the specified login
     * configuration. Return <code>true</code> if any specified constraint has
     * been satisfied, or <code>false</code> if we have created a response
     * challenge already.
     * 
     * @param request
     *            Request we are processing
     * @param response
     *            Response we are creating
     * @param config
     *            Login configuration describing how authentication should be
     *            performed
     * 
     * @exception IOException
     *                if an input/output error occurs
     * @return boolean
     */
    // CHECKSTYLE:OFF
    public boolean authenticate(Request request, HttpServletResponse response, // NOPMD
            LoginConfig config) throws IOException {

        // Have we already authenticated someone?
        Principal principal = request.getUserPrincipal();
        String ssoId = (String) request.getNote(Constants.REQ_SSOID_NOTE);
        if (principal != null) {
            // Associate the session with any existing SSO session
            if (ssoId != null) // NOPMD
                associate(ssoId, request.getSessionInternal(true));
            return (true);
        }

        // Is there an SSO session against which we can try to reauthenticate?
        if (ssoId != null) {
            // Try to reauthenticate using data cached by SSO. If this fails,
            // either the original SSO logon was of DIGEST or SSL (which
            // we can't reauthenticate ourselves because there is no
            // cached username and password), or the realm denied
            // the user's reauthentication for some reason.
            // In either case we have to prompt the user for a logon */
            if (reauthenticateFromSSO(ssoId, request)) // NOPMD
                return true;
        }

        Session session = request.getSessionInternal(true);

        // Have we authenticated this user before but have caching disabled?
        if (!cache) {
            String username = (String) session
                    .getNote(Constants.SESS_USERNAME_NOTE);
            String password = (String) session
                    .getNote(Constants.SESS_PASSWORD_NOTE);
            if ((username != null) && (password != null)) {
                principal = context.getRealm().authenticate(username, password);
                if (principal != null) { // NOPMD
                    session.setNote(Constants.FORM_PRINCIPAL_NOTE, principal);
                    if (!matchRequest(request)) {
                        register(request, response, principal,
                                HttpServletRequest.FORM_AUTH, username,
                                password);
                        return (true);
                    }
                }
            }
        }

        // Is this the re-submit of the original request URI after successful
        // authentication? If so, forward the *original* request instead.
        if (matchRequest(request)) {
            session = request.getSessionInternal(true);
            principal = (Principal) session
                    .getNote(Constants.FORM_PRINCIPAL_NOTE);
            register(request, response, principal,
                    HttpServletRequest.FORM_AUTH,
                    (String) session.getNote(Constants.SESS_USERNAME_NOTE),
                    (String) session.getNote(Constants.SESS_PASSWORD_NOTE));
            // If we're caching principals we no longer need the username
            // and password in the session, so remove them
            if (cache) {
                session.removeNote(Constants.SESS_USERNAME_NOTE);
                session.removeNote(Constants.SESS_PASSWORD_NOTE);
            }
            if (restoreRequest(request, session)) {
                return (true);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return (false);
            }
        }

        // Acquire references to objects we will need to evaluate
        MessageBytes uriMB = MessageBytes.newInstance();
        CharChunk uriCC = uriMB.getCharChunk();
        uriCC.setLimit(-1);
        String contextPath = request.getContextPath();
        String requestURI = request.getDecodedRequestURI();

        // Is this the action request from the login page?
        boolean loginAction = requestURI.startsWith(contextPath)
                && requestURI.endsWith(Constants.FORM_ACTION);

        // No -- Save this request and redirect to the form login page
        if (!loginAction) {
            session = request.getSessionInternal(true);
            try {
                saveRequest(request, session);
            } catch (IOException ioe) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        sm.getString("authenticator.requestBodyTooBig"));
                return (false);
            }
            forwardToLoginPage(request, response, config);
            return (false);
        }

        // Yes -- Acknowledge the request, validate the specified credentials
        // and redirect to the error page if they are not correct
        request.getResponse().sendAcknowledgement();
        Realm realm = context.getRealm();
        if (characterEncoding != null) {
            request.setCharacterEncoding(characterEncoding);
        }
        String username = request.getParameter(Constants.FORM_USERNAME);
        String password = request.getParameter(Constants.FORM_PASSWORD);

        // Check if there is sso id as well as sessionkey
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            forwardToLoginPage(request, response, config);
            return (false);
        }

        principal = performLDAPAuthentication(username, password);

        if (principal == null) {
            forwardToErrorPage(request, response, config);
            return false;
        }

        username = principal.getName();
        principal = realm.authenticate(username, password); // this will go
                                                            // through JAAS
                                                            // LoginModules
        if (principal == null) {
            forwardToErrorPage(request, response, config);
            return false;
        }

        if (session == null) // NOPMD
            session = request.getSessionInternal(false);
        if (session == null) {
            if (landingPage == null) {
                response.sendError(HttpServletResponse.SC_REQUEST_TIMEOUT,
                        sm.getString("authenticator.sessionExpired"));
            } else {
                // Make the authenticator think the user originally requested
                // the landing page
                String uri = request.getContextPath() + landingPage;
                SavedRequest saved = new SavedRequest();
                saved.setRequestURI(uri);
                request.getSessionInternal(true).setNote(
                        Constants.FORM_REQUEST_NOTE, saved);
                response.sendRedirect(response.encodeRedirectURL(uri));
            }
            return (false);
        }

        // Save the authenticated Principal in our session
        session.setNote(Constants.FORM_PRINCIPAL_NOTE, principal);

        // Save the username and password as well
        session.setNote(Constants.SESS_USERNAME_NOTE, username);
        session.setNote(Constants.SESS_PASSWORD_NOTE, password);

        // Redirect the user to the original request URI (which will cause
        // the original request to be restored)
        requestURI = savedRequestURL(session);
        if (requestURI == null)
            if (landingPage == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        sm.getString("authenticator.formlogin"));
            } else {
                // Make the authenticator think the user originally requested
                // the landing page
                String uri = request.getContextPath() + landingPage;
                SavedRequest saved = new SavedRequest();
                saved.setRequestURI(uri);
                session.setNote(Constants.FORM_REQUEST_NOTE, saved);
                response.sendRedirect(response.encodeRedirectURL(uri));
            }
        else {
            response.sendRedirect(response.encodeRedirectURL(requestURI));
        }
        return (false);
    }

    private Principal performLDAPAuthentication(String username, String password)
            throws IOException {
        if (Boolean.valueOf(System.getProperty(CTRP_CI))
                && "pass".equals(password)) {
            LOG.warn(CTRP_CI
                    + " runtime property is set to true: we are running in a CI environment. "
                    + "Skipping LDAP authentication and going directly to CSM.");
            return new GenericPrincipal(context.getRealm(), username, password);
        }
        return new LDAPAuthenticator().authenticateAndCreateCsmUser(username,
                password);

    }

}
