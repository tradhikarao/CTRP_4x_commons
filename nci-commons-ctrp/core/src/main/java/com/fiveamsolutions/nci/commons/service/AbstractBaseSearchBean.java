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
package com.fiveamsolutions.nci.commons.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.hibernate.Query;

import com.fiveamsolutions.nci.commons.data.persistent.PersistentObject;
import com.fiveamsolutions.nci.commons.data.search.PageSortParams;
import com.fiveamsolutions.nci.commons.data.search.SortCriterion;
import com.fiveamsolutions.nci.commons.search.OneCriterionRequiredException;
import com.fiveamsolutions.nci.commons.search.SearchCriteria;
import com.fiveamsolutions.nci.commons.search.SearchableUtils;

/**
 * @param <T> what this service can search for
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public abstract class AbstractBaseSearchBean<T extends PersistentObject> implements
        GenericSearchService<T, SearchCriteria<T>> {

    /**
     * Maximum number of ids in an IN clause.
     */
    public static final int MAX_IN_CLAUSE_SIZE = 500;

    private static final String ORDER_BY = " ORDER BY ";
    private static final String ASC = " ASC ";
    private static final String DESC = " DESC ";
    private static final String COMMA = " , ";
   
    /**
     * @param sc criteria object to validate
     */
    protected void validateSearchCriteria(SearchCriteria<T> sc) {
        if (sc == null) {
            throw new OneCriterionRequiredException();
        }
        sc.isValid();
    }

    /**
     * {@inheritDoc}
     */
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<T> search(SearchCriteria<T> criteria) {
        return search(criteria, null);
    }

    /**
     * {@inheritDoc}
     */
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<T> search(SearchCriteria<T> criteria, PageSortParams<T> pageSortParams) {
        validateSearchCriteria(criteria);
        StringBuffer orderBy = new StringBuffer("");
        StringBuffer joinClause = new StringBuffer("");
        if (pageSortParams != null) {
            processFixedSortCriteria(criteria, pageSortParams, orderBy, joinClause);
            processDynamicSortCriteria(criteria, pageSortParams, orderBy);
        }

        Query q = criteria.getQuery(orderBy.toString(), joinClause.toString(), false);

        if (pageSortParams != null) {
            q.setMaxResults(pageSortParams.getPageSize());
            if (pageSortParams.getIndex() > 0) {
                q.setFirstResult(pageSortParams.getIndex());
            }
        }

        return getResultList(q);
    }
    

   

    /**
     * 
     * @param criteria search criteria
     * @param pageSortParams the parameters for page sort
     * @param orderBy order by criteria
     * @param joinClause join clause
     */
    protected void processFixedSortCriteria(SearchCriteria<T> criteria, PageSortParams<T> pageSortParams,
            StringBuffer orderBy, StringBuffer joinClause) {
        if (pageSortParams.getSortCriteria() != null && !pageSortParams.getSortCriteria().isEmpty()) {
            boolean first = orderBy.length() == 0;
            Map<SortCriterion<T>, String> aliasMap = new HashMap<SortCriterion<T>, String>();
            if (first) {
                orderBy.append(ORDER_BY);
            }
            for (SortCriterion<T> sc : pageSortParams.getSortCriteria()) {
                if (!first) {
                    orderBy.append(COMMA);
                }
                if (sc.getLeftJoinField() == null) {
                    orderBy.append(criteria.getRootAlias());
                } else {
                    aliasMap.put(sc, SearchableUtils.getNextAlias());
                    orderBy.append(aliasMap.get(sc));
                }
                orderBy.append('.');
                orderBy.append(sc.getOrderField());
                orderBy.append((pageSortParams.isDesc() ? DESC : ASC));

                first = false;
            }
            joinClause.append(getLeftJoinClause(criteria, pageSortParams, aliasMap));
        }
    }
    
    /**
     * 
     * @param criteria search criteria
     * @param pageSortParams page sort params
     * @param orderBy order by clause
     */
    protected void processDynamicSortCriteria(SearchCriteria<T> criteria, PageSortParams<T> pageSortParams,
            StringBuffer orderBy) {
        if (pageSortParams.getDynamicSortCriteria() != null && !pageSortParams.getDynamicSortCriteria().isEmpty()) {
            boolean first = orderBy.length() == 0;
            if (first) {
                orderBy.append(ORDER_BY);
            }
            for (String sortCriterion : pageSortParams.getDynamicSortCriteria()) {
                if (!first) {
                    orderBy.append(COMMA);
                }

                orderBy.append(criteria.getRootAlias());
                orderBy.append('.');
                orderBy.append(sortCriterion);
                orderBy.append((pageSortParams.isDesc() ? DESC : ASC));

                first = false;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<T> getResultList(Query q) {
        // due to the use of the DISTINCT keyword we must add the order by fields into
        // the select clause. As a result the result may be a list of arrays. the first element
        // in each array will be the T object we want. If we find arrays in the result list
        // we generate a new list with just the T elements.
        @SuppressWarnings("rawtypes")
        List rawList = q.list();
        boolean isListOfArrays = rawList != null && !rawList.isEmpty() && rawList.get(0).getClass().isArray();

        List<T> returnVal = null;
        if (isListOfArrays) {

            returnVal = new ArrayList<T>();

            for (Object[] arr : (List<Object[]>) rawList) {
                returnVal.add((T) arr[0]);
            }

        } else {
            returnVal = (List<T>) rawList;
        }
        return returnVal;
    }

    private String getLeftJoinClause(SearchCriteria<T> criteria, PageSortParams<T> pageSortParams,
            Map<SortCriterion<T>, String> aliasMap) {
        StringBuffer joinClause = new StringBuffer();
        for (SortCriterion<T> sc : pageSortParams.getSortCriteria()) {
            if (sc.getLeftJoinField() != null) {
                joinClause.append(" LEFT JOIN ");
                joinClause.append(criteria.getRootAlias());
                joinClause.append('.');
                joinClause.append(sc.getLeftJoinField());
                joinClause.append(' ');
                joinClause.append(aliasMap.get(sc));
            }
        }
        return joinClause.toString();
    }

    /**
     * {@inheritDoc}
     */
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public int count(SearchCriteria<T> criteria) {
        validateSearchCriteria(criteria);
        Query q = criteria.getQuery("", true);
        return ((Number) q.uniqueResult()).intValue();
    }
}
