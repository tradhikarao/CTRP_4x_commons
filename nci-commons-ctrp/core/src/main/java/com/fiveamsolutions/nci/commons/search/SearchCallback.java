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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.fiveamsolutions.nci.commons.data.persistent.PersistentObject;
import com.fiveamsolutions.nci.commons.service.GenericSearchService;

/**
 * Callback implementation.
 */
@SuppressWarnings({ "PMD.AvoidStringBufferField", "PMD.TooManyMethods", "PMD.ExcessiveParameterList",
        "PMD.ExcessiveClassLength" })
class SearchCallback extends AbstractQueryGenerationCallback implements SearchableUtils.AnnotationCallback {

    private final StringBuffer selectClause;
    // use == for comparisons, rather than equals()
    private final Map<Object, Object> nestedHistory = new IdentityHashMap<Object, Object>();
    // we need to maintain both uniqueness (only adding a join clause once) but also ordering (so that top-level
    // joins are done first), so use a LinkedHashSet instead of a normal HashSet
    private final Set<String> nestedJoinClauses = new LinkedHashSet<String>();
    private final Map<String, NestedSearchField> nestedSearchFields = new HashMap<String, NestedSearchField>();

    /**
     * @param whereClause string buffer containing the where clause
     * @param selectClause string buffer containing the select clause
     * @param params map of query parameter name to parameter value
     */
    public SearchCallback(StringBuffer whereClause, StringBuffer selectClause, Map<String, Object> params) {
        super(whereClause, params);
        this.selectClause = selectClause;
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void callback(Method m, Object result, String objAlias) throws Exception {
        this.objectAlias = objAlias;
        String fieldName = StringUtils.uncapitalize(m.getName().substring("get".length()));
        String paramName = objectAlias + "_" + fieldName;
        if (result == null) {
            return;
        }
        SearchOptions searchOptions = new SearchOptions(m);
        validateSettings(searchOptions, result);

        if (result instanceof Collection<?>) {
            processCollectionField((Collection<?>) result, fieldName, this.objectAlias, searchOptions);
        } else if (!ArrayUtils.isEmpty(searchOptions.getFields())) {
            processFieldWithSubProp(result, fieldName, paramName, searchOptions, false);
        } else if (searchOptions.isNested()) {
            processNestedField(result, fieldName, paramName, searchOptions);
        } else {
            processSimpleField(result, fieldName, paramName, searchOptions);
        }
    }

    private void validateSettings(SearchOptions searchOptions, Object result) {

        boolean isCollection = result instanceof Collection<?>;

        boolean isPersistentObject = result instanceof PersistentObject;

        if (!ArrayUtils.isEmpty(searchOptions.getFields()) && searchOptions.isNested()) {
            throw new IllegalArgumentException("Both fields and nested cannot be set at the same time.");
        }

        validateIsHibernateComponentSetting(searchOptions.isHibernateComponent(), ArrayUtils.isEmpty(searchOptions
                .getFields()), isCollection, isPersistentObject);

    }

    private void validateIsHibernateComponentSetting(boolean isHibernateComponent, boolean isFieldsEmpty,
            boolean isCollection, boolean isPersistentObject) {
        if (isHibernateComponent && (isFieldsEmpty
                || isCollection || isPersistentObject)) {
            throw new IllegalArgumentException(
                    "isHibernateComponent may only be set on a non-collection and non-PersistentObject,"
                    + " and fields must be provided.");
        }
    }

    private void processNestedField(Object result, String fieldName, String paramName, SearchOptions searchOptions) {
        if (!nestedHistory.containsKey(result)) {
            if (result instanceof PersistentObject && ((PersistentObject) result).getId() != null) {
                // don't update nestedHistory here, searching only on ID doesn't allow for infinite loops
                // but does allow for the same PersistentObject to be used repeatedly in a search
                searchOptions.setFields(new String[] {"id"});
                processFieldWithSubProp(result, fieldName, paramName, searchOptions, false);
            } else {
                // update nestedHistory here to prevent infinite loops
                nestedHistory.put(result, result);

                String objAlias = objectAlias + "_" + fieldName;
                if (!searchOptions.isHibernateComponent()) {
                    String joinClause =
                            String.format(" join %s.%s %s_%s ", this.objectAlias, fieldName, this.objectAlias,
                                    fieldName);
                    selectClause.append(joinClause);
                } else {
                    objAlias = objectAlias + "." + fieldName;
                }

                SearchableUtils.iterateAnnotatedMethods(result, this, objAlias);
            }
        }
    }



    private void processFieldWithSubProp(Object result, String fieldName, String paramName,
            SearchOptions searchOptions, boolean inNestedCollection) {
        for (String currentProp : searchOptions.getFields()) {
            String subPropParamName = paramName + "_" + currentProp;
            Object subPropResult = getSimpleProperty(result, currentProp);

            if (subPropResult != null) {
                if (isStringAndBlank(subPropResult)) {
                    continue;
                }
                if (inNestedCollection) {
                    handleNestedFieldInCollection(subPropParamName, paramName, currentProp, searchOptions,
                            subPropResult);
                } else {
                    handleProcessFieldWithSubProp(subPropResult, fieldName, searchOptions, currentProp,
                            subPropParamName);
                }

            }
        }
    }

    private Object getSimpleProperty(Object bean, String property) {
        Object subPropResult = null;
        try {
            subPropResult = PropertyUtils.getSimpleProperty(bean, property);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to process property with name:" + property, e);
        }
        return subPropResult;
    }

    private void handleProcessFieldWithSubProp(Object subPropResult, String fieldName, SearchOptions searchOptions,
            String currentProp, String subPropParamName) {
        whereClause.append(whereOrAnd);
        whereOrAnd = SearchableUtils.AND;
        String hqlField = String.format("%s.%s.%s", this.objectAlias, fieldName, currentProp);
        whereClause.append(determineModeMatchAndCase(subPropResult, subPropParamName, searchOptions.getSearchMethod(),
                searchOptions.isCaseSensitive(), hqlField));
    }

    private void processCollectionField(Collection<?> col, String fieldName, String baseObjectAlias,
            SearchOptions searchOptions) throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        validateCollection(col, searchOptions);

        if (col.isEmpty()) {
            // empty collection of values means no search criteria.
            return;
        }

        String collectionTableAlias = baseObjectAlias + "_" + fieldName;
        // join clause is "<alias>.<field> <alias>_<field>"
        String collectionTableJoin = String.format("%s.%s %s", baseObjectAlias, fieldName, collectionTableAlias);

        Object firstCollectionObject = col.iterator().next();

        if (searchOptions.isNested()) {
            nestedJoinClauses.add(collectionTableJoin);
            List<Method> methods = SearchableUtils.getSearchableMethods(firstCollectionObject);
            for (Method m : methods) {
                for (Object collectionObject : col) {
                    Object result = m.invoke(collectionObject);
                    dispatchCollectionNestedField(collectionTableAlias, m, result);
                }
            }

            updateQueryClausesForCollection();

        } else {
            Class<? extends Object> fieldClass = firstCollectionObject.getClass();
            StringBuffer processCollectionPropertiesResult =
                    processCollectionProperties(collectionTableAlias, col, fieldClass, searchOptions);
            if (processCollectionPropertiesResult.length() > 0) {
                // add " join obj.<field> obj_<field>" to select clause
                selectClause.append(String.format(" join %s.%s %s", baseObjectAlias, fieldName, collectionTableAlias));

                // now add the where clauses
                whereClause.append(whereOrAnd);
                whereOrAnd = SearchableUtils.AND;
                whereClause.append(" ( ");
                whereClause.append(processCollectionPropertiesResult);
                whereClause.append(" ) ");
            }
        }
    }

