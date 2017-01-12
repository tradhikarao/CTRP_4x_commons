/**
 * The software subject to this notice and license includes both human readable
 * source code form and machine readable, binary, object code form. The nci-commons
 * Software was developed in conjunction with the National Cancer Institute
 * (NCI) by NCI employees and 5AM Solutions, Inc. (5AM). To the extent
 * government employees are authors, any rights in such works shall be subject
 * to Title 17 of the United States Code, section 105.
 *
 * This nci-commons Software License (the License) is between NCI and You. You (or
 * Your) shall mean a person or an entity, and all other entities that control,
 * are controlled by, or are under common control with the entity. Control for
 * purposes of this definition means (i) the direct or indirect power to cause
 * the direction or management of such entity, whether by contract or otherwise,
 * or (ii) ownership of fifty percent (50%) or more of the outstanding shares,
 * or (iii) beneficial ownership of such entity.
 *
 * This License is granted provided that You agree to the conditions described
 * below. NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up,
 * no-charge, irrevocable, transferable and royalty-free right and license in
 * its rights in the nci-commons Software to (i) use, install, access, operate,
 * execute, copy, modify, translate, market, publicly display, publicly perform,
 * and prepare derivative works of the nci-commons Software; (ii) distribute and
 * have distributed to and by third parties the nci-commons Software and any
 * modifications and derivative works thereof; and (iii) sublicense the
 * foregoing rights set out in (i) and (ii) to third parties, including the
 * right to license such rights to further third parties. For sake of clarity,
 * and not by way of limitation, NCI shall have no right of accounting or right
 * of payment from You or Your sub-licensees for the rights granted under this
 * License. This License is granted at no charge to You.
 *
 * Your redistributions of the source code for the Software must retain the
 * above copyright notice, this list of conditions and the disclaimer and
 * limitation of liability of Article 6, below. Your redistributions in object
 * code form must reproduce the above copyright notice, this list of conditions
 * and the disclaimer of Article 6 in the documentation and/or other materials
 * provided with the distribution, if any.
 *
 * Your end-user documentation included with the redistribution, if any, must
 * include the following acknowledgment: This product includes software
 * developed by 5AM and the National Cancer Institute. If You do not include
 * such end-user documentation, You shall include this acknowledgment in the
 * Software itself, wherever such third-party acknowledgments normally appear.
 *
 * You may not use the names "The National Cancer Institute", "NCI", or "5AM"
 * to endorse or promote products derived from this Software. This License does
 * not authorize You to use any trademarks, service marks, trade names, logos or
 * product names of either NCI or 5AM, except as required to comply with the
 * terms of this License.
 *
 * For sake of clarity, and not by way of limitation, You may incorporate this
 * Software into Your proprietary programs and into any third party proprietary
 * programs. However, if You incorporate the Software into third party
 * proprietary programs, You agree that You are solely responsible for obtaining
 * any permission from such third parties required to incorporate the Software
 * into such third party proprietary programs and for informing Your
 * sub-licensees, including without limitation Your end-users, of their
 * obligation to secure any required permissions from such third parties before
 * incorporating the Software into such third party proprietary software
 * programs. In the event that You fail to obtain such permissions, You agree
 * to indemnify NCI for any claims against NCI by such third parties, except to
 * the extent prohibited by law, resulting from Your failure to obtain such
 * permissions.
 *
 * For sake of clarity, and not by way of limitation, You may add Your own
 * copyright statement to Your modifications and to the derivative works, and
 * You may provide additional or different license terms and conditions in Your
 * sublicenses of modifications of the Software, or any derivative works of the
 * Software as a whole, provided Your use, reproduction, and distribution of the
 * Work otherwise complies with the conditions stated in this License.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO
 * EVENT SHALL THE NATIONAL CANCER INSTITUTE, 5AM SOLUTIONS, INC. OR THEIR
 * AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.fiveamsolutions.nci.commons.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.ConnectionReleaseMode;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.context.ManagedSessionContext;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;

import com.fiveamsolutions.nci.commons.data.persistent.PersistentObject;
import com.fiveamsolutions.nci.commons.validator.MultipleCriteriaMessageInterpolator;

/**
 * Class to help initialize hibernate in the nci environment.
 * @author Scott Miller
 */
