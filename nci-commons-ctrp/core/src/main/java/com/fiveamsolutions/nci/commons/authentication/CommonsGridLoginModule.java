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

import gov.nih.nci.security.util.StringEncrypter;
import gov.nih.nci.security.util.StringEncrypter.EncryptionException;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Performs authentication services in the jaas environment. Handles Grid authentication only, using the JBoss password
 * stacking mechanism to be able to authenticate via a standard grid username/password, but get role information from
 * CSM. This class bridges between the Grid world and JBoss.
 * 
 * <pre>
 * This module expects the following module-options:
 * + gridServicePrincipal - representing the principal used to authenticate against JBoss using a shared principal
 * + gridServiceCredential - the password
 * + gridServicePrincipalSeparator - separator to parse the value of gridServicePrincipal from the Grid User Identity.
 * </pre>
 */
public class CommonsGridLoginModule implements LoginModule {

    private static final Logger LOG = Logger.getLogger(CommonsGridLoginModule.class);
    private static String gridServicePrincipal;
    private static String gridServiceCredential;
    private static String gridServicePrincipalSeparator;

    static final String GRID_SERVICE_PRINCIPAL_PROPERTY = "gridServicePrincipal";
    static final String GRID_SERVICE_PRINCIPAL_SEPARATOR_PROPERTY = "gridServicePrincipalSeparator";
    static final String GRID_SERVICE_CREDENTIAL_PROPERTY = "gridServiceCredential";
    
    private static final int GRID_AUTHENTICATION_ACCOUNT_INDEX = 0;
    private static final int GRID_AUTHORIZATION_ACCOUNT_INDEX = 1;

    private Subject subj;
    private CallbackHandler callbackHandler;
    private Map<String, Object> state;
    private boolean loginSuccessful;

    /**
     * @return the gridServicePrincipalSeparator
     */
    public static String getGridServicePrincipalSeparator() {
        return gridServicePrincipalSeparator;
    }

    /**
     * @param gridServicePrincipalSeparator the gridServicePrincipalSeparator to set
     */
    public static void setGridServicePrincipalSeparator(String gridServicePrincipalSeparator) {
        CommonsGridLoginModule.gridServicePrincipalSeparator = gridServicePrincipalSeparator;
    }

    /**
     * Initialize this LoginModule.
     * 
     * @param subject represents the Subject currently being authenticated and is updated with relevant Credentials if
     *            authentication succeeds
     * @param callback - provided as a way to obtain username and password
     * @param sharedState - a Map of data that is shared between login modules
     * @param options - login module options obtained from security-config.xml
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void initialize(Subject subject, CallbackHandler callback, Map sharedState, Map options) {
        this.subj = subject;
        this.callbackHandler = callback;
        this.state = sharedState;

        // Authorize the username and password
        gridServicePrincipal = (String) options.get(GRID_SERVICE_PRINCIPAL_PROPERTY);
        gridServiceCredential = (String) options.get(GRID_SERVICE_CREDENTIAL_PROPERTY);
        gridServicePrincipalSeparator = (String) options.get(GRID_SERVICE_PRINCIPAL_SEPARATOR_PROPERTY);
        if (StringUtils.isEmpty(gridServicePrincipal) || StringUtils.isEmpty(gridServiceCredential)
                || StringUtils.isEmpty(gridServicePrincipalSeparator)) {
            throw new IllegalStateException(
                    "Configuration is missing either grid service principal, credential or principal separator.");
        }
    }

    /**
     * Method to authenticate a Subject (phase 1). This method obtains the login credentials from the callback handle,
     * which obtains the information from the JNDILoginInitialContex, parses the default Grid Service account and Grid
     * User Identity from the username, authenticates the Grid Service account and password and then stores the Grid
     * User Identity in the Login sharedState.
     * 
     * @exception LoginException thrown for callbackHandler errors
     * @return true if successful otherwise false
     */
    public boolean login() throws LoginException {
        LOG.debug("In login");

        loginSuccessful = false;

        CallackHandlerRecorder cbhr = new CallackHandlerRecorder(callbackHandler);
        String password = cbhr.getPassword();

        /* NameCallback (within CallbackHandler) contains Grid Service account and Grid User Identity, separated 
         * by gridServicePrincipalSeparator
         * For example, "ejbclient||parnellt" 
         * (gridServicePrincipal="ejbclient" 
         * gridServicePrincipalSeparator="||" 
         * Grid User Identity="parnellt")
         */
        String[] identityArray = StringUtils.split(cbhr.getIdentities(), gridServicePrincipalSeparator);

        if (identityArray.length != 2) {
            throw new LoginException("Invalid java.naming.security.principal in InitialContext for Grid Login");
        }

        //this represents the grid service username/account/principal used to authenticate to JNDI
        String username = identityArray[GRID_AUTHENTICATION_ACCOUNT_INDEX];
        //this represents the actual grid client user to be added to JBoss's password-stack for future
        //authorization checks
        String gridUserIdentity = identityArray[GRID_AUTHORIZATION_ACCOUNT_INDEX];

        LOG.debug("Username = " + username);
        LOG.debug("Grid Identity = " + gridUserIdentity);

        /*
         * Check whether the grid service's credentials to authenticate to the application's JAAS 
         * using JNDI is correct. If so, TRUST that the gridUserIdentity has already been authenticated 
         * and add this principal to the JBoss's password-stack for later login-module(s) to perform 
         * authorization checks
         */
        if (gridServicePrincipal.equals(username) && getDecryptedPassword().equals(password)) {
            // Set the Grid User Identity as the authenticated username
            // The password stacking configuration will user the grid identity to authorize access to EJBs
            state.put(CommonLoginModule.JBOSS_PASSWORD_STACKING_USER_PARAM,
                    gridUserIdentity.replaceFirst("^.*?/CN=", "").toLowerCase());
            state.put(CommonLoginModule.JBOSS_PASSWORD_STACKING_PASSWORD_PARAM, password);

            loginSuccessful = true;
            LOG.debug("After setting loginSuccessful to true");
        }

        return loginSuccessful;
    }

