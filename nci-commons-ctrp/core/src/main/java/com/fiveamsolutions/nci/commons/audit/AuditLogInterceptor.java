/**
 * The software subject to this notice and license includes both human readable
 * source code form and machine readable, binary, object code form. The po-app
 * Software was developed in conjunction with the National Cancer Institute
 * (NCI) by NCI employees and 5AM Solutions, Inc. (5AM). To the extent
 * government employees are authors, any rights in such works shall be subject
 * to Title 17 of the United States Code, section 105.
 *
 * This po-app Software License (the License) is between NCI and You. You (or
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
 * its rights in the po-app Software to (i) use, install, access, operate,
 * execute, copy, modify, translate, market, publicly display, publicly perform,
 * and prepare derivative works of the po-app Software; (ii) distribute and
 * have distributed to and by third parties the po-app Software and any
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

package com.fiveamsolutions.nci.commons.audit;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.JoinTable;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;
import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.annotations.MapKeyManyToMany;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.collection.PersistentSet;
import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Table;
import org.hibernate.property.Getter;
import org.hibernate.type.CompositeCustomType;
import org.hibernate.type.Type;
import org.json.simple.JSONObject;

import com.fiveamsolutions.nci.commons.util.HibernateHelper;
import com.fiveamsolutions.nci.commons.util.ProxyUtils;
import com.fiveamsolutions.nci.commons.util.UsernameHolder;

/**
 * Interceptor that adds audit log records for audits.  The username that identifies
 * the actor of the change request is determined with {@link UsernameHolder}.
 * Don't forget to define a sequence {@value #SEQUENCE_NAME}, if your database supports it.
 * @see UsernameHolder
 */

@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ExcessiveClassLength", "PMD.TooManyMethods", "rawtypes" })
public class AuditLogInterceptor extends EmptyInterceptor {
   
    private HibernateHelper hibernateHelper;
    private DefaultProcessor processor = new DefaultProcessor();

    private static final String COLUMN_NAME = "columnName";
    private static final String TABLE_NAME = "tableName";
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(AuditLogInterceptor.class);

    /**
     * Name of the database sequence defined for audit transaction id.
     */
    public static final String SEQUENCE_NAME = "AUDIT_ID_SEQ";

    /**
     * For getting sequence numbers.
     */
    private Dialect dialect;
    private TxIdGenerator idGenerator;

    /**
     * Cache for getColumnTableName.
     */
    private static final Map<String, Map<String, String>> COLUMN_CACHE = new HashMap<String, Map<String, String>>();

    /**
     * Set to contain insert update log records.
     */
    private final transient ThreadLocal<Set<AuditLogHelper>> audits = new ThreadLocal<Set<AuditLogHelper>>();

    private final transient ThreadLocal<Map<RecordKey, AuditLogRecord>> records =
        new ThreadLocal<Map<RecordKey, AuditLogRecord>>();
    
    private final transient ThreadLocal<Boolean> disabledFlag = new ThreadLocal<Boolean>();

    /**
     *@param helper the helper used for persistence operations.
     */
    public AuditLogInterceptor(HibernateHelper helper) {
        hibernateHelper = helper;
        processor.setHibernateHelper(hibernateHelper);
    }

    /**
     * default ctor.
     */
    public AuditLogInterceptor() {
        // noop
    }

    /**
     * @return the helper used for persistence operations.
     */
    public HibernateHelper getHibernateHelper() {
        return hibernateHelper;
    }

    /**
     * @param hibernateHelper the helper to use for persistence operations.
     */
    public void setHibernateHelper(HibernateHelper hibernateHelper) {
        this.hibernateHelper = hibernateHelper;
        if (processor != null) {
            processor.setHibernateHelper(hibernateHelper);
        }
    }

    /**
     * @param processor replace the default selector.
     */
    public void setProcessor(DefaultProcessor processor) {
        this.processor = processor;
        this.processor.setHibernateHelper(hibernateHelper);
    }

