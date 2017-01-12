package gov.nih.nci.coppa.web;

import gov.nih.nci.security.SecurityServiceProvider;
import gov.nih.nci.security.exceptions.CSException;
import gov.nih.nci.security.system.ApplicationSessionFactory;

import java.util.HashMap;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

/**
 * @author dkrylov
 * 
 */
public final class CSMInitializingListener implements ServletContextListener {
    private static final Logger LOG = Logger
            .getLogger(CSMInitializingListener.class);
    private final Properties properties = new Properties();

    /**
     * {@inheritDoc}
     */
    public void contextInitialized(ServletContextEvent context) {
        loadProperties();

        HashMap<String, String> connectionProperties = new HashMap<String, String>();
        connectionProperties.put("hibernate.connection.url",
                properties.getProperty("csm.db.connection.url"));
        connectionProperties.put("hibernate.connection.username",
                properties.getProperty("csm.db.user"));
        connectionProperties.put("hibernate.connection.password",
                properties.getProperty("csm.db.password"));
        connectionProperties.put("hibernate.dialect",
                properties.getProperty("csm.db.hibernate.dialect"));
        connectionProperties.put("hibernate.connection.driver_class",
                properties.getProperty("csm.db.driver"));
        try {
            final String appName = context.getServletContext()
                    .getInitParameter("csmApplicationName");
            context.getServletContext().setAttribute(
                    "csmSessionFactory",
                    ApplicationSessionFactory.getSessionFactory(appName,
                            connectionProperties));
            SecurityServiceProvider.getUserProvisioningManager(appName,
                    connectionProperties);

        } catch (CSException e) {
            LOG.error(e, e);
            throw new RuntimeException(e); // NOPMD
        }

    }

    /**
     * @throws RuntimeException
     */
    private void loadProperties() {
        try {
            try {
                properties.load(getClass().getResourceAsStream(
                        "/WEB-INF/classes/csm.properties"));
            } catch (RuntimeException e) {
                LOG.warn("Unable to load /WEB-INF/classes/csm.properties; now trying /csm.properties...");
                properties.load(Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream("/csm.properties"));
            }
        } catch (Exception e) {
            throw new RuntimeException("ERROR LOADING CSM PROPERTIES FILE!", e); // NOPMD
        }
    }

    /**
     * {@inheritDoc}
     */
    public void contextDestroyed(ServletContextEvent arg) {
        // NOOP
    }

}
