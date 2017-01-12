/**
 * The software subject to this notice and license includes both human readable
 * source code form and machine readable, binary, object code form. The COPPA PO
 * Software was developed in conjunction with the National Cancer Institute
 * (NCI) by NCI employees and 5AM Solutions, Inc. (5AM). To the extent
 * government employees are authors, any rights in such works shall be subject
 * to Title 17 of the United States Code, section 105.
 *
 * This COPPA PO Software License (the License) is between NCI and You. You (or
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
 * its rights in the COPPA PO Software to (i) use, install, access, operate,
 * execute, copy, modify, translate, market, publicly display, publicly perform,
 * and prepare derivative works of the COPPA PO Software; (ii) distribute and
 * have distributed to and by third parties the COPPA PO Software and any
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
package com.fiveamsolutions.nci.commons.search;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * Utility functions for searching.
 * 
 * @see Searchable
 */
@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.AvoidDuplicateLiterals", "PMD.ExcessiveClassLength" })
public final class SearchableUtils {

    /**
     * HQL select statement.
     */
    public static final String SELECT = " SELECT ";

    /**
     * HQL distinct statement.
     */
    public static final String DISTINCT = "DISTINCT ";

    /**
     * HQL from statement.
     */
    public static final String FROM = " FROM ";

    /**
     * HQL as keyword.
     */
    public static final String AS = " as ";
    /**
     * HQL comma keyword.
     */
    public static final String COMMA = " , ";

    /**
     * HQL JOIN statement.
     */
    public static final String JOIN = " JOIN ";

    /**
     * HQL WHERE statement.
     */
    public static final String WHERE = " WHERE ";

    /**
     * HQL AND operator.
     */
    public static final String AND = " AND ";

    /**
     * HQL OR operator.
     */
    public static final String OR = " OR ";
    
    /**
     * HQL Having operator.
     */
    public static final String HAVING = "HAVING";
    /**
     * HQL Greater-than-or-equal-to operator.
     */
    public static final String GTE = ">=";
    
    /**
     * HQL Less-than-or-equal-to operator.
     */
    public static final String LTE = "<=";
    
    /**
     * HQL Greater-than operator.
     */
    public static final String GREATER_THAN = ">";
    
    /**
     * HQL Less-than operator.
     */
    public static final String LESS_THAN = "<";

    /**
     * HQL property separator.
     */
    public static final String DOT = ".";

    /**
     * PersistentObject property name.
     */
    public static final String ID = "id";

    /**
     * The searchable framework alias.
     */
    public static final String SEARCHABLE_FRAMEWORK = "searchable_framework_";

    /**
     * The Maximum Integer the alias counter should iterate to before it is reset to 0.
     */
    public static final int MAX_ALIAS_COUNTER = 1000;

    /**
     * root object alias name.
     */
    public static final String ROOT_OBJ_ALIAS = "obj";

    private static final Logger LOG = Logger.getLogger(SearchableUtils.class);
    private static int aliasCounter = 0;

    /**
     * Callback interface.
     */
    public interface AnnotationCallback {
        /**
         * @param m method callback was invoked on
         * @param result result of invoking the method
         * @param objectAlias query alias for the given object
         * @throws Exception on error
         */
        @SuppressWarnings("PMD.SignatureDeclareThrowsException")
        void callback(Method m, Object result, String objectAlias) throws Exception;

        /**
         * @return any callback saved state
         */
        Object getSavedState();
    }

    private SearchableUtils() {
        // prevent instantiation
    }

    private static QueryCallback callback = new QueryCallback();

    /**
     * @param callback new callback (unit tests only)
     */
    protected static void setCallback(QueryCallback callback) {
        SearchableUtils.callback = callback;
    }

    /**
     * Calls through with a null helper.
     * 
     * @param obj object to inspect
     * @param isCountOnly do a count query
     * @param orderByClause hql order by clause, if none leave blank
     * @param session hibernate session
     * @return query object
     */
    public static Query getQueryBySearchableFields(final Object obj, boolean isCountOnly, String orderByClause,
            Session session) {
        return getQueryBySearchableFields(obj, isCountOnly, orderByClause, session, null);
    }

