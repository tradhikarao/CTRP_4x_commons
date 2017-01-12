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
package com.fiveamsolutions.nci.commons.authentication;

import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import com.fiveamsolutions.nci.commons.data.security.AbstractUser;
import com.fiveamsolutions.nci.commons.data.security.AccountStatus;
import com.fiveamsolutions.nci.commons.data.security.Password;
import com.fiveamsolutions.nci.commons.util.HibernateHelper;
import com.fiveamsolutions.nci.commons.util.SecurityUtils;

/**
 * Database login module that understand the User + Password schema.
 */
public class CommonLoginModule implements LoginModule {

    static final String JBOSS_PASSWORD_STACKING_USER_PARAM = "javax.security.auth.login.name";
    static final String JBOSS_PASSWORD_STACKING_PASSWORD_PARAM = "javax.security.auth.login.password";

    private static final Logger LOG = Logger.getLogger(CommonLoginModule.class);
    private static HibernateHelper hibernateHelper;

    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map<String, ?> sharedState;
    private String name;

    private Principal principal;

    /**
     * {@inheritDoc}
     */
    public void initialize(Subject sub, CallbackHandler handler, Map<String, ?> state, Map<String, ?> opts) {
        LOG.trace("initialize()");
        subject = sub;
        callbackHandler = handler;
        sharedState = state;
    }

    private synchronized HibernateHelper getHibernateHelper() {
        if (hibernateHelper == null) {
            hibernateHelper = new HibernateHelper();
            hibernateHelper.initialize();
        }
        return hibernateHelper;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"unchecked", "PMD.PreserveStackTrace" }) // LoginException can't take a cause
    public boolean login() throws LoginException {
        LOG.trace("login()");
        NameCallback nameCallback = new NameCallback("Username: ");
        PasswordCallback passwordCallback = new PasswordCallback("Password: ", false);
        Callback[] callbacks = new Callback[2];
        callbacks[0] = nameCallback;
        callbacks[1] = passwordCallback;

        try {
            callbackHandler.handle(callbacks);
        } catch (IOException e) {
            LOG.warn(e.getMessage(), e);
            throw new LoginException("Unable to handle callbacks: " + e.getMessage());
        } catch (UnsupportedCallbackException e) {
            LOG.warn(e.getMessage(), e);
            throw new LoginException("Unable to handle callbacks: " + e.getMessage());
        }

        String username = nameCallback.getName();
        char[] password = passwordCallback.getPassword();

        // Do the hibernate manipulation directly, rather than via a service call (we're outside the OSIV filter)
        Session s = getHibernateHelper().getSessionFactory().openSession();
        try {
            Query query = s.createQuery("SELECT username, password, passwordExpirationDate "
                    + " FROM " + AbstractUser.class.getName()
                    + " WHERE lower(username) = lower(:username) and status = :active");
            query.setString("username", username);
            query.setString("active",  AccountStatus.ACTIVE.name());
            Object[] user = (Object []) query.uniqueResult();
            if (user == null || !SecurityUtils.matches((Password) user[1], new String(password))) {
                throw new FailedLoginException("Unknown user and/or bad password");
            }

            checkExpirationDate((Date) user[2]);

            name = (String) user[0];
        } finally {
            s.close();
        }

        // These are the two options JBoss will look for with the useFirstPass option
        ((Map<String, Object>) sharedState).put(JBOSS_PASSWORD_STACKING_USER_PARAM, name);
        ((Map<String, Object>) sharedState).put(JBOSS_PASSWORD_STACKING_PASSWORD_PARAM, password);

        return true; // this module should never be ignored
    }

    private void checkExpirationDate(Date expirationDate) throws LoginException {
        if (expirationDate != null && expirationDate.before(new Date())) {
            throw new CredentialExpiredException("password has expired");
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean abort() throws LoginException {
        LOG.trace("abort()");
        name = null;
        return true; // aborting always works
    }

    /**
     * {@inheritDoc}
     */
    public boolean commit() throws LoginException {
        LOG.trace("commit()");
        principal = new Principal() {
            public String getName() {
                return name;
            }
        };
        subject.getPrincipals().add(principal);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean logout() throws LoginException {
        LOG.trace("logout");
        subject.getPrincipals().remove(principal);
        return true;
    }

}
