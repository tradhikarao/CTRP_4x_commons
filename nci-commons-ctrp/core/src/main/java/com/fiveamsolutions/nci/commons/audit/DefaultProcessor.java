/**
 * The software subject to this notice and license includes both human readable
 * source code form and machine readable, binary, object code form. The po
 * Software was developed in conjunction with the National Cancer Institute
 * (NCI) by NCI employees and 5AM Solutions, Inc. (5AM). To the extent
 * government employees are authors, any rights in such works shall be subject
 * to Title 17 of the United States Code, section 105.
 *
 * This po Software License (the License) is between NCI and You. You (or
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
 * its rights in the po Software to (i) use, install, access, operate,
 * execute, copy, modify, translate, market, publicly display, publicly perform,
 * and prepare derivative works of the po Software; (ii) distribute and
 * have distributed to and by third parties the po Software and any
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.mapping.PersistentClass;

import com.fiveamsolutions.nci.commons.util.HibernateHelper;
import com.fiveamsolutions.nci.commons.util.ProxyUtils;

/**
 *
 * @author gax
 */
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.TooManyMethods" })
public class DefaultProcessor {
    private static final String RAWTYPES = "rawtypes";
    private static final Logger LOG = Logger.getLogger(DefaultProcessor.class);

    @SuppressWarnings(RAWTYPES)
    private final List<Class> autitableClasses = new ArrayList<Class>(1);
    private HibernateHelper hibernateHelper;

    /**
     * @param auditables persistent class to audit.
     */
    @SuppressWarnings(RAWTYPES)
    public DefaultProcessor(Class... auditables) {
        autitableClasses.addAll(Arrays.asList(auditables));
    }

    /**
     * only persistent classes implementing the {@link Auditable} marker interface are audited.
     */
    public DefaultProcessor() {
        this(Auditable.class);
    }

    /**
     * @param hibernateHelper the helper to use.
     */
    public void setHibernateHelper(HibernateHelper hibernateHelper) {
        this.hibernateHelper = hibernateHelper;
    }

    /**
     * @return the helper being used.
     */
    public HibernateHelper getHibernateHelper() {
        return hibernateHelper;
    }

    /**
     * @return list of auditable classes.
     */
    @SuppressWarnings(RAWTYPES)
    public List<Class> getAutitableClasses() {
        return autitableClasses;
    }

    /**
     * Are we interested in auditing this entity?
     * By default, the class of the entity determines its auditability.
     * @see #isAuditableClass(java.lang.Class)
     * @param o entity who's Auditablility we want to determine.
     * @return true if o is auditable.
     */
    public boolean isAuditableEntity(Object o) {
        if (o == null) {
            return false;
        }
        return isAuditableClass(o.getClass());
    }