    /**
     * Calls through with a null helper.
     * 
     * @param obj object to inspect
     * @param isCountOnly do a count query
     * @param orderByClause hql order by clause, if none leave blank
     * @param groupByClause hql group by clause, if none leave blank
     * @param session hibernate session
     * @return query object
     */
    public static Query getQueryBySearchableFields(final Object obj, boolean isCountOnly, String orderByClause,
            String groupByClause, Session session) {
        return getQueryBySearchableFields(obj, isCountOnly, orderByClause, groupByClause, session, null);
    }

    /**
     * Calls through after converting the orderByClause into an array of Strings containing the attributes to order by.
     * 
     * @param obj object to inspect
     * @param isCountOnly do a count query
     * @param orderByClause hql order by clause, if none leave blank
     * @param session hibernate session
     * @param helper helper function to call after iterating, but before doing hibernate session work
     * @return query object
     */
    public static Query getQueryBySearchableFields(final Object obj, boolean isCountOnly, String orderByClause,
            Session session, AfterIterationHelper helper) {
        return getQueryBySearchableFields(obj, isCountOnly, orderByClause, "", session, helper);
    }

    /**
     * @param obj object to inspect
     * @param isCountOnly do a count query
     * @param orderByClause hql order by clause, if none leave blank
     * @param leftJoinClause hql left join clause, if none leave blank
     * @param session hibernate session
     * @param helper helper function to call after iterating, but before doing hibernate session work
     * @return query object
     */
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public static Query getQueryBySearchableFields(final Object obj, boolean isCountOnly, String orderByClause,
            String leftJoinClause, Session session, AfterIterationHelper helper) {
        return getQueryBySearchableFields(obj, isCountOnly, orderByClause, "", leftJoinClause, session, helper);
    }

    /**
     * @param obj object to inspect
     * @param isCountOnly do a count query
     * @param orderByClause hql order by clause, if none leave blank
     * @param groupByClause hql group by clause, if none leave blank
     * @param leftJoinClause hql left join clause, if none leave blank
     * @param session hibernate session
     * @param helper helper function to call after iterating, but before doing hibernate session work
     * @return query object
     */
    @SuppressWarnings({ "PMD.ExcessiveParameterList", "PMD.CyclomaticComplexity" })   
    public static Query getQueryBySearchableFields(final Object obj, boolean isCountOnly, String orderByClause,
            String groupByClause, String leftJoinClause, Session session, AfterIterationHelper helper) {   
        return getQueryBySearchableFields(obj, new ArrayList<String>(),
                isCountOnly, orderByClause, groupByClause, leftJoinClause,
                session, helper);
    }
    
    /**
     * @param obj
     *            object to inspect
     * @param isCountOnly
     *            do a count query
     * @param orderByClause
     *            hql order by clause, if none leave blank
     * @param groupByClause
     *            hql group by clause, if none leave blank
     * @param leftJoinClause
     *            hql left join clause, if none leave blank
     * @param session
     *            hibernate session
     * @param helper
     *            helper function to call after iterating, but before doing
     *            hibernate session work
     * @param attributes object attributes to select
     * @return query object
     */
    @SuppressWarnings({ "PMD.ExcessiveParameterList", "PMD.CyclomaticComplexity" })
    // CHECKSTYLE:OFF
    public static Query getQueryBySearchableFields(final Object obj,
            List<String> attributes, boolean isCountOnly, String orderByClause,
            String groupByClause, String leftJoinClause, Session session,
            AfterIterationHelper helper) {
        final StringBuffer selectClause = new StringBuffer(SELECT);
        final Map<String, Object> params = new HashMap<String, Object>();

        constructSelectClause(obj, attributes, isCountOnly, orderByClause,
                groupByClause, leftJoinClause, helper, selectClause, params);
        return callback.doQueryInteraction(session, params, selectClause);
    }
    // CHECKSTYLE:ON
    
