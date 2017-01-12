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

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;

import com.fiveamsolutions.nci.commons.search.AbstractSearchCriteria;
import com.fiveamsolutions.nci.commons.util.HibernateUtil;

/**
 * Class used to store search criteria for finding audit records.
 */
public class AuditLogRecordSearchCriteria extends AbstractSearchCriteria<AuditLogRecord> {

    private static final long serialVersionUID = 1L;
    private static final String ROOT_ALIAS = "alr";
    private final Long id;
    private final Set<Long> transactionId;

    /**
     * @param id id of object to find audit log records for (may be null)
     * @param transactionId transaction id to find audit log records for (may be null)
     */
    public AuditLogRecordSearchCriteria(Long id, Set<Long> transactionId) {
        this.id = id;
        this.transactionId = transactionId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasOneCriterionSpecified() {
        return id != null || transactionId != null;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @return the transaction id
     */
    public Set<Long> getTransactionId() {
        return transactionId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return hasOneCriterionSpecified();
    }

    /**
     * {@inheritDoc}
     */
    public Criteria getCriteria() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("PMD.ConsecutiveLiteralAppends")
    // Can't satisfy this AND line length at the same time
    public Query getQuery(String orderByProperty, boolean isCountOnly) {
        Session session = getCurrentSession();
        StringBuffer query = new StringBuffer("SELECT "
                + (isCountOnly ? "COUNT(distinct " + ROOT_ALIAS + ") " : "distinct " + ROOT_ALIAS) + " FROM "
                + AuditLogRecord.class.getName() + " " + ROOT_ALIAS + "," + AuditLogDetail.class.getName()
                + " ald WHERE ");

        return helpBuildQuery(session, query, orderByProperty);
    }

    /**
     * @return current HB Session
     */
    protected Session getCurrentSession() {
        return HibernateUtil.getCurrentSession();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRootAlias() {
        return ROOT_ALIAS;
    }

    /**
     * {@inheritDoc}
     */
    public Query getQuery(String orderByProperty, String leftJoinClause, boolean isCountOnly) {
        if (StringUtils.isNotBlank(leftJoinClause)) {
            throw new IllegalArgumentException("The use of the left join clause is currently not supported."
                    + " Please ref jira issues PO-1115, PO-1116, PO-1118");
        }

        return getQuery(orderByProperty, isCountOnly);

    }

    private Query helpBuildQuery(Session session, StringBuffer query, String orderByProperty) {
        if (id != null) {
            query.append(String.format(" %s.entityId = :entityId OR ", ROOT_ALIAS));
            query.append(String.format("  (ald in elements(%s.details) ", ROOT_ALIAS));
            query.append("    AND (ald.oldValue = :entityIdStr OR ald.newValue = :entityIdStr)) ");
        } else {
            query.append(String.format(" %s.transactionId in (:transactionIds) ", ROOT_ALIAS));
        }

        query.append(orderByProperty);
        Query q = session.createQuery(query.toString());

        if (id != null) {
            q.setLong("entityId", getId());
            q.setString("entityIdStr", Long.toString(getId()));
        } else {
            q.setParameterList("transactionIds", getTransactionId());
        }

        return q;
    }
}
