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
package com.fiveamsolutions.nci.commons.data.security;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

import com.fiveamsolutions.nci.commons.data.persistent.PersistentObject;

/**
 * When a user says they forgot their password, this is the class that models that
 * request.  Password resets are little more than a nonce and administrative data.
 */
@Entity
@org.hibernate.annotations.Entity(mutable = false)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PasswordReset implements PersistentObject {

    private static final long serialVersionUID = 2856657693692181122L;
    private static final int NONCE_LENGTH = 28;
    private static final Random R = new SecureRandom();

    /**
     * @param user the user to associate this password reset to
     * @return the newly created instance
     */
    public static PasswordReset newInstance(AbstractUser user) {
        PasswordReset result = new PasswordReset();

        result.setUser(user);
        result.setCreateDate(new Date());

        // Create a string with at least 128 bits of entropy:
        // 26 possible characters -> ~4.7 bits / char
        // 28 digit string > 128 bits
        StringBuffer sb = new StringBuffer(NONCE_LENGTH);
        final int numLetters = 'Z' - 'A';
        for (int i = 0; i < NONCE_LENGTH; ++i) {
            int nextInt = R.nextInt(numLetters);
            sb.append((char) ('A' + nextInt));
        }
        result.setNonce(sb.toString());
        user.getPasswordResets().add(result);

        return result;
    }

    private Long id;
    private String nonce;
    private Date createDate;
    private AbstractUser user;

    /**
     * @deprecated hibernate only for public use
     */
    @Deprecated
    public PasswordReset() {
        // hibernate / static init only
    }

    /**
     * {@inheritDoc}
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    /**
     * @return the createDate
     */
    @NotNull
    public Date getCreateDate() {
        return createDate;
    }

    /**
     * @return the gbUser
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    @ForeignKey(name = "RESET_USER_FK")
    @Index(name = "reset_user_idx")
    @NotNull
    public AbstractUser getUser() {
        return user;
    }

    /**
     * @return the nonce
     */
    @NotEmpty
    @Column(length = NONCE_LENGTH)
    public String getNonce() {
        return nonce;
    }

    @SuppressWarnings("unused")
    private void setId(Long id) {
        this.id = id;
    }

    private void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    private void setNonce(String nonce) {
        this.nonce = nonce;
    }

    private void setUser(AbstractUser user) {
        this.user = user;
    }
}
