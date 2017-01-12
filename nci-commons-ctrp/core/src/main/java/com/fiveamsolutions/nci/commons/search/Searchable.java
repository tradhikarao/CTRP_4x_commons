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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker interface for methods that should be part of search criteria.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("PMD.VariableNamingConventions")
public @interface Searchable {

    /**
     * The constant for the exact match mode.
     */
    String MATCH_MODE_EXACT = "exact";

    /**
     * The constant for the start match mode.
     */
    String MATCH_MODE_START = "start";

    /**
     * The constant for the contains match mode.
     */
    String MATCH_MODE_CONTAINS = "contains";

    /**
     * The constant for the less than match mode.
     */
    String MATCH_MODE_LESS = "less";

    /**
     * The constant for the less than or equal to match mode.
     */
    String MATCH_MODE_LESS_OR_EQUAL = "less_or_equal";

    /**
     * The constant for the greater than match mode.
     */
    String MATCH_MODE_GREATER = "greater";

    /**
     * The constant for the greater than or equal to match mode.
     */
    String MATCH_MODE_GREATER_OR_EQUAL = "greater_or_equal";

    /**
     * The constant for the not equal to match mode.
     */
    String MATCH_MODE_NOT_EQUAL = "not_equal";

    /**
     * If the field being searched has nested properties, you can specify which properties to
     * use in the search criteria using this value.
     *
     * @Searchable(fields = { "name", "abbreviatedName"})
     * public Organization getOrg.....
     *
     * would result in a search being performed on both name and abbreviatedName.
     *
     * If this searchable is applied to a collection, setting this property
     * will cause the generic search interface to extract the named field
     * (via a getter) and use the values therein for the search.
     *
     * <p>Example: Imagine the following code:
     *
     * <tt>
     * @Entity
     * public class MyClass {
     *   ...
     *   @Searchable(fields = "value")
     *   public Collection&lt;Email&gt; getEmails() { ... }
     * }
     * </tt>
     *
     * The resultant HQL clause will look something like:
     *
     * <tt>
     *   SELECT obj FROM MyClass obj, Email obj_email
     *   WHERE  obj_email = SOME ELEMENTS(obj.emails)
     *   AND    obj_email.value IN ('value1', 'value2')
     * </tt>
     *
     * Where value1 and value2 are the <code>value</code> property from
     * each element of the collection.
     *
     * Specifying both <code>nested = true</code> and <code>fields</code> will result in an IllegalArgumentException.
     */
    String[] fields() default { };

    /**
     * The match mode to use. "exact" and "start" are allowed.
     */
    String matchMode() default MATCH_MODE_EXACT;

    /**
     * The case sensitivity mode to use.
     */
    boolean caseSensitive() default false;

    /**
     * If the field being searched has nested properties, you can look into that field for additional properties
     * that are searchable.  For example:
     *
     * @Searchable(nested = true)
     * public Organization getOrg.....
     * would result in a search being performed on all searchable fields of Organization.
     *
     * If this searchable is applied to a collection, setting this property will cause the generic search interface to
     * extract the searchable fields from each element of the collection.
     *
     * Specifying both <code>nested = true</code> and <code>fields</code> will result in an IllegalArgumentException.
     * In addition, this property is currently ignored for collections.
     */
    boolean nested() default false;

    /**
     * Set this to true if the field is a hibernate component that does not have any hibernate annotations inside it
     * (ie, all hibernate settings are on the getter for the object, rather than on the object's getters).  This
     * property should only be set to true if nested = false, the object is not a Collection, and fields have been
     * specified.
     *
     * <p>Example: Imagine the following code:
     *
     * <tt>
     * @Entity
     * public class MyClass {
     *   ...
     *   @Searchable(fields = "value", isHibernateComponent = true)
     *   public Email getEmail() { ... }
     * }
     * </tt>
     *
     * The resultant HQL clause will look something like:
     *
     * <tt>
     *   SELECT obj FROM MyClass obj
     *   WHERE obj.email.value IN ('value1', 'value2')
     * </tt>
     *
     * Note that no explicit join was added to the from clause.
     */
    boolean isHibernateComponent() default false;
}