@SuppressWarnings("PMD.TooManyMethods")
public class HibernateHelper {
    /**
     * The maximum number of elements that can be in a single in clause. This is due to bug
     * http://opensource.atlassian.com/projects/hibernate/browse/HHH-2166
     */
    public static final int MAX_IN_CLAUSE_LENGTH = 500;
    private static final Map<Class<?>, ClassValidator<?>> CLASS_VALIDATOR_MAP =
        new HashMap<Class<?>, ClassValidator<?>>();
    private static final MultipleCriteriaMessageInterpolator INTERPOLATOR = new MultipleCriteriaMessageInterpolator();
    private static final String DEFAULT_BUNDLE = "ValidatorMessages";
    private static ResourceBundle bundle;

    private static final Logger LOG = Logger.getLogger(HibernateHelper.class);

    private Configuration configuration;
    private SessionFactory sessionFactory;
    private final NamingStrategy namingStrategy;
    private final Interceptor interceptor;

    /**
     * Default constructor.
     */
    public HibernateHelper() {
        this(null, null);
    }

    /**
     * Constructor to build the hibernate helper.
     *
     * @param namingStrategy the name strategy, if one is needed, null otherwise.
     * @param interceptor the interceptor, if one is needed, null otherwise.
     */
    public HibernateHelper(NamingStrategy namingStrategy, Interceptor interceptor) {
        this.namingStrategy = namingStrategy;
        this.interceptor = interceptor;
    }

    /**
     * This builds both the configuration and the session factory.
     */
    public synchronized void initialize() {
        if (configuration == null) {
            try {
                configuration = new AnnotationConfiguration();
                initializeConfig();

                // We call buildSessionFactory twice, because it appears that the annotated classes are
                // not 'activated' in the config until we build. The filters required the classes to
                // be present, so we throw away the first factory and use the second. If this is
                // removed, you'll likely see a NoClassDefFoundError in the unit tests
                SessionFactory sf = configuration.buildSessionFactory();
                sf.close();

                modifyConfig();

                buildSessionFactory();
            } catch (HibernateException e) {
                LOG.error(e.getMessage(), e);
                throw new ExceptionInInitializerError(e);
            }
        }
    }

    /**
     * This method is used to edit the configuration object and call configuration.configure.  This is called by the
     * initialize method after creating an instance of AnnotationConfiguration.  The last thing this method should do
     * is to call configuration.configure.
     */
    protected void initializeConfig() {
        if (namingStrategy != null) {
            configuration.setNamingStrategy(namingStrategy);
        }
        if (interceptor != null) {
            configuration.setInterceptor(interceptor);
        }

        configuration = configuration.configure();
    }

    /**
     * This method is used to modify the configuration after configuration.config has been called.  This is called by
     * the initialize method creates an instance of AnnotationConfiguration, calls initializeConfig and creates and
     * destroys a session factory in order to work around a bug in the configuration that causes some classes to not
     * be activated in the config properly.
     */
    protected void modifyConfig() {
        // do nothing
    }

