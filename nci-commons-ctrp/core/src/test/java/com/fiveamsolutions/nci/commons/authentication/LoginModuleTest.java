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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
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

import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;

import com.fiveamsolutions.nci.commons.data.security.AbstractUser;
import com.fiveamsolutions.nci.commons.data.security.Password;
import com.fiveamsolutions.nci.commons.data.security.PasswordType;
import com.fiveamsolutions.nci.commons.test.AbstractHibernateTestCase;

/**
 * Tests the functionality of the CommonLoginModule
 */
public class LoginModuleTest extends AbstractHibernateTestCase {

    @Test
    public void testIt() throws Exception {
        String un = "user";
        String pw = "Password1";
        LoginModule module = new CommonLoginModule();
        Map<String, ?> options = new HashMap<String, Object>();
        Map<String, ?> sharedState = new HashMap<String, Object>();
        Subject subject = new Subject();
        CallbackHandler cbh = new MockCallbackHandler(true);

        module.initialize(subject, cbh, sharedState, options);

        try {
            module.login();
            fail();
        } catch (LoginException e) {
            // expected
        }
        assertTrue(sharedState.isEmpty());
        assertTrue(module.abort());

        cbh = new MockCallbackHandler(false);
        module.initialize(subject, cbh, sharedState, options);

        try {
            module.login();
            fail();
        } catch (LoginException e) {
            // expected
        }
        assertTrue(sharedState.isEmpty());
        assertTrue(module.abort());

        cbh = new MockCallbackHandler(un, "pass".toCharArray());
        module.initialize(subject, cbh, sharedState, options);

        try {
            module.login();
            fail();
        } catch (FailedLoginException e) {
            // expected
        }
        assertTrue(sharedState.isEmpty());
        assertTrue(module.abort());


        createUser(un, pw);
        try {
            module.login();
            fail();
        } catch (FailedLoginException e) {
            // expected
        }
        assertTrue(sharedState.isEmpty());
        assertTrue(module.abort());

        cbh = new MockCallbackHandler(un.toUpperCase(), pw.toCharArray());
        module.initialize(subject, cbh, sharedState, options);

        assertTrue(module.login());
        assertTrue(!sharedState.isEmpty());
        assertEquals(un, sharedState.get("javax.security.auth.login.name"));
        assertEquals(pw, new String((char[]) sharedState.get("javax.security.auth.login.password")));
        assertTrue(subject.getPrincipals().isEmpty());

        assertTrue(module.commit());
        assertTrue(!subject.getPrincipals().isEmpty());
        assertEquals(un, subject.getPrincipals().iterator().next().getName());

        assertTrue(module.logout());
        assertTrue(subject.getPrincipals().isEmpty());
    }

    @Test(expected = CredentialExpiredException.class)
    public void testPasswordExpiration() throws Exception {
        String un = "user";
        String pw = "Password1";
        LoginModule module = new CommonLoginModule();
        Map<String, ?> options = new HashMap<String, Object>();
        Map<String, ?> sharedState = new HashMap<String, Object>();
        Subject subject = new Subject();
        CallbackHandler cbh = new MockCallbackHandler(un, pw.toCharArray());
        module.initialize(subject, cbh, sharedState, options);
        Long userId = createUser(un, pw);
        assertTrue(module.login());
        assertTrue(!sharedState.isEmpty());

        sharedState.clear();
        updatePasswordExpirationDate(userId, null);
        assertTrue(module.login());
        assertTrue(!sharedState.isEmpty());

        sharedState.clear();
        updatePasswordExpirationDate(userId, DateUtils.addDays(new Date(), -1));
        module.login();
    }

    private Long createUser(String un, String pw) {
        AbstractUser au = new DummyUser();
        au.setEmail("test@example.com");
        au.setUsername(un);
        au.setFirstName("Dummy");
        au.setLastName("User");
        Password password = new Password();
        password.setType(PasswordType.PLAINTEXT);
        password.setValue(pw);
        au.setPassword(password);
        au.setPasswordExpirationDate(DateUtils.addDays(new Date(), 2));
        getCurrentSession().save(au);
        return au.getId();
    }

    private void updatePasswordExpirationDate(Long userId, Date expirationDate) {
        AbstractUser au = (AbstractUser) getCurrentSession().get(DummyUser.class, userId);
        au.setPasswordExpirationDate(expirationDate);
        getCurrentSession().update(au);
        getCurrentSession().flush();
        getCurrentSession().clear();
    }

    public static class MockCallbackHandler implements CallbackHandler {

        private String username;
        private char[] password;
        private Boolean throwIOException;

        public MockCallbackHandler(String username, char[] password) {
            this.username = username;
            this.password = password;
        }

        public MockCallbackHandler(boolean io) {
            throwIOException = io;
        }

        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            if (throwIOException != null) {
                if (throwIOException) {
                    throw new IOException();
                }
                throw new UnsupportedCallbackException(null);
            }

            for (Callback c : callbacks) {
                if (c instanceof NameCallback) {
                    ((NameCallback) c).setName(username);
                }
                if (c instanceof PasswordCallback) {
                    ((PasswordCallback) c).setPassword(password);
                }
            }
        }
    }
}