    /**
     * 
     * @param obj object to inspect
     * @param isCountOnly do a count query
     * @param orderByClause hql order by clause, if none leave blank
     * @param groupByClause hql group by clause, if none leave blank
     * @param leftJoinClause hql left join clause, if none leave blank
     * @param session hibernate session
     * @param helper helper function to call after iterating, but before doing hibernate session work
     * @param countThreshold Result count threshold (min or max) for aggregate results returned.
     * @param countThresholdOperator Comparison operator for count threshold (greater-than, less-than, etc.)
     * @return query object
     */
    @SuppressWarnings("PMD.ExcessiveParameterList")
    //CHECKSTYLE:OFF - maximum parameters
    public static Query getQueryBySearchableFields(final Object obj, boolean isCountOnly, String orderByClause,
            String groupByClause, String leftJoinClause, Session session, AfterIterationHelper helper, 
            int countThreshold, String countThresholdOperator) {
        //CHECKSTYLE:ON
        final StringBuffer selectClause = new StringBuffer(SELECT);
        final Map<String, Object> params = new HashMap<String, Object>();
        
        constructSelectClause(obj, new ArrayList<String>(), isCountOnly,
                orderByClause, groupByClause, leftJoinClause, helper,
                selectClause, params);
        selectClause.append(" " + HAVING);
        selectClause.append(" COUNT(" + ROOT_OBJ_ALIAS + ") ");
        selectClause.append(countThresholdOperator);
        selectClause.append(' ');
        selectClause.append(Integer.toString(countThreshold));
        return callback.doQueryInteraction(session, params, selectClause);
    }
    
    @SuppressWarnings("PMD.ExcessiveParameterList")
    //CHECKSTYLE:OFF - maximum parameters
    private static void constructSelectClause(final Object obj, List<String> attributes, boolean isCountOnly, String orderByClause,
            String groupByClause, String leftJoinClause, AfterIterationHelper helper, final StringBuffer selectClause, 
            final Map<String, Object> params) {
        //CHECKSTYLE:ON
        final StringBuffer whereClause = new StringBuffer();
        boolean dontIncludeInSelect = false;

        if (StringUtils.isNotBlank(groupByClause)) {
            addGroupByFieldsToSelectClause(selectClause, groupByClause);
            selectClause.append(", COUNT(" + ROOT_OBJ_ALIAS + ") ");
            dontIncludeInSelect = true;
        } else {
            constructDistinctClause(selectClause, attributes, isCountOnly);           
        }

        if (!isCountOnly) {
            addSortFieldsToSelectClause(selectClause, orderByClause);
        }
        
        selectClause.append(FROM);
        selectClause.append(obj.getClass().getName());
        selectClause.append(' ');
        selectClause.append(ROOT_OBJ_ALIAS);

        if (StringUtils.isNotBlank(leftJoinClause)) {
            selectClause.append(leftJoinClause);
        }

        AnnotationCallback ac = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(obj, ac);

        if (helper != null) {
            helper.afterIteration(obj, (isCountOnly || dontIncludeInSelect), whereClause, params);
        }

        selectClause.append(whereClause);

        if (StringUtils.isNotBlank(groupByClause)) {
            selectClause.append(groupByClause);
        } else if (StringUtils.isNotBlank(orderByClause)) {
            selectClause.append(orderByClause);
        }
    }

    /**
     * @param selectClause
     * @param attributes
     * @param isCountOnly
     * @return
     */
    private static void constructDistinctClause(StringBuffer selectClause,
            List<String> attributes, boolean isCountOnly) {
        String distinctClause;
        if (attributes == null || attributes.isEmpty()) {
            distinctClause = DISTINCT.concat(ROOT_OBJ_ALIAS);
        } else {
            distinctClause = DISTINCT;
            for (Iterator<String> iterator = attributes.iterator(); iterator
                    .hasNext();) {
                String attr = (String) iterator.next();
                distinctClause = distinctClause.concat(ROOT_OBJ_ALIAS + DOT
                        + attr);
                if (iterator.hasNext()) {
                    distinctClause = distinctClause.concat(COMMA);
                }
            }
        }
        selectClause.append(isCountOnly ? "COUNT (" + distinctClause + ")"
                : distinctClause);
    }

    private static void addGroupByFieldsToSelectClause(StringBuffer selectClause, String groupByClause) {
        if (StringUtils.isNotBlank(groupByClause)) {
            String sortFields = null;
            sortFields = groupByClause.replaceAll("GROUP BY", "");
            sortFields = sortFields.trim();
            if (StringUtils.isNotBlank(sortFields)) {
                selectClause.append(sortFields);
            }
        }
    }

