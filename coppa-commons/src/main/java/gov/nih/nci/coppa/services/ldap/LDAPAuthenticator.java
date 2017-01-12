/**
 * 
 */
package gov.nih.nci.coppa.services.ldap;

import java.io.IOException;
import java.security.Principal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

import org.apache.catalina.realm.GenericPrincipal;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

/**
 * @author dkrylov
 * 
 */
// CHECKSTYLE:OFF
public final class LDAPAuthenticator extends AbstractLDAPAccessor {

    public static final String EMAIL = "mail";

    public static final String LAST_NAME = "sn";

    public static final String FIRST_NAME = "givenName";

    private static final String CTRP_CI = "ctrp.env.ci";

    private static final Logger LOG = Logger.getLogger(LDAPAuthenticator.class);

    private static final Set<String> USERS_CREATED = new HashSet<String>();

    public LDAPAuthenticator() throws IOException {
        super();
    }

    /**
     * @param username
     *            username
     * @param password
     *            password
     * @return Principal Principal
     */
    public Principal authenticateAndCreateCsmUser(String username,
            String password) {
        try {
            if (Boolean.valueOf(System.getProperty(CTRP_CI))
                    && "pass".equals(password)) {
                LOG.warn(CTRP_CI
                        + " runtime property is set to true: we are running in a CI environment. "
                        + "Skipping LDAP going directly to CSM.");
                createCsmUser(username);
                return new GenericPrincipal(null, username, password);
            } else {
                final SearchResult user = searchForUserInLDAP(username);
                final String fullDnName = user != null ? user
                        .getNameInNamespace() : null;
                if (StringUtils.isBlank(fullDnName)) {
                    LOG.warn("Unable to find a user in LDAP: " + username);
                    return null;
                }
                checkUserPassword(fullDnName, password);

                // At this point we know the user exists in LDAP and the
                // password is
                // correct.
                // We need to create a local copy in csm_user table, if not
                // there
                // yet.
                final String uid = StringUtils.lowerCase(getUid(user));
                createCsmUser(uid);
                return new GenericPrincipal(null, uid, password);
            }
        } catch (Exception e) {
            LOG.error("Unable to authenticate " + username, e);
            return null;
        }
    }

    /**
     * @param username
     *            username
     * @return Map<String, String>
     */
    public Map<String, String> getUserAttributes(String username) {
        final Map<String, String> attrs = new HashMap<String, String>();
        try {
            final SearchResult user = searchForUserInLDAP(username);
            final String fullDnName = user != null ? user.getNameInNamespace()
                    : null;
            if (!StringUtils.isBlank(fullDnName)) {
                Attributes attributes = user.getAttributes();
                attrs.put(FIRST_NAME, getAttrValue(attributes, FIRST_NAME));
                attrs.put(LAST_NAME, getAttrValue(attributes, LAST_NAME));
                attrs.put(EMAIL, getAttrValue(attributes, EMAIL));
            }
        } catch (Exception e) {
            LOG.error("Unable to authenticate " + username, e);
        }
        return attrs;
    }

    private String getAttrValue(Attributes attributes, String attrName)
            throws NamingException {
        Attribute attr = attributes.get(attrName);
        return attr != null ? ((String) attr.get()) : "";
    }

    private void createCsmUser(final String username) {
        if (USERS_CREATED.contains(username)) {
            return;
        }
        Connection c = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Properties properties = getCsmDatabaseProperties();
            Class.forName(properties.getProperty("csm.db.driver").trim());
            c = DriverManager.getConnection(
                    properties.getProperty("csm.db.connection.url").trim(),
                    properties.getProperty("csm.db.user").trim(), properties
                            .getProperty("csm.db.password").trim());
            stmt = c.prepareStatement("select user_id from csm_user where login_name=?");
            stmt.setString(1, username);
            stmt.setQueryTimeout(30);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                DbUtils.closeQuietly(rs);
                DbUtils.closeQuietly(stmt);

                stmt = c.prepareStatement("insert into csm_user (login_name, first_name, last_name, update_date, migrated_flag) "
                        + "values (?,'','', now(),0)");
                stmt.setString(1, username);
                stmt.setQueryTimeout(30);
                stmt.executeUpdate();
            }

            USERS_CREATED.add(username);
        } catch (Exception e) {
            LOG.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(c);
        }

    }

}