    private void updateQueryClausesForCollection() {
        if (!nestedSearchFields.isEmpty()) {
            for (String joinClause : nestedJoinClauses) {
                selectClause.append(" join ").append(joinClause);
            }
            nestedJoinClauses.clear();

            whereClause.append(whereOrAnd);
            whereOrAnd = SearchableUtils.AND;
            whereClause.append(" ( ");
            String andClause = "";
            for (NestedSearchField nsf : nestedSearchFields.values()) {
                whereClause.append(buildCollectionPropWhereClause(nsf.getContainingTableAlias(), nsf.getMatchMode(),
                        nsf.isCaseSensitive(), andClause, nsf.getFieldName(), nsf.getValues()));
                andClause = SearchableUtils.AND;
            }
            nestedSearchFields.clear();
            whereClause.append(" ) ");
        } else {
            nestedJoinClauses.clear();
        }
    }

    private void dispatchCollectionNestedField(String baseParamName, Method m, Object result)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        String fieldName = StringUtils.uncapitalize(m.getName().substring("get".length()));
        String paramName = baseParamName + "_" + fieldName;
        SearchOptions searchOptions = new SearchOptions(m);
        validateSettings(searchOptions, result);

        if (result != null) {
            if (result instanceof Collection<?>) {
                processCollectionField((Collection<?>) result, fieldName, baseParamName, searchOptions);
            } else if (!ArrayUtils.isEmpty(searchOptions.getFields())) {
                if (!searchOptions.isHibernateComponent()) {
                    nestedJoinClauses.add(String.format("%s.%s %s", baseParamName, fieldName, paramName));
                } else {
                    paramName = String.format("%s.%s", baseParamName, fieldName);
                }
                processFieldWithSubProp(result, fieldName, paramName, searchOptions, true);
            } else if (searchOptions.isNested()) {
                handleNestedCollectionNestedField(baseParamName, result, fieldName, paramName, searchOptions);
            } else {
                // just a simple field
                handleNestedFieldInCollection(paramName, baseParamName, fieldName, searchOptions, result);
            }
        }
    }

    private void handleNestedCollectionNestedField(String baseParamName, Object result, String fieldName,
            String paramName, SearchOptions searchOptions) throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        if (!nestedHistory.containsKey(result)) {

            String paramNameToUse = paramName;
            if (!searchOptions.isHibernateComponent()) {
                nestedJoinClauses.add(String.format("%s.%s %s", baseParamName, fieldName, paramName));
            } else {
                paramNameToUse = String.format("%s_%s", baseParamName, fieldName);
            }
            if (result instanceof PersistentObject && ((PersistentObject) result).getId() != null) {
                // don't update nestedHistory here, searching only on ID doesn't allow for infinite loops
                // but does allow for the same PersistentObject to be used repeatedly in a search
                searchOptions.setFields(new String[] {"id"});
                processFieldWithSubProp(result, fieldName, paramNameToUse, searchOptions, true);
            } else {
                // update nestedHistory here to prevent infinite loops
                nestedHistory.put(result, result);
                List<Method> nestedMethods = SearchableUtils.getSearchableMethods(result);
                for (Method nestedMethod : nestedMethods) {
                    Object nestedResult = nestedMethod.invoke(result);
                    dispatchCollectionNestedField(paramNameToUse, nestedMethod, nestedResult);
               }
            }
        }

    }

    private void handleNestedFieldInCollection(String nestedParamName, String collectionTableAlias, String fieldName,
            SearchOptions searchOptions, Object result) {
        if (result != null && !isStringAndBlank(result)) {
            NestedSearchField nsf = nestedSearchFields.get(nestedParamName);
            if (nsf == null) {
                nsf = new NestedSearchField(collectionTableAlias, fieldName, searchOptions.getSearchMethod(),
                                searchOptions.isCaseSensitive());
                nestedSearchFields.put(nestedParamName, nsf);
            }
            nsf.addValue(result);
        }
    }

    private void validateCollection(Collection<?> col, SearchOptions searchOptions) {
        if (ArrayUtils.isEmpty(searchOptions.getFields()) && !searchOptions.isNested()) {
            throw new IllegalArgumentException("Cannot use the searchable annotation on a collection without"
                    + " specifying at least one field name or nested.");
        }

        if (col.size() > GenericSearchService.MAX_IN_CLAUSE_SIZE) {
            throw new IllegalArgumentException(String.format("Cannot query on more than %s elements.",
                    GenericSearchService.MAX_IN_CLAUSE_SIZE));
        }
    }

    private StringBuffer processCollectionProperties(String collectionTableAlias,  Collection<?> col,
            Class<? extends Object> fieldClass, SearchOptions searchOptions)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        StringBuffer result = new StringBuffer();
        String andClause = "";
        for (String propName : searchOptions.getFields()) {
            boolean sensitive = searchOptions.isCaseSensitive();
            Collection<Object> values = getNonNullValues(col, fieldClass, propName, sensitive);

            if (!values.isEmpty()) {
                String searchMethod = searchOptions.getSearchMethod();
                result.append(buildCollectionPropWhereClause(collectionTableAlias, searchMethod, sensitive, andClause,
                        propName, values));
                andClause = SearchableUtils.AND;
            }
        }
        return result;
    }

    private StringBuffer buildCollectionPropWhereClause(String collectionTableAlias, String searchMethod,
            boolean sensitive, String andClause, String propName, Collection<Object> values) {
        boolean exactSearch = searchMethod.equals(Searchable.MATCH_MODE_EXACT);
        // exchange all dots for underscores
        StringBuffer result = new StringBuffer();
        result.append(andClause);
        if (!exactSearch) {
            result.append(addLikeClausesForCollectionProp(collectionTableAlias, propName, values, searchMethod,
                    sensitive).toString());
        } else {
            result.append(addInClauseForCollectionProp(collectionTableAlias, propName, values, sensitive).toString());
        }
        return result;
    }

    /**
     * Extracts the non-null values from the collection and adjusts them for case insensitivity, if necessary.
     */
    private Collection<Object> getNonNullValues(Collection<?> col, Class<? extends Object> fieldClass, String propName,
            boolean caseSenstive) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method m2 = fieldClass.getMethod("get" + StringUtils.capitalize(propName));
        Collection<Object> valueCollection = new HashSet<Object>();
        for (Object collectionObj : col) {
            Object val = m2.invoke(collectionObj);
            if (val != null && !isStringAndBlank(val)) {
                if (canBeUsedInLikeExpression(val) && !caseSenstive) {
                    String strValue = (String) val;
                    valueCollection.add(strValue.toLowerCase(Locale.getDefault()));
                } else {
                    valueCollection.add(val);
                }
            }
        }
        return valueCollection;
    }

    private StringBuffer addLikeClausesForCollectionProp(String collectionTableAlias, String propName,
            Collection<Object> valueCollection, String searchMethod, boolean sensitive) {
        StringBuffer result = new StringBuffer();
        result.append(" (");
        int i = 0;
        for (Object val : valueCollection) {
            if (i != 0) {
                result.append(" " + SearchableUtils.OR + " ");
            }

            String subParamName = new String(collectionTableAlias + "_" + propName + "_" + i).trim().replace('.', '_');

            String hqlField = collectionTableAlias + "." + propName;
            result.append(determineModeMatchAndCase(val, subParamName, searchMethod, sensitive, hqlField));

            i++;
        }
        result.append(") ");
        return result;
    }

    private StringBuffer addInClauseForCollectionProp(String collectionTableAlias, String propName,
            Collection<Object> valueCollection, boolean caseSensitive) {
        String hqlField = collectionTableAlias + "." + propName;
        boolean likeable = canBeUsedInLikeExpression(valueCollection.iterator().next());

        StringBuffer result = new StringBuffer();
        result.append(determineSensitivity(hqlField, caseSensitive, likeable));

        String subParamName = new String(collectionTableAlias + "_" + propName).trim().replace('.', '_');
        result.append(String.format(" in (:%s)", subParamName));

        params.put(subParamName, valueCollection);
        return result;
    }

    public Object getSavedState() {
        return whereOrAnd;
    }


    /**
     * Helper class to hold the information about a search field.
     * @author Steve Lustbader
     */
    private class NestedSearchField {
        // the HQL alias for the table containing this field; eg, "obj_researchOrganizations"
        private final String containingTableAlias;
        // the field name for this field; eg, "name."  Can be combined with the containingTableAlias to form either
        // join clauses ("join obj_researchOrganizations.addresses obj_researchOrganizations_addresses") or bind
        // parameter names (":obj_researchOrganizations_name")
        private final String fieldName;
        private final String matchMode;
        private final boolean caseSensitive;
        private final Set<Object> values = new HashSet<Object>();

        /**
         * Constructor.
         * @param containingTableAlias HQL alias for the table containing this field; eg, "obj_researchOrganizations"
         * @param fieldName the field name for this field; eg, "name"
         * @param matchMode match mode for this field
         * @param caseSensitive case sensitivity of this field
         */
        public NestedSearchField(String containingTableAlias, String fieldName, String matchMode,
                boolean caseSensitive) {
            this.containingTableAlias = containingTableAlias;
            this.fieldName = fieldName;
            this.matchMode = matchMode;
            this.caseSensitive = caseSensitive;
        }

        /**
         * @return the matchMode
         */
        public String getMatchMode() {
            return matchMode;
        }

        /**
         * @return the caseSensitive
         */
        public boolean isCaseSensitive() {
            return caseSensitive;
        }

        /**
         * @return the values
         */
        public Set<Object> getValues() {
            return values;
        }

        /**
         * @param value
         */
        public void addValue(Object value) {
            if (canBeUsedInLikeExpression(value) && !caseSensitive) {
                values.add(((String) value).toLowerCase(Locale.getDefault()));
            } else {
                values.add(value);
            }
        }

        /**
         * @return the containingTableAlias
         */
        public String getContainingTableAlias() {
            return containingTableAlias;
        }

        /**
         * @return the fieldName
         */
        public String getFieldName() {
            return fieldName;
        }
    }
}