    /**
     * @return the current processor.
     */
    public DefaultProcessor getProcessor() {
        return processor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public boolean onFlushDirty(Object obj, Serializable id, Object[] newValues, Object[] oldValues,
            String[] properties, Type[] types) {
        if (isEnabled() && processor.isAuditableEntity(obj)) {
            auditChangesIfNeeded(obj, id, newValues, oldValues, properties,
                                      types, AuditType.UPDATE);
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onSave(Object obj, Serializable id, Object[] newValues, String[] properties, Type[] types) {
        if (isEnabled() && processor.isAuditableEntity(obj)) {
            auditChangesIfNeeded(obj, id, newValues, new Object[properties.length],
                                      properties, types, AuditType.INSERT);
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDelete(Object obj, Serializable id, Object[] newValues, String[] properties, Type[] types) {
        if (isEnabled() && processor.isAuditableEntity(obj)) {
            // there are not new values since this object is being deleted.
            // a null is passed in for the old values so that we will be forced
            // to retrieve a fresh set of values from the session which will include
            // even those fields on the component items that were mapped with a updatable=false.
            auditChangesIfNeeded(obj, id, new Object[properties.length], null,
                                      properties, types, AuditType.DELETE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCollectionUpdate(Object collection, Serializable key) {
        if (!(collection instanceof PersistentCollection) || !isEnabled()) {
            return;
        }

        PersistentCollection pc = (PersistentCollection) collection;
        Object owner = pc.getOwner();
        if (!processor.isAuditableEntity(owner)) {
            return;
        }
        if (audits.get() == null) {
            audits.set(new HashSet<AuditLogHelper>());
        }

        String role = pc.getRole();

        Serializable oldSerial = pc.getStoredSnapshot();
        Object oldValue = null;
        if (oldSerial != null && pc instanceof PersistentSet) {
            // PersistentSet seems to build a strange map where the key is also the value.
            oldValue = ((Map) oldSerial).keySet();
        } else {
            oldValue = oldSerial;
        }
        Object newValue = pc.getValue();

        int idx = role.lastIndexOf('.');
        String className = role.substring(0, idx);
        String property = role.substring(idx + 1);

        if (collectionNeedsAuditing(owner, newValue, oldValue, property)) {
            Map<String, String> tabColMA = getColumnTableName(className, property);

                AuditLogRecord record = getOrCreateRecord(owner, key, className,
                        tabColMA.get(TABLE_NAME), AuditType.UPDATE);
            Getter getter;
            try {
                //Use current thread's class loader, instead of this class's class loader.  This
                //ensures accessibility to class BO even if nci-commons is created via a higher level class loader
                //than the application's class loader (web/ear class loader).
                getter = getIdGetter(Thread.currentThread().getContextClassLoader().loadClass(className));
            } catch (ClassNotFoundException ex) {
                throw new HibernateException(ex);
            }
            AuditLogHelper helper = new AuditLogHelper(record, owner, getter);
            audits.get().add(helper);
            helper.getDetails().add(new DetailHelper(property, tabColMA.get(COLUMN_NAME), oldValue, newValue));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("deprecation")
    public void postFlush(Iterator arg0) {

        if (audits.get() != null && !audits.get().isEmpty()) {
            SessionFactory sf = getHibernateHelper().getSessionFactory();
            Session session = sf.openSession(getHibernateHelper().getCurrentSession().connection());
            Long transactionId = null;
            try {
                transactionId = getTxIdGenerator().generateId(getDialect(), session);

                // Resolve the new entity values on the audit log record itself, then save
                Iterator<AuditLogHelper> it = audits.get().iterator();
                while (it.hasNext()) {
                    AuditLogHelper audit = it.next();
                    it.remove();
                    audit.getAuditLogRecord().setTransactionId(transactionId);
                    Serializable id = (Serializable) audit.getIdGetter().get(audit.getEntity());
                    audit.getAuditLogRecord().setEntityId((Long) id);

                    for (DetailHelper detail : audit.getDetails()) {
                        processor.processDetail(audit.getAuditLogRecord(), audit.getEntity(), id, detail.getProperty(),
                                detail.getColumnName(), detail.getOldVal(), detail.getNewVal());
                    }
                    session.save(audit.getAuditLogRecord());
                }
                session.flush();
            } catch (HibernateException e) {
                LOG.error(e);
                throw new CallbackException(e);
            } finally {
                audits.get().clear();
                records.get().clear();
                session.close();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterTransactionCompletion(Transaction arg0) {
        clearCurrentAuditLog();
    }

    /**
     * 
     */
    private void clearCurrentAuditLog() {
        if (audits.get() != null) {
            audits.get().clear();
        }

        if (records.get() != null) {
            records.get().clear();
        }
    }

    /** a composite key for entity audit records. */
    private static final class RecordKey {
        private final Object entity;
        private final AuditType type;

        RecordKey(Object entity, AuditType type) {
            this.entity = entity;
            this.type = type;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            RecordKey r = (RecordKey) obj;
            return entity == r.entity && type == r.type;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(entity).appendSuper(type.hashCode()).toHashCode();
        }

    }

    /**
     * @param id
     */
    private AuditLogRecord getOrCreateRecord(Object entity, Serializable id, String entityName, String tableName,
            AuditType eventToLog) {
        Map<RecordKey, AuditLogRecord> map = records.get();
        if (map == null) {
            map = new HashMap<RecordKey, AuditLogRecord>();
            records.set(map);
        }
        RecordKey key = new RecordKey(entity, eventToLog);
        AuditLogRecord record = map.get(key);
        if (record == null) {
            String user = UsernameHolder.getUser();
            record = processor.newAuditLogRecord(entity, id, eventToLog, entityName, tableName, user);
            map.put(key, record);
        }
        return record;
    }

    @SuppressWarnings("PMD.ExcessiveParameterList")
    private void auditChangesIfNeeded(Object auditableObj, Serializable id, Object[] newValues, Object[] oldValues,
            String[] properties, Type[] types, AuditType eventToLog) {

        // first make sure we have old values around. The passed in set will be null when an object is being updated
        // that was detached from the session
        Object[] nonNullOldValues = getOldValues(oldValues, properties, auditableObj, id);

        if (audits.get() == null) {
            audits.set(new HashSet<AuditLogHelper>());
        }

        AuditLogRecord record = null;
        AuditLogHelper helper = null;

        for (int i = 0; i < properties.length; i++) {
            String property = properties[i];
            Object oldValue = nonNullOldValues[i];
            Object newValue = newValues[i];
            if (needsAuditing(auditableObj, types[i], newValue, oldValue, property)) {
                Class clazz = getPersistentClass(auditableObj);
                Map<String, String> tabColMA = getColumnTableName(clazz.getName(), property);
                if (record == null) {
                    String entityName = getPersistentClass(auditableObj).getName();
                    record = getOrCreateRecord(auditableObj, id, entityName, tabColMA.get(TABLE_NAME), eventToLog);
                    helper = new AuditLogHelper(record, auditableObj, getIdGetter(clazz));
                    audits.get().add(helper);
                }

                String colName = tabColMA.get(COLUMN_NAME);
                if (types[i] instanceof CompositeCustomType) {
                    oldValue = getCompositeValue((CompositeCustomType) types[i], oldValue);
                    newValue = getCompositeValue((CompositeCustomType) types[i], newValue);
                }
                helper.getDetails().add(new DetailHelper(property, colName, oldValue, newValue));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Object getCompositeValue(CompositeCustomType type, Object input) {
        if (input == null) {
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        for (int i = 0; i < type.getPropertyNames().length; i++) {
            jsonObject.put(type.getPropertyNames()[i], type.getPropertyValue(input, i));
        }
        return jsonObject.toString();
    }

    private Getter getIdGetter(Class clazz) {
        return getHibernateHelper().getConfiguration().getClassMapping(clazz.getName()).getIdentifierProperty()
                .getGetter(clazz);
    }

    @SuppressWarnings("deprecation")
    private Object[] getOldValues(Object[] oldValues, String[] properties, Object auditableObj, Serializable id) {
        Object[] nonNullOldValues = oldValues;
        Session session = null;
        if (nonNullOldValues == null) {
            try {
                SessionFactory sf = getHibernateHelper().getSessionFactory();
                session = sf.openSession(getHibernateHelper().getCurrentSession().connection());
                nonNullOldValues = retrieveOldValues(session, id, properties, getPersistentClass(auditableObj));
            } finally {
                session.close();
            }
        }
        return nonNullOldValues;
    }

    private Dialect getDialect() {
        if (dialect != null) {
            return dialect;
        }
        Dialect d = null;
        try {
            // Use current thread's class loader, instead of this class's class loader. This
            // ensures accessibility to class BO even if nci-commons is created via a higher level class loader
            // than the application's class loader (web/ear class loader).
            d = (Dialect) Thread.currentThread().getContextClassLoader()
                    .loadClass(getHibernateHelper().getConfiguration().getProperty(Environment.DIALECT)).newInstance();
        } catch (Exception e) {
            LOG.error("Unable to determine dialect.", e);
        }
        dialect = d;
        return d;
    }

    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    private TxIdGenerator getTxIdGenerator() {
        if (idGenerator != null) {
            return idGenerator;
        }
        TxIdGenerator g = new SequenceIdGenerator();
        if (!g.isSupported(getDialect())) {
            g = new GuidGenerator();
            if (!g.isSupported(getDialect())) {
                throw new Error("Cannot generate transaction ids for this database (" + getDialect().getClass() + ")");
            }
        }
        idGenerator = g;
        return g;
    }

    private boolean needsAuditing(Object auditableObj, Type type, Object newValue, Object oldValue, String property) {
        if (type.isCollectionType()) {
            return collectionNeedsAuditing(auditableObj, newValue, oldValue, property);
        }
        if (type.isEntityType()) {
            if (newValue != null && !processor.isAuditableEntity(newValue)) {
                return false;
            }
            if (oldValue != null && !processor.isAuditableEntity(oldValue)) {
                return false;
            }
        }

        return !ObjectUtils.equals(newValue, oldValue);
    }

    private boolean collectionNeedsAuditing(Object auditableObj, Object newValue, Object oldValue, String property) {
        // PO-6030: this method has undesirable side-effect that may impact
        // performance: it initializes lazy
        // collections on the auditableObj entity even if the collections don't
        // need to be audited.
        // Initialization happens during the CollectionUtils.isEqualCollection
        // check below. We should skip the check at least
        // when oldValue and newValue point to the same uninitialized collection
        // instance, because in such case it is obvious that the collection hasn't been touched.       
        try {
            if (!sameNonInitializedCollection(newValue, oldValue)) {
                String cn = ProxyUtils.unEnhanceCGLIBClassName(auditableObj.getClass());
                Method getter = getHibernateHelper().getConfiguration().getClassMapping(cn).getProperty(property)
                        .getGetter(auditableObj.getClass()).getMethod();
                if (getter.getAnnotation(MapKey.class) != null
                        || getter.getAnnotation(MapKeyManyToMany.class) != null) {
                    // this is some sort of map
                    Map<?, ?> oldMap = (Map<?, ?>) oldValue;
                    Map<?, ?> newMap = (Map<?, ?>) newValue;
                    oldMap = oldMap == null ? Collections.emptyMap() : oldMap;
                    newMap = newMap == null ? Collections.emptyMap() : newMap;
                    return !equalsMap(oldMap, newMap);
                } else if (getter.getAnnotation(JoinTable.class) != null
                        || getter.getAnnotation(OneToMany.class) != null) {
                    Collection<?> oldSet = (Collection<?>) oldValue;
                    Collection<?> newSet = (Collection<?>) newValue;
                    return !CollectionUtils.isEqualCollection(oldSet == null ? Collections.emptySet() : oldSet,
                                                              newSet == null ? Collections.emptySet() : newSet);
                }
            }
        } catch (SecurityException e) {
            LOG.error(e.getMessage(), e);
        }

        return false;
    }

    private boolean sameNonInitializedCollection(Object newValue,
            Object oldValue) {
        return (newValue == oldValue // NOPMD
                && newValue instanceof PersistentCollection && !((PersistentCollection) newValue)
                    .wasInitialized());
    }

    /**
     * Retrieve the old values.
     * @param session the session
     * @param id the id of the object
     * @param properties the array of property names that should be retrieved and placed in the resulting array.
     * @param theClass the class of the object to retrieve the data from.
     * @return the array of values for the properties.
     */
    protected Object[] retrieveOldValues(Session session, Serializable id, String[] properties,
                                              Class<?> theClass) {
        Object[] nonNullOldValues = new Object[properties.length];

        Object oldObject = session.get(theClass, id);
        if (oldObject != null) {
            for (int i = 0; i < properties.length; i++) {
                try {
                    nonNullOldValues[i] = PropertyUtils.getProperty(oldObject, properties[i]);
                    getLazyLoadedObjects(session, nonNullOldValues[i]);
                } catch (Exception e) {
                    LOG.error("Unable to read the old value of a property while logging.", e);
                    nonNullOldValues[i] = null;
                }
            }
        }

        return nonNullOldValues;
    }

    /**
     * in case this is a collection which is lazy loaded by default
     * we want to make sure that we retrieve the collection items
     * before the session is closed.
     * @param session the session to load the object
     * @param lazyLoadedObject the lazily loaded object
     */
    protected void getLazyLoadedObjects(Session session, Object lazyLoadedObject) {
        if (lazyLoadedObject instanceof Collection) {
            for (Object item : (Collection<?>) lazyLoadedObject) {
                if (item instanceof PersistentClass) {
                    session.refresh(item);
                    break;
                }
            }
        }
    }

    /**
     * Retrieves the table name and the column name for the given class and property.
     *
     * @param className
     * @param fieldName
     * @return Map
     */
    private synchronized Map<String, String> getColumnTableName(String className, String fieldName) {
        String hashkey = className + ";" + fieldName;
        Map<String, String> retMap = COLUMN_CACHE.get(hashkey);
        if (retMap != null) {
            return retMap;
        }

        retMap = new HashMap<String, String>();
        COLUMN_CACHE.put(hashkey, retMap);

        PersistentClass pc = getHibernateHelper().getConfiguration().getClassMapping(className);
        // get the table and column information
        Table table = pc.getTable();
        String tableName = table.getName();
        String columnName = getColumnName(pc, fieldName);
        if (columnName == null) {
            columnName = fieldName;
        }
        retMap.put(TABLE_NAME, tableName);
        retMap.put(COLUMN_NAME, columnName);
        return retMap;
    }

    /**
     * Retrieves the column name for the given PersistentClass and fieldName.
     *
     * @param pc
     * @param fieldName
     * @return columnName
     */
    private static String getColumnName(PersistentClass pc, String fieldName) {
        if (pc == null) {
            return null;
        }

        String columnName = null;
        Property property = pc.getProperty(fieldName);
        for (Iterator<?> it3 = property.getColumnIterator(); it3.hasNext();) {
            Object o = it3.next();
            if (!(o instanceof Column)) {
                LOG.debug("Skipping non-column (probably a formula");
                continue;
            }
            Column column = (Column) o;
            columnName = column.getName();
            break;
        }
        if (columnName == null) {
            try {
                columnName = getColumnName(pc.getSuperclass(), fieldName);
            } catch (MappingException e) {
                // in this case the annotation / mapping info was at the current class and not the base class
                // but for some reason the column name could not be determined.
                // This will happen when a subclass of an entity uses a joined subclass.
                // in this case just set column Name to null and let the caller default to the property name.
                columnName = null;
            }
        }

        return columnName;
    }

    /**
     * . Returns the Actual class name from the CGILIB Proxy classname of the entity
     *
     * @param obj
     * @return
     */
    private Class<?> getPersistentClass(Object obj) {
        Class<?> clazz = obj.getClass();
        Configuration cfg = getHibernateHelper().getConfiguration();
        while (cfg.getClassMapping(clazz.getName()) == null) {
            if (clazz.getSuperclass() == null) {
                return null;
            }
            clazz = clazz.getSuperclass();
        }
        return clazz;
    }

    /**
     * does not care about order.
     */
    private static boolean equalsMap(Map<?, ?> a, Map<?, ?> b) {
        if (ObjectUtils.equals(a, b)) {
            return true;
        }
        if (a == null || b == null || a.size() != b.size()) {
            return false;
        }
        for (Map.Entry<?, ?> e : a.entrySet()) {
            // some maps may allow null as values
            if (!b.containsKey(e.getKey())) {
                return false;
            }
            Object vb = b.get(e.getKey());
            if (!ObjectUtils.equals(e.getValue(), vb)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Class to hold the audit log pair.
     */
    private static class AuditLogHelper {
        private final AuditLogRecord auditLogRecord;
        private final Object entity;
        private final List<DetailHelper> details = new ArrayList<DetailHelper>();
        private final Getter idGetter;

        /**
         * @return the auditLogRecord
         */
        public AuditLogRecord getAuditLogRecord() {
            return auditLogRecord;
        }

        /**
         * @return the entity
         */
        public Object getEntity() {
            return entity;
        }

        /**
         * @return detail helpers associated to this record.
         */
        public List<DetailHelper> getDetails() {
            return details;
        }

        /**
         * @return the getter for Entity's id.
         */
        public Getter getIdGetter() {
            return idGetter;
        }

        /**
         * @param record audit record
         * @param entity entity
         */
        public AuditLogHelper(AuditLogRecord record, Object entity, Getter idGetter) {
            auditLogRecord = record;
            this.entity = entity;
            this.idGetter = idGetter;
        }
    }

    /**
     * Class to hold information about detail records that we can't immediately resolve the 'new' value of.
     */
    private static class DetailHelper {
        private final Object newVal;
        private final Object oldVal;
        private final String property;
        private final String columnName;

        /**
         * @param property affected property
         * @param oldVal value of the property befor change.
         * @param newVal (potentially non-persisted [yet]) list of new values
         */
        public DetailHelper(String property, String columnName, Object oldVal, Object newVal) {
            this.property = property;
            this.columnName = columnName;
            this.newVal = newVal;
            this.oldVal = oldVal;
        }

        /**
         * @return the newVal
         */
        public Object getNewVal() {
            return newVal;
        }

        /**
         * @return oldVal
         */
        public Object getOldVal() {
            return oldVal;
        }

        /**
         * @return property name.
         */
        public String getProperty() {
            return property;
        }

        /**
         * @return column name.
         */
        public String getColumnName() {
            return columnName;
        }
    }

    /**
     * Simple interface to generate id's and select a scheme.
     */
    interface TxIdGenerator {
        Long generateId(Dialect dialect, Session session);

        boolean isSupported(Dialect dialect);
    }

    /**
     * for postgres, HSQLDB, ...
     */
    private static class SequenceIdGenerator implements TxIdGenerator {
        @Override
        public Long generateId(Dialect dialect, Session session) {
            String sql = dialect.getSequenceNextValString(SEQUENCE_NAME);
            Number uniqueResult = (Number) session.createSQLQuery(sql).uniqueResult();
            if (uniqueResult == null) {
                // For some reason, HSQL isn't returning a result here, even though the query doesn't
                // throw an exception. This works correctly in postgres.
                LOG.warn("should only happen in unit tests!!");
                uniqueResult = 1L;
            }
            return uniqueResult.longValue();
        }

        @Override
        public boolean isSupported(Dialect dialect) {
            try {
                dialect.getSequenceNextValString(SEQUENCE_NAME);
                return true;
            } catch (MappingException e) {
                return false;
            }
        }
    }

    private static final int HEX = 16;
    private static final int BITLENGHT_LONG = 64;

    /**
     * For MySql and such.
     */

    private static class GuidGenerator implements TxIdGenerator {

        @Override
        public Long generateId(Dialect dialect, Session session) {
            String sql = dialect.getSelectGUIDString();
            String uniqueResult = (String) session.createSQLQuery(sql).uniqueResult();
            // some examples of uuid's gened by mysql
            // 45f28058-3039-11de-aec3-0013a95a7546
            // 2474e532-3035-11de-aec3-0013a95a7546
            uniqueResult = uniqueResult.replaceAll("-", "");
            BigInteger uuid = new BigInteger(uniqueResult, HEX);

            long lo = uuid.longValue();
            long hi = uuid.shiftRight(BITLENGHT_LONG).longValue();
            return hi ^ lo;
        }

        @Override
        public boolean isSupported(Dialect dialect) {
            try {
                dialect.getSelectGUIDString();
                return true;
            } catch (MappingException e) {
                return false;
            }
        }
    }
    
    /**
     * Enables the auditing for the current thread, if previously disabled. Auditing is always enabled by default.
     */
    public synchronized void enable() {
        if (!isEnabled()) {
            clearCurrentAuditLog();
            disabledFlag.remove();
        }
    }
    
    /**
     * Disables the auditing for the current thread, if previously enabled. Auditing is always enabled
     * by default.
     */
    public synchronized void disable() {
        clearCurrentAuditLog();
        disabledFlag.set(Boolean.TRUE);
    }

    /**
     * Whether the auditing is enabled for the current thread, which is always
     * the case by default.
     * 
     * @return the enabled
     */
    public boolean isEnabled() {
        return disabledFlag.get() == null
                || Boolean.FALSE.equals(disabledFlag.get());
    }

}
