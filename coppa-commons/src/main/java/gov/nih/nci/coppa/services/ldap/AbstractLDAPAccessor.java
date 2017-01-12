/**
 * 
 */
package gov.nih.nci.coppa.services.ldap;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

/**
 * Base class for sub-classes needing to access LDAP.
 * 
 * @author dkrylov
 * 
 */
// CHECKSTYLE:OFF
abstract class AbstractLDAPAccessor {
    private static final String LDAP_MIGRATION_EXCLUDES = "ldap.migration.excludes";

    private static final String LDAP_MIGRATION_GROUPER_TO_ID_STRING_MAP = "ldap.migration.grouperToIdStringMap";

    private static final String LDAP_SECURITY_PROTOCOL = "ldap.security.protocol";

    private static final String LDAP_SECURITY_AUTHENTICATION = "ldap.security.authentication";

    private static final String LDAP_SEARCH_ROOT = "ldap.context";

    private static final String LDAP_PASSWORD = "ldap.password";

    private static final String LDAP_USER = "ldap.user";

    private static final String LDAP_TIMEOUTS_CONNECT = "ldap.timeouts.connect";

    private static final String LDAP_TIMEOUTS_READ = "ldap.timeouts.read";

    private static final String LDAP_URL = "ldap.url";

    private static final String LDAP_UID_ATTRNAME = "ldap.uid.attrname";

    private final Properties ldapProperties = new Properties();

    private static final Logger LOG = Logger
            .getLogger(AbstractLDAPAccessor.class);

    /**
     * @throws IOException
     *             IOExceptions
     * 
     */
    AbstractLDAPAccessor() throws IOException {
        ldapProperties.load(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("/ldap.properties"));

    }

    private Hashtable<String, String> prepareLDAPEnvironmentProperties() { // NOPMD
        final Hashtable<String, String> environment = new Hashtable<String, String>(); // NOPMD
        environment.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");
        environment.put(Context.PROVIDER_URL,
                ldapProperties.getProperty(LDAP_URL));
        environment.put("com.sun.jndi.ldap.read.timeout",
                ldapProperties.getProperty(LDAP_TIMEOUTS_READ));
        environment.put("com.sun.jndi.ldap.connect.timeout",
                ldapProperties.getProperty(LDAP_TIMEOUTS_CONNECT));
        environment.put(Context.SECURITY_AUTHENTICATION,
                ldapProperties.getProperty(LDAP_SECURITY_AUTHENTICATION));
        environment.put(Context.SECURITY_PROTOCOL,
                ldapProperties.getProperty(LDAP_SECURITY_PROTOCOL));
        environment.put(Context.SECURITY_PRINCIPAL,
                ldapProperties.getProperty(LDAP_USER));
        environment.put(Context.SECURITY_CREDENTIALS,
                loadLdapPasswordFromFile());
        environment.put(Context.REFERRAL, "follow");
        return environment;
    }

    /**
     * @return isLdapPasswordProvided
     */
    protected boolean isLdapPasswordProvided() {
        return StringUtils.isNotBlank(loadLdapPasswordFromFile());
    }

    /**
     * @return
     * @throws IOException
     */
    private String loadLdapPasswordFromFile() {
        File file = new File(
                (ldapProperties.getProperty(LDAP_PASSWORD, new File(
                        SystemUtils.USER_HOME, ".ctrp_ldap_password")
                        .getAbsolutePath()))
                        .replace("~", SystemUtils.USER_HOME));
        try {
            return FileUtils.readFileToString(file);
        } catch (IOException e) {
            LOG.error(ExceptionUtils.getFullStackTrace(e));
            return null;
        }
    }

    protected SearchResult searchForUserInLDAP(String loginName)
            throws NamingException {
        Hashtable<String, String> environment = prepareLDAPEnvironmentProperties(); // NOPMD
        String searchFilter = "(&(objectclass=user)("
                + ldapProperties.getProperty(LDAP_UID_ATTRNAME) + "="
                + loginName + "))";

        DirContext dirContext = null;

        try {
            dirContext = new InitialDirContext(environment);

            SearchControls searchControls = new SearchControls();
            searchControls.setReturningAttributes(null);
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            final NamingEnumeration<SearchResult> searchEnum = dirContext
                    .search(ldapProperties.getProperty(LDAP_SEARCH_ROOT),
                            searchFilter, searchControls);

            if (searchEnum.hasMore()) {
                return searchEnum.next();
            } else {
                return null;
            }
        } finally {
            try {
                if (dirContext != null) {
                    dirContext.close();
                }
            } catch (Exception e) {
                LOG.error(ExceptionUtils.getFullStackTrace(e));
            }
        }

    }

    protected void checkUserPassword(final String fullDnName,
            final String password) throws NamingException {
        final Hashtable<String, String> environment = prepareLDAPEnvironmentProperties(); // NOPMD
        environment.put(Context.SECURITY_PRINCIPAL, fullDnName);
        environment.put(Context.SECURITY_CREDENTIALS, password);

        DirContext context = null;
        try {
            context = new InitialDirContext(environment);
            Attributes attributes = context.getAttributes(fullDnName);
            Attribute uid = attributes.get(ldapProperties
                    .getProperty(LDAP_UID_ATTRNAME));
            LOG.info("LDAP user " + uid.get() + " passed authentication.");
        } finally {
            try {
                if (context != null) {
                    context.close();
                }
            } catch (Exception e) {
                LOG.error(ExceptionUtils.getFullStackTrace(e));
            }
        }

    }

    protected String getUid(SearchResult user) throws NamingException {
        return (String) user.getAttributes()
                .get(ldapProperties.getProperty(LDAP_UID_ATTRNAME)).get();
    }

    protected String getGridGrouperToIdentityMapping() {
        return ldapProperties
                .getProperty(LDAP_MIGRATION_GROUPER_TO_ID_STRING_MAP);
    }

    protected String getMigrationExcludes() {
        return ldapProperties.getProperty(LDAP_MIGRATION_EXCLUDES);
    }

    protected Properties getCsmDatabaseProperties() {
        final Properties properties = new Properties();
        try {
            try {
                properties.load(getClass().getResourceAsStream(
                        "/WEB-INF/classes/csm.properties"));
            } catch (RuntimeException e) {
                properties.load(Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream("/csm.properties"));
            }
        } catch (Exception e) {
            throw new RuntimeException("ERROR LOADING CSM PROPERTIES FILE!", e); // NOPMD
        }
        return properties;
    }
}