    /**
     * @param clazz the type who's auditability we want to determine.
     * @return true if this type is assignable to any of preset auditable classes.
     */
    @SuppressWarnings({"unchecked", RAWTYPES })
    public boolean isAuditableClass(Class clazz) {
        for (Class c : autitableClasses) {
            if (c.isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param entity the entity being processed.
     * @param key the id of the entity, may not be available untill detail processing.
     * @param type action type to audit.
     * @param entityName the entities class name.
     * @param tableName the table the entity is mapped to.
     * @param username the principale responsible for the change being audited.
     * @return an audit log record.
     */
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public AuditLogRecord newAuditLogRecord(Object entity, Serializable key, AuditType type, String entityName,
            String tableName, String username) {
        return new AuditLogRecord(type, tableName, (Long) key, username, new Date());
    }

    /**
     * @param record the record that should cointain this detail.
     * @param entity  the entity who's property we are processing.
     * @param key the id of the entity.
     * @param property the property name.
     * @param columnName the column name associated with this property.
     * @param oldVal value before the property changed.
     * @param newVal value after the property changed.
     */
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public void processDetail(AuditLogRecord record, Object entity, Serializable key,
            String property, String columnName, Object oldVal, Object newVal) {
        AuditLogDetail detail = new AuditLogDetail(record, property, null, null);
        processDetail(detail, oldVal, newVal);
        record.getDetails().add(detail);
    }


    /**
     * @param detail the detail entry for the property change to process.
     * @param oldVal value before the property changed.
     * @param newVal value after the property changed.
     */
    @SuppressWarnings("PMD.CyclomaticComplexity")
    protected void processDetail(AuditLogDetail detail, Object oldVal, Object newVal) {
        if (oldVal instanceof Map<?, ?> || newVal instanceof Map<?, ?>) {
            processDetailMap(detail, (Map<?, ?>) oldVal, (Map<?, ?>) newVal);
        } else if (oldVal instanceof Collection<?> || newVal instanceof Collection<?>) {
            processDetailCollection(detail, (Collection<?>) oldVal, (Collection<?>) newVal);
        } else if (oldVal instanceof String || newVal instanceof String) {
            processDetailString(detail, (String) oldVal, (String) newVal);
        } else {
            processDetailObject(detail, oldVal, newVal);
        }
    }

    /**
     * @param detail the detail entry for the property change to process.
     * @param oldVal value before the property changed.
     * @param newVal value after the property changed.
     */
    protected void processDetailCollection(AuditLogDetail detail, Collection<?> oldVal, Collection<?> newVal) {
        String oldValueStr = getValueString(oldVal);
        String newValueStr = getValueString(newVal);
        processDetailString(detail, oldValueStr, newValueStr);
    }

   /**
     * @param detail the detail entry for the property change to process.
     * @param oldVal value before the property changed.
     * @param newVal value after the property changed.
     */
    protected void processDetailMap(AuditLogDetail detail, Map<?, ?> oldVal, Map<?, ?> newVal) {
        String oldValueStr = getValueString(oldVal);
        String newValueStr = getValueString(newVal);
        processDetailString(detail, oldValueStr, newValueStr);
    }

   /**
     * @param detail the detail entry for the property change to process.
     * @param oldVal value before the property changed.
     * @param newVal value after the property changed.
     */
    protected void processDetailString(AuditLogDetail detail, String oldVal, String newVal) {
        detail.setOldValue(oldVal);
        detail.setNewValue(newVal);
    }

    private String getValueString(Collection<?> value) {
        if (value == null) {
            return null;
        }
        String sep = "";
        StringBuffer sb = new StringBuffer();
        for (Object a : value) {
            if (sb.length() > AuditLogDetail.VALUE_LENGTH) {
                break;
            }
            sb.append(sep);
            sep = ",";

            if (isPersistent(a)) {
                sb.append(getId(a));
            } else {
                escape(sb, ObjectUtils.toString(decomposeIi(a), null));
            }
        }
        return StringUtils.abbreviate(sb.toString(), AuditLogDetail.VALUE_LENGTH);
    }

    @SuppressWarnings("unchecked")
    private Object decomposeIi(Object value) {
        if (value == null) {
            return null;
        }
        if (value.getClass().getName().equals("gov.nih.nci.iso21090.Ii")) {
            try {
                return PropertyUtils.describe(value).toString();
            } catch (Exception e) {
                LOG.warn(e, e);
            }
        }
        return value;

    }

    /**
     * Produces a string formatted as follows "(String.valueOf(k),String.valueOf(v)) ,
     * (String.valueOf(k),String.valueOf(v)), ...".
     * @param map
     * @return a value string of the provided map
     */
    @SuppressWarnings("PMD.ConsecutiveLiteralAppends")
    private String getValueString(Map<?, ?> map) {
        if (map == null) {
            return null;
        }
        String sep = "";
        StringBuffer sb = new StringBuffer();
        for (Object k : map.keySet()) {
            if (sb.length() > AuditLogDetail.VALUE_LENGTH) {
                break;
            }
            Object v = map.get(k);
            if (isPersistent(k)) {
                k = getId(k);
            }
            if (v == null) {
                v = "";
            } else if (isPersistent(v)) {
                v = getId(v);
            }
            sb.append(sep);
            sb.append('(');
            escape(sb, ObjectUtils.toString(k, null));
            sb.append(',');
            escape(sb, ObjectUtils.toString(v, null));
            sb.append(')');
            sep = ",";
        }
        return StringUtils.abbreviate(sb.toString(), AuditLogDetail.VALUE_LENGTH);
    }

    /**
     * @param entity an entity.
     * @return the id of an entity.
     */
    protected Long getId(Object entity) {
        // Note: Only Long primary keys are supported.
        @SuppressWarnings(RAWTYPES)
        Class c = ProxyUtils.unEnhanceClass(entity.getClass());
        PersistentClass pc = hibernateHelper.getConfiguration().getClassMapping(c.getName());
        if (pc == null) {
            throw new IllegalArgumentException("not persistent");
        }
        pc.getIdentifierProperty().getGetter(c).get(entity);
        return (Long) pc.getIdentifierProperty().getGetter(c).get(entity);
    }

    /**
     * @param entity an entity.
     * @return true if this object is an entity handled by the hibernateHelper.
     */
    protected boolean isPersistent(Object entity) {
        if (entity == null) {
            return false;
        }
        @SuppressWarnings(RAWTYPES)
        Class c = ProxyUtils.unEnhanceClass(entity.getClass());
        PersistentClass pc = hibernateHelper.getConfiguration().getClassMapping(c.getName());
        return pc != null;
    }


    /**
     * @param detail the detail entry for the property change to process.
     * @param oldVal value before the property changed.
     * @param newVal value after the property changed.
     */
    protected void processDetailObject(AuditLogDetail detail, Object oldVal, Object newVal) {
        String o, n;
        if (isPersistent(oldVal)) {
            o = getId(oldVal).toString();
        } else {
            o = ObjectUtils.toString(oldVal, null);
        }
        if (isPersistent(newVal)) {
            n = getId(newVal).toString();
        } else {
            n = ObjectUtils.toString(newVal, null);
        }
        processDetailString(detail, o, n);
    }

    /**
     * encodes individual values in multi-value (eg Collection) attribute.
     * @param result where to write the encoded value.
     * @param val an individual value in the collection.
     */
    public static void escape(StringBuffer result, CharSequence val) {
        if (val == null) {
            return;
        } else if (val.length() == 0) {
            result.append("\"\"");
        } else {
            for (int i = 0; i < val.length(); i++) {
                char c = val.charAt(i);
                if (c == '\\' || c == ',' || c == '(' || c == ')') {
                    result.append('\\');
                }
                result.append(c);
            }
        }
    }

    /**
     * decode individual values in the CSV.
     * @param val value in a CSV column.
     * @return actual value.
     */
    public static String unescape(String val) {
        if (val == null || val.length() == 0) {
            return null;
        }
        if ("\"\"".equals(val)) {
            return "";
        }
        StringBuffer sb = new StringBuffer(val.length());
        for (int i = 0; i < val.length(); i++) {
            char c = val.charAt(i);
            if (c == '\\') {
                if (i == val.length()) {
                    LOG.warn("unexpected escape character at end of string.");
                    break;
                }
                c = val.charAt(++i);
                if (c != '\\' && c != ',' && c != '(' && c != ')') {
                    LOG.warn("character '" + c + "' at index " + i + " is not special (does not need escaping)");
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