    private String getDecryptedPassword() throws LoginException {
        try {
            StringEncrypter encrypter = new StringEncrypter();
            return encrypter.decrypt(gridServiceCredential);
        } catch (EncryptionException e) {
            LOG.warn("Unable to encrypt password: " + e.getMessage(), e);
        }
        throw new LoginException("Could not decrypt saved password");
    }

    /**
     * Class to record the callback's username and password values.
     */
    private class CallackHandlerRecorder {
        private final String password;
        private final String identities;
        
        @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
        CallackHandlerRecorder(CallbackHandler callbackHandler) throws LoginException {
            try {
                Callback[] callbacks = new Callback[2];
                callbacks[0] = new NameCallback("userid: ");
                callbacks[1] = new PasswordCallback("password: ", false);

                callbackHandler.handle(callbacks);
                identities = ((NameCallback) callbacks[0]).getName();
                char[] tmpPassword = ((PasswordCallback) callbacks[1]).getPassword();
                password = new String(tmpPassword == null ? new char[0] : tmpPassword);
                ((PasswordCallback) callbacks[1]).clearPassword();

                LOG.debug("Identities = " + identities);
                return;
            } catch (Exception e) {
                LOG.warn("Could not create CallBackHolder: " + e.getMessage(), e);
            }

            throw new LoginException("Could not create CallackHandlerRecorder");
        }

        /**
         * @return password
         */
        public String getPassword() {
            return password;
        }

        /**
         * @return gridPrincipal + separator + grid user identity
         */
        public String getIdentities() {
            return identities;
        }
    }

    /**
     * @return Method to authenticate a Subject (phase 2). This method will abort the login process if phase 1 of the
     * authentication has failed. This failure is identified using the loginSuccessful variable.
     * @exception LoginException if any errors
     */
    public boolean abort() throws LoginException {
        return !loginSuccessful;
    }

    /**
     * @return Method to authenticate a Subject (phase 2). This method will commit the login process if phase 1 of the
     * authentication has succeeded. Login success is identified using the loginSuccessful variable.
     * @exception LoginException if any errors
     */
    public boolean commit() throws LoginException {
        return loginSuccessful;
    }

    /**
     * @return This method removes/destroys credentials associated with the Subject during the commit operation
     * @exception LoginException if any errors
     */
    public boolean logout() throws LoginException {
        if (subj != null && subj.getPrincipals() != null) {
            subj.getPrincipals().clear();
        }
        loginSuccessful = false;
        return true;
    }
}
