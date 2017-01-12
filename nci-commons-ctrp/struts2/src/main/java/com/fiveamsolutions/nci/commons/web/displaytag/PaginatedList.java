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
package com.fiveamsolutions.nci.commons.web.displaytag;

import java.util.List;

import org.displaytag.properties.SortOrderEnum;

/**
 * Default simple implementation of display tags paginated list interface.
 * @param <T> type of object in the underlying list
 * @author Scott Miller
 */
public class PaginatedList<T> implements org.displaytag.pagination.PaginatedList {

    /**
     * The default page size.
     */
    public static final int DEFAULT_PAGE_SIZE = 20;

    private int fullListSize;
    private List<T> list;
    private int objectsPerPage;
    private int pageNumber;
    private String searchId;
    private String sortCriterion;
    private SortOrderEnum sortDirection;

    /**
     * Default constructor.
     */
    public PaginatedList() {
        this(0, null, DEFAULT_PAGE_SIZE, 1, null, null, SortOrderEnum.ASCENDING);
    }

    /**
     * Set all args constructor.
     * @param fullListSize the list size
     * @param list the list
     * @param objectsPerPage the number of objects per page
     * @param pageNumber the current page number
     * @param searchId the search id
     * @param sortCriterion the sort criterion
     * @param sortDirection the sort order
     */
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public PaginatedList(int fullListSize, List<T> list, int objectsPerPage, int pageNumber, String searchId,
            String sortCriterion, SortOrderEnum sortDirection) {
        this.fullListSize = fullListSize;
        this.list = list;
        this.objectsPerPage = objectsPerPage;
        this.pageNumber = pageNumber;
        this.searchId = searchId;
        this.sortCriterion = sortCriterion;
        this.sortDirection = sortDirection;
    }

    /**
     * {@inheritDoc}
     */
    public int getFullListSize() {
        return fullListSize;
    }

    /**
     * @param fullListSize the fullListSize to set
     */
    public void setFullListSize(int fullListSize) {
        this.fullListSize = fullListSize;
    }

    /**
     * {@inheritDoc}
     */
    public List<T> getList() {
        return list;
    }

    /**
     * @param list the list to set
     */
    public void setList(List<T> list) {
        this.list = list;
    }

    /**
     * {@inheritDoc}
     */
    public int getObjectsPerPage() {
        return objectsPerPage;
    }

    /**
     * @param objectsPerPage the objectsPerPage to set
     */
    public void setObjectsPerPage(int objectsPerPage) {
        this.objectsPerPage = objectsPerPage;
    }

    /**
     * {@inheritDoc}
     */
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * @param pageNumber the pageNumber to set
     */
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    /**
     * {@inheritDoc}
     */
    public String getSearchId() {
        return searchId;
    }

    /**
     * @param searchId the searchId to set
     */
    public void setSearchId(String searchId) {
        this.searchId = searchId;
    }

    /**
     * {@inheritDoc}
     */
    public String getSortCriterion() {
        return sortCriterion;
    }

    /**
     * @param sortCriterion the sortCriterion to set
     */
    public void setSortCriterion(String sortCriterion) {
        this.sortCriterion = sortCriterion;
    }

    /**
     * {@inheritDoc}
     */
    public SortOrderEnum getSortDirection() {
        return sortDirection;
    }

    /**
     * @param sortDirection the sortDirection to set
     */
    public void setSortDirection(SortOrderEnum sortDirection) {
        this.sortDirection = sortDirection;
    }
}
