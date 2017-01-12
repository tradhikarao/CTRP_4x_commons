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

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * A LoginModule that prevents Session Fixation vulnerability by generating a new session, with a new id, and migrating
 * session attributes from the previous session to the new session, on login.
 *
 * <p>
 * When used in JBoss, you must also configure it to not reuse the old session id when a new session is created. this is
 * done on JBoss by setting <code>document('JBOSS_HOME/server/default/deploy/jbossweb-tomcat55.sar/server.xml')
 * /Server/Service/Connector/@emptySessionPath</code> to <code>false</code>
 *
 * <p>
 * This LoginModule should be placed as the first one in a login configuration.
 * Example JBoss security-config.xml:
 * <code>
 * &lt;policy>
 *   &lt;application-policy name="caarray">
 *       &lt;authentication>
 * ...
 *          &lt;login-module code="gov.nih.nci.caarray.security.SessionFixationLoginModule" flag="required">
 *              &lt;!--module-option name="migrateSessionAttributes">true&lt;/module-option-->
 *          &lt;/login-module>
 * ...
 *</code>
 *
 * For complete protection from Session Fixation attacks, this should be used in conjunction with the
 * SessionFixationProtectionFilter.
 *
 * @author gax
 */
@SuppressWarnings({ "PMD.AvoidThrowingRawExceptionTypes", "PMD.PreserveStackTrace" })
public class SessionFixationProtectionLoginModule implements LoginModule {

    private static final String EJB_INVOCATION_CHAIN_MARKER = "org.jboss.ejb3.security.Ejb3AuthenticationInterceptor";

    private static final Logger LOG = Logger.getLogger(SessionFixationProtectionLoginModule.class);

    private static final String WEB_REQUEST_KEY = "javax.servlet.http.HttpServletRequest";
    
    /**
     * Option name for whether to migrate session attributes.
     */
    public static final String MIGRATE_SESSION_OPTION = "migrateSessionAttributes";
    
    private static final String MIGRATED_SESSION_KEY = SessionFixationProtectionLoginModule.class.getName()
            + ".migrated";
    private static final Field SESSION_FIELD;
    private static final Field NOTES_FIELD;

    static {

        try {
            SESSION_FIELD = org.apache.catalina.session.StandardSessionFacade.class.getDeclaredField("session");
            NOTES_FIELD = org.apache.catalina.session.StandardSession.class.getDeclaredField("notes");
            SESSION_FIELD.setAccessible(true);
            NOTES_FIELD.setAccessible(true);
        } catch (Exception ex) {
            LOG.error("configuration error", ex);
            throw new Error(ex);
        }
    }


    private boolean migrateSessionAttributes = true;
    
    /**
     * Check if session attributes should be copied into the new session (default is true).
     * @return true when attribute migration is enabled.
     */
    public boolean isMigrateSessionAttributes() {
        return migrateSessionAttributes;
    }

    /**
     * Enable / disable session attribute migration (default is true).
     * @param migrateSessionAttributes migrateSessionAttributes
     */
    public void setMigrateSessionAttributes(boolean migrateSessionAttributes) {
        this.migrateSessionAttributes = migrateSessionAttributes;
    }

    /**
     * {@inheritDoc}
     */
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
            Map<String, ?> options) {

        if (options.containsKey(MIGRATE_SESSION_OPTION)) {
            migrateSessionAttributes = Boolean.parseBoolean(options.get(MIGRATE_SESSION_OPTION).toString());
        }           
    }

    /**
     * {@inheritDoc}
     */
    public boolean login() throws LoginException {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean commit() throws LoginException {
        try {
            HttpServletRequest request = (HttpServletRequest) PolicyContext.getContext(WEB_REQUEST_KEY);
            if (request != null) {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    boolean alreadyMigrated = Boolean.TRUE.equals(session
                            .getAttribute(MIGRATED_SESSION_KEY));
                    boolean withinEjbInvocationContext = isWithinEjbInvocationContext();
                    if (!alreadyMigrated && !withinEjbInvocationContext) { // NOPMD
                        migrateSession(session, request);
                    }
                }
            }
            return true;
        } catch (PolicyContextException ex) {
            LOG.error("failed to get request", ex);
            throw new LoginException(ex.toString());
        }
    }

    /**
     * See https://tracker.nci.nih.gov/browse/PO-4773. This login module has
     * been responsible for a hard-to-reproduce bug described in the
     * aforementioned JIRA item. Long story short, this login module was invoked
     * within EJB3 invocation chain (see logs attached to the JIRA item) and was
     * invalidating the session. As a result, the user would get a error page
     * and kicked out of the application. I haven't been able to determine exact
     * condition that would reproduce the problem, other than that it would
     * happen when one EJB called another EJB. <br/>
     * <br>
     * This method will indicate whether or not this login module is being
     * called within an EJB invocation chain. <b>No portability.</b> The method
     * is JBoss-specific, although no compile- or run-time dependencies on
     * JBoss' classes are present.
     * 
     * @return
     * @see https://tracker.nci.nih.gov/browse/PO-4773
     * @author Denis G. Krylov
     */
    boolean isWithinEjbInvocationContext() {
        Throwable t = getThrowableWithStackTrace();
        for (StackTraceElement element : t.getStackTrace()) {
            if (EJB_INVOCATION_CHAIN_MARKER.equals(element.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return
     */
    Throwable getThrowableWithStackTrace() {
        Throwable t = new Throwable();
        t.fillInStackTrace();
        return t;
    }

    /**
     * {@inheritDoc}
     */
    public boolean abort() throws LoginException {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean logout() throws LoginException {
        return true;
    }

    private HttpSession migrateSession(HttpSession oldSession, HttpServletRequest request) throws LoginException {
        String oldId = oldSession.getId();
        Map<String, Object> attributes = new HashMap<String, Object>();
        if (migrateSessionAttributes) {
            @SuppressWarnings("unchecked")
            Enumeration<String> en = oldSession.getAttributeNames();
            while (en.hasMoreElements()) {
                String n = en.nextElement();
                attributes.put(n, oldSession.getAttribute(n));
            }
        }

        oldSession.invalidate();
        HttpSession newSession = request.getSession(true);
        if (oldId.equals(newSession.getId())) {
            throw new LoginException(
                    "Failed to renew session Id. (Set emptySessionPath='false' in Tomcat's server.xml)");
        }
        for (Map.Entry<String, Object> e : attributes.entrySet()) {
            newSession.setAttribute(e.getKey(), e.getValue());
        }
        newSession.setAttribute(MIGRATED_SESSION_KEY, Boolean.TRUE);
        migrateSessionNotes(oldSession, newSession);

        return newSession;
    }
    
    private void migrateSessionNotes(HttpSession oldSessionFacade, HttpSession newSessionFacade) throws LoginException {
        try {
            Object oldSession = SESSION_FIELD.get(oldSessionFacade);
            Object newSession = SESSION_FIELD.get(newSessionFacade);

            @SuppressWarnings("unchecked")
            Map<String, Object> oldNotes = (Map<String, Object>) NOTES_FIELD.get(oldSession);
            @SuppressWarnings("unchecked")
            Map<String, Object> newNotes = (Map<String, Object>) NOTES_FIELD.get(newSession);

            newNotes.putAll(oldNotes);
        } catch (Exception ex) {
            LOG.error("failed to migrate session notes", ex);
            throw new LoginException(ex.toString());
        }
    }
}
