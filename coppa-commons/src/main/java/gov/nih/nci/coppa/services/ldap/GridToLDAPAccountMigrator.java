/**
 * 
 */
package gov.nih.nci.coppa.services.ldap;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import javax.naming.NamingException;
import javax.naming.directory.SearchResult;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Migrates grid accounts in CSM tables to corresponding LDAP accounts.
 * 
 * @author dkrylov
 * 
 */
// CHECKSTYLE:OFF
public final class GridToLDAPAccountMigrator extends AbstractLDAPAccessor
        implements ServletContextListener {

    private static final Logger LOG = Logger
            .getLogger(GridToLDAPAccountMigrator.class);
    static {
        LOG.setLevel(Level.INFO);
    }

    /**
     * @throws IOException
     *             IOException
     */
    public GridToLDAPAccountMigrator() throws IOException {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.servlet.ServletContextListener#contextInitialized(javax.servlet
     * .ServletContextEvent)
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (!isLdapPasswordProvided()) {
            LOG.warn("No LDAP password specified. Skipping migration altogether!");
            return;
        }
        LOG.info("GridToLDAPAccountMigrator started.");
        Connection c = null;
        try {
            c = openConnection();
            c.setAutoCommit(false);
            String gridGrouperURL = getGridGrouperURL(c);
            LOG.info("Grid Grouper URL: " + gridGrouperURL);
            if (StringUtils.isNotBlank(gridGrouperURL)) {
                String identityStringPrefix = determineIdentityStringPrefix(gridGrouperURL);
                LOG.info("Identity Prefix: " + identityStringPrefix);
                migrateUsersWithPrefix(c, identityStringPrefix);
            }
            c.commit();
        } catch (Exception e) {
            LOG.error(ExceptionUtils.getFullStackTrace(e));
            DbUtils.rollbackAndCloseQuietly(c);
            throw new RuntimeException(e); // NOPMD
        } finally {
            DbUtils.closeQuietly(c);
            LOG.info("GridToLDAPAccountMigrator finished.");
        }
    }

    private void migrateUsersWithPrefix(Connection c,
            String identityStringPrefix) throws SQLException, NamingException {
        QueryRunner runner = new QueryRunner();
        final List<Object[]> results = runner
                .query(c,
                        "select user_id, login_name from csm_user where login_name like '"
                                + StringEscapeUtils
                                        .escapeSql(identityStringPrefix)
                                + "%' order by update_date asc",
                        new ArrayListHandler());
        for (Object[] row : results) {
            Number userID = (Number) row[0];
            String loginName = (String) row[1];
            if (!excludedFromMigration(loginName)) {
                migrateUser(c, userID, loginName, identityStringPrefix);
            } else {
                LOG.info("Login name is on exclude list; skipping " + loginName);
            }
        }
    }

    private boolean excludedFromMigration(String loginName) {
        String[] excludes = StringUtils.defaultString(getMigrationExcludes())
                .split(";");
        return ArrayUtils.contains(excludes, loginName);
    }

    private void migrateUser(Connection c, Number userID, String loginName,
            String identityStringPrefix) throws NamingException, SQLException {
        LOG.info("Migrating user: " + loginName);
        String uid = loginName.replace(identityStringPrefix, "");
        String ldapUserID = findLDAPUserId(uid);
        if (StringUtils.isNotBlank(ldapUserID)) {
            LOG.info("LDAP ID: " + ldapUserID);
            renameAnyPotentialMatchWithLdapID(c, ldapUserID);
            new QueryRunner().update(c,
                    "update csm_user set premgrt_login_name='"
                            + StringEscapeUtils.escapeSql(loginName) + "',"
                            + " update_date=now(), login_name='"
                            + StringEscapeUtils.escapeSql(ldapUserID)
                            + "' where user_id=" + userID);
            LOG.info("Summary: migrated user " + userID + " from " + loginName
                    + " to " + ldapUserID);
        } else {
            LOG.error("UNABLE to migrate " + loginName
                    + ": unable to find user account in LDAP!");
        }
    }

    /**
     * @param c
     * @param ldapUserID
     * @throws SQLException
     */
    private void renameAnyPotentialMatchWithLdapID(Connection c,
            String ldapUserID) throws SQLException {
        if (!new QueryRunner().query(
                c,
                "select user_id from csm_user where login_name='"
                        + StringEscapeUtils.escapeSql(ldapUserID) + "'",
                new ArrayListHandler()).isEmpty()) {
            final long currentTimeMillis = System.currentTimeMillis();
            LOG.info("CSM User with this LDAP ID already exists. Changing its login name to "
                    + (ldapUserID + "_" + currentTimeMillis));
            new QueryRunner()
                    .update(c,
                            "update csm_user set update_date=now(), login_name='"
                                    + StringEscapeUtils.escapeSql((ldapUserID
                                            + "_" + currentTimeMillis))
                                    + "' where login_name='"
                                    + StringEscapeUtils.escapeSql(ldapUserID)
                                    + "'");
        }
    }

    private String findLDAPUserId(String uid) throws NamingException {
        SearchResult user = searchForUserInLDAP(uid);
        return user != null ? StringUtils.lowerCase(getUid(user)) : null;
    }

    private String determineIdentityStringPrefix(final String gridGrouperURL) {
        String map = getGridGrouperToIdentityMapping();
        LOG.info("Using this mapping to determine identity string that corresponds to the grouper: "
                + map);
        for (String entry : map.split(";")) {
            String grouperHint = entry.substring(0, entry.indexOf('='));
            if (gridGrouperURL.contains(grouperHint)) {
                LOG.info("Matched on " + grouperHint);
                return entry.replaceFirst("^.*?=", "");
            }
        }
        throw new RuntimeException( // NOPMD
                "We are unable to determine which grid identity string to key on for this grouper configuration "
                        + "and thus unable to perform the migration.");
    }

    private String getGridGrouperURL(Connection c) throws SQLException {
        try {
            final Object[] result = new QueryRunner().query(c,
                    "select grid_grouper_url from csm_remote_group LIMIT 1",
                    new ArrayHandler());
            return result != null && result.length > 0 ? (String) result[0]
                    : null;
        } catch (SQLException e) {
            LOG.error(e, e);
            return null;
        }
    }

    /**
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private Connection openConnection() throws ClassNotFoundException,
            SQLException {
        Connection c;
        Properties properties = getCsmDatabaseProperties();
        Class.forName(properties.getProperty("csm.db.driver").trim());
        c = DriverManager.getConnection(
                properties.getProperty("csm.db.connection.url").trim(),
                properties.getProperty("csm.db.user").trim(), properties
                        .getProperty("csm.db.password").trim());
        return c;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.
     * ServletContextEvent)
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // NOOP
    }

}