    private static void addSortFieldsToSelectClause(StringBuffer selectClause, String orderByClause) {
        if (StringUtils.isNotBlank(orderByClause)) {

            String sortFields = null;
            sortFields = orderByClause.replaceAll("ORDER BY", "");
            sortFields = sortFields.replaceAll("ASC", "");
            sortFields = sortFields.replaceAll("DESC", "");
            sortFields = sortFields.trim();
            if (StringUtils.isNotBlank(sortFields)) {
                selectClause.append(", ");
                selectClause.append(sortFields);
            }
        }

    }

    /**
     * Mechanism for users to add extra clauses after every call to getQUeryBySearchableFields.
     */
    public interface AfterIterationHelper {
        /**
         * Callback routine.
         * 
         * @param obj object under consideration.
         * @param isCountOnly count query?
         * @param whereClause current clause (can be modified)
         * @param params current params (can be modified)
         */
        void afterIteration(Object obj, boolean isCountOnly, StringBuffer whereClause, Map<String, Object> params);
    }

    /**
     * Only unit tests should override.
     */
    protected static class QueryCallback {
        /**
         * Broken out so that test classes can override.
         * 
         * @param session session
         * @param params params
         * @param selectClause clause
         * @return query
         */
        public Query doQueryInteraction(Session session, Map<String, Object> params, StringBuffer selectClause) {
            Query q = session.createQuery(selectClause.toString());
            if (LOG.isDebugEnabled()) {
                LOG.debug(q.getQueryString());
            }
            setQueryParams(params, q);
            return q;
        }
    }

    private static void setQueryParams(final Map<String, Object> params, Query q) {
        for (String key : params.keySet()) {
            Object value = params.get(key);
            if (value instanceof Collection<?>) {
                q.setParameterList(key, (Collection<?>) value);
            } else {
                q.setParameter(key, value);
            }
        }
    }

    /**
     * @param o object to search over
     * @param cb callback to use
     */
    public static void iterateAnnotatedMethods(Object o, AnnotationCallback cb) {
        iterateAnnotatedMethods(o, cb, ROOT_OBJ_ALIAS);
    }

    /**
     * @param o object to search over
     * @param cb callback to use
     * @param objectAlias query alias to use in the search
     */
    public static void iterateAnnotatedMethods(Object o, AnnotationCallback cb, String objectAlias) {
        if (o == null) {
            return;
        }
        for (Method m : getSearchableMethods(o)) {
            try {
                cb.callback(m, m.invoke(o), objectAlias);
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format(
                        "The @Searchable annotation cannot be applied to %s.%s()", o.getClass(), m), e);
            }
        }
    }

    /**
     * Get the methods on a given object that are searchable.
     * 
     * @param o object to get methods from
     * @return list of searchable methods
     */
    public static List<Method> getSearchableMethods(Object o) {
        List<Method> methods = new ArrayList<Method>();
        for (Method m : o.getClass().getMethods()) {
            if (m.getAnnotation(Searchable.class) != null) {
                if (m.getReturnType().equals(Void.TYPE)) {
                    throw new IllegalArgumentException(String.format(
                            "The @Searchable annotation cannot be applied to %s.%s() [void return]", o.getClass(), m));
                }
                methods.add(m);
            }
        }
        return methods;
    }

    /**
     * Looks through all Searchable methods and determines if the example object has at least one method that
     * participates in the query by example. For each method marked as searchable, the result of invoking the method is
     * examined as follows:
     * <ul>
     * <li>Null results do not participate in query by example.
     * <li>If the result is a <code>PersistentObject</code> and the <code>getId</code> method returns a non-null value,
     * the object is considered to have a criteria.
     * <li>If the result is a <code>Collection</code> and the <code>isEmpty</code> method returns false, the object is
     * considered to have a criterion.
     * <li>Otherwise, if the result is non-null, the object is considered to have a criterion.
     * </ul>
     * 
     * @param o the object to examine
     * @return whether one (or more) methods participate in the criteria
     */
    public static boolean hasSearchableCriterion(final Object o) {

        AnnotationCallback ac = new OneCriterionSpecifiedCallback();

        iterateAnnotatedMethods(o, ac);
        return (Boolean) ac.getSavedState();
    }

    /**
     * Iterates the aliasCounter and returns the alias used by a dependent field in a SortCriterion.
     * 
     * @return alias String
     */
    public static String getNextAlias() {
        return SEARCHABLE_FRAMEWORK + (++aliasCounter % MAX_ALIAS_COUNTER);
    }

}
