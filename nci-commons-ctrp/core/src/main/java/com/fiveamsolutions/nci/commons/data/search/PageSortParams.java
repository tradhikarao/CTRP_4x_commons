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
package com.fiveamsolutions.nci.commons.data.search;

import java.util.Collections;
import java.util.List;


/**
 * A simple bean for specifying the page and sorting parameters for paged / sorted lists.
 *
 * @author Winston Cheng
 * @param <T> the type of persistent class for which this defines page and sorting parameters
 */
public class PageSortParams<T> {
    private int pageSize;
    private int index;
    private List<? extends SortCriterion<T>> sortCriteria;
    private List<String> dynamicSortCriteria;
    private boolean desc;

    /**
     * Constructor.
     * @param pageSize page size
     * @param index start index
     * @param sortCriterion sort criterion
     * @param desc true for descending sort
     */
    public PageSortParams(int pageSize, int index, SortCriterion<T> sortCriterion, boolean desc) {
        this(pageSize, index, (sortCriterion == null)? null : Collections.singletonList(sortCriterion) , desc, null);
    }

    /**
     * Constructor.
     * @param pageSize page size
     * @param index start index
     * @param sortCriteria sort criteria
     * @param desc true for descending sort
     */
    public PageSortParams(int pageSize, int index, List<? extends SortCriterion<T>> sortCriteria, boolean desc) {
        this(pageSize, index, sortCriteria, desc, null);
    }

    /**
     * Constructor.
     * @param pageSize page size
     * @param index start index
     * @param sortCriteria sort criteria
     * @param desc true for descending sort
     * @param dynamicSortCriteria dynamic sort criteria
     */
    public PageSortParams(int pageSize, int index, List<? extends SortCriterion<T>> sortCriteria, boolean desc,
            List<String> dynamicSortCriteria) {
        this.pageSize = pageSize;
        this.index = index;
        this.setSortCriteria(sortCriteria);
        this.desc = desc;
        this.dynamicSortCriteria = dynamicSortCriteria;
    }

    /**
     * @return the pageSize
     */
    public int getPageSize() {
        return pageSize;
    }
    /**
     * @param pageSize the pageSize to set
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }
    /**
     * @param index the index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }
    /**
     * @deprecated use the list version getSortCriteria()
     * @return the first sortCriterion is the list
     */
    @Deprecated
    public SortCriterion<T> getSortCriterion() {
        return getSortCriteria() == null ? null : getSortCriteria().get(0);
    }
    /**
     * @deprecated use the list version setSortCriteria()
     * @param sortCriterion the sortCriterion to set.  Makes the list have a single element.
     */
    @Deprecated
    public void setSortCriterion(SortCriterion<T> sortCriterion) {
        this.setSortCriteria(Collections.singletonList(sortCriterion));
    }

    /**
     * @return true to sort in descending order
     */
    public boolean isDesc() {
        return desc;
    }

    /**
     * @param desc true to sort in descending order
     */
    public void setDesc(boolean desc) {
        this.desc = desc;
    }

    /**
     * @param sortCriteria the sortCriteria to set
     */
    public final void setSortCriteria(List<? extends SortCriterion<T>> sortCriteria) {
        this.sortCriteria = sortCriteria;
    }

    /**
     * @return the sortCriteria
     */
    public final List<? extends SortCriterion<T>> getSortCriteria() {
        return sortCriteria;
    }

    /**
     * @return the dynamicSortCriteria
     */
    public List<String> getDynamicSortCriteria() {
        return this.dynamicSortCriteria;
    }

    /**
     * @param dynamicSortCriteria the dynamicSortCriteria to set
     */
    public void setDynamicSortCriteria(List<String> dynamicSortCriteria) {
        this.dynamicSortCriteria = dynamicSortCriteria;
    }
}