    /**
     * This method just creates the session factory using the current config.
     */
    protected void buildSessionFactory() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
        sessionFactory = configuration.buildSessionFactory();
    }

    /**
     * @return the configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * @return the sessionFactory
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Get the session that is bound to the current context.
     * @return the current session
     */
    public Session getCurrentSession() {
        return getSessionFactory().getCurrentSession();
    }

    /**
     * Starts a transaction on the current Hibernate session. Intended for use in
     * unit tests - DAO / Service layer logic should rely on container-managed transactions
     *
     * @return a Hibernate session.
     */
    public Transaction beginTransaction() {
        return getSessionFactory().getCurrentSession().beginTransaction();
    }

    /**
     * Checks if the transaction is active and then rolls it back.
     *
     * @param tx the Transaction to roll back.
     */
    public void rollbackTransaction(Transaction tx) {
        if (tx != null && tx.isActive()) {
            tx.rollback();
        }
    }

    /**
     * Open a hibernate session and bind it as the current session via
     * {@link ManagedSessionContext#bind(org.hibernate.classic.Session)}. The hibernate property
     * "hibernate.current_session_context_class" must be set to "managed" for this to have effect. This method should be
     * called from within an Interceptor or Filter type class that is setting up the scope of the Session. This method
     * should then call {@link HibernateUtil#unbindAndCleanupSession()} when the scope of the Session is expired.
     *
     * @see ManagedSessionContext#bind(org.hibernate.classic.Session)
     */
    public void openAndBindSession() {
        openAndBindSession(getSessionFactory());
    }

    /**
     * Open a hibernate session and bind it as the current session via
     * {@link ManagedSessionContext#bind(org.hibernate.classic.Session)}. The hibernate property
     * "hibernate.current_session_context_class" must be set to "managed" for this to have effect This method should be
     * called from within an Interceptor or Filter type class that is setting up the scope of the Session. This method
     * should then call {@link HibernateUtil#unbindAndCleanupSession()} when the scope of the Session is expired.
     *
     * @see ManagedSessionContext#bind(org.hibernate.classic.Session)
     * @param sf the session factory.
     */
    public void openAndBindSession(SessionFactory sf) {
        SessionFactoryImplementor sessionFactoryImplementor = (SessionFactoryImplementor) sf;
        org.hibernate.classic.Session currentSession = sessionFactoryImplementor.openSession(null, true, false,
                ConnectionReleaseMode.AFTER_STATEMENT);
        currentSession.setFlushMode(FlushMode.COMMIT);
        ManagedSessionContext.bind(currentSession);
    }

    /**
     * Close the current session and unbind it via {@link ManagedSessionContext#unbind(SessionFactory)}. The hibernate
     * property "hibernate.current_session_context_class" must be set to "managed" for this to have effect. This method
     * should be called from within an Interceptor or Filter type class that is setting up the scope of the Session,
     * when this scope is about to expire.
     */
    public void unbindAndCleanupSession() {
        unbindAndCleanupSession(getSessionFactory());
    }

    /**
     * Close the current session and unbind it via {@link ManagedSessionContext#unbind(SessionFactory)}. The hibernate
     * property "hibernate.current_session_context_class" must be set to "managed" for this to have effect. This method
     * should be called from within an Interceptor or Filter type class that is setting up the scope of the Session,
     * when this scope is about to expire.
     *
     * @param sf the session factory.
     */
    public void unbindAndCleanupSession(SessionFactory sf) {
        org.hibernate.classic.Session currentSession = ManagedSessionContext.unbind(sf);
        if (currentSession != null) {
            currentSession.close();
        }
    }

    /**
     * Determines if we are currently using managed sessions.
     * @return true if we are, false otherwise.
     */
    public boolean isManagedSession() {
        return "managed".equals(getConfiguration().getProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS));
    }

    /**
     * Break up a list of items into separate in clauses, to avoid limits imposed by databases or by Hibernate bug
     * http://opensource.atlassian.com/projects/hibernate/browse/HHH-2166.
     * @param items list of items to include in the in clause
     * @param columnName name of column to match against the list
     * @param blocks empty Map of HQL param name to param list of values to be set in the HQL query - it will be
     *               populated by the method
     * @return full HQL "in" clause, of the form: " columnName in (:block1) or ... or columnName in (:blockN)"
     */
    public static String buildInClause(List<? extends Serializable> items, String columnName,
            Map<String, List<? extends Serializable>> blocks) {
        StringBuffer inClause = new StringBuffer();
        for (int i = 0; i < items.size(); i += MAX_IN_CLAUSE_LENGTH) {
            List<? extends Serializable> block = items.subList(i, Math.min(items.size(), i + MAX_IN_CLAUSE_LENGTH));
            String paramName = "block" + i / MAX_IN_CLAUSE_LENGTH;
            if (inClause.length() > 0) {
                inClause.append(" or");
            }
            inClause.append(" " + columnName + " in (:" + paramName + ")");
            blocks.put(paramName, block);
        }
        return inClause.toString();
    }

    /**
     * Bind the parameters returned by {@link #buildInClause(List, String, Map)} to a hibernate Query.
     * @param query hibernate query to bind to
     * @param blocks blocks to be bound to query
     */
    public static void bindInClauseParameters(Query query, Map<String, List<? extends Serializable>> blocks) {
        for (Map.Entry<String, List<? extends Serializable>> block : blocks.entrySet()) {
            query.setParameterList(block.getKey(), block.getValue());
        }
    }

    /**
     * Get the class validator for a given object, caching validators to speed up subsequent lookups.
     * @param <T> type of object to retrieve the ClassValidator for
     * @param o object to retrieve the ClassValidator for
     * @return the ClassValidator for the given object
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static synchronized <T> ClassValidator<T> getClassValidator(T o) {
        ClassValidator<T> classValidator = (ClassValidator<T>) CLASS_VALIDATOR_MAP.get(o.getClass());
        if (classValidator == null) {
            classValidator = new ClassValidator(o.getClass(), bundle, INTERPOLATOR, null, null);
            CLASS_VALIDATOR_MAP.put(o.getClass(), classValidator);
        }
        return classValidator;
    }

    /**
     * @param aResourceBundle the bundle to set
     */
    public void setBundle(ResourceBundle aResourceBundle) {
        if (aResourceBundle == null) {
            bundle = ResourceBundle.getBundle(DEFAULT_BUNDLE);
        } else {
            bundle = aResourceBundle;
        }
        INTERPOLATOR.setBundle(bundle);
    }

    /**
     * Set name of the resource bundle to be used by validator.
     * @param bundleName the name of the bundle.  If null or empty, the default bundle is used.
     */
    public void setBundleName(String bundleName) {
        bundle = ResourceBundle.getBundle(StringUtils.defaultIfEmpty(bundleName, DEFAULT_BUNDLE));
        INTERPOLATOR.setBundle(bundle);
    }

    /**
     * @param entity the entity to validate
     * @return a map of validation messages keyed by the property path. The keys represent the field/property validation
     *         errors however, when key is null it means the validation is a type/class validation error
     */
    public static Map<String, String[]> validate(PersistentObject entity) {
        Map<String, List<String>> messageMap = new HashMap<String, List<String>>();
        ClassValidator<PersistentObject> classValidator = getClassValidator(entity);
        InvalidValue[] validationMessages = classValidator.getInvalidValues(entity);
        for (InvalidValue validationMessage : validationMessages) {
            String path = StringUtils.defaultString(validationMessage.getPropertyPath());
            List<String> m = messageMap.get(path);
            if (m == null) {
                m = new ArrayList<String>();
                messageMap.put(path, m);
            }
            String msg = validationMessage.getMessage();
            msg = msg.replace("(fieldName)", "").trim();
            if (!m.contains(msg)) {
                m.add(msg);
            }
        }

        return convertMapListToMapArray(messageMap);
    }

    /**
     * Convert list to array for map of string, string list.
     * @param messageMap map of string, string list to convert.
     * @return map string, string list.
     */
    public static Map<String, String[]> convertMapListToMapArray(Map<String, List<String>> messageMap) {
        Map<String, String[]> returnMap = new HashMap<String, String[]>();
        for (Map.Entry<String, List<String>> entry : messageMap.entrySet()) {
            returnMap.put(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]));
        }
        return returnMap;
    }

}
