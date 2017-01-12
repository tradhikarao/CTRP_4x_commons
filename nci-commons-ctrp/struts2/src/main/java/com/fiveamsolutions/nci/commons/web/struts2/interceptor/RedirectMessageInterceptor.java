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
package com.fiveamsolutions.nci.commons.web.struts2.interceptor;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.struts2.dispatcher.ServletActionRedirectResult;
import org.apache.struts2.dispatcher.ServletRedirectResult;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Result;
import com.opensymphony.xwork2.ValidationAware;
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor;

/**
 * An Interceptor to preserve an actions ValidationAware messages across a
 * redirect result.
 *
 * It makes the assumption that you always want to preserve messages across a
 * redirect and restore them to the next action if they exist.
 *
 * The way this works is it looks at the result type after a action has executed
 * and if the result was a redirect (ServletRedirectResult) or a redirectAction
 * (ServletActionRedirectResult) and there were any errors, messages, or
 * fieldErrors they are stored in the session. Before the next action executes
 * it will check if there are any messages stored in the session and add them to
 * the next action.
 *
 * <br>
 * This code was adapted from:
 *   http://glindholm.wordpress.com/2008/07/02/preserving-messages-across-a-redirect-in-struts-2/
 */
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.SignatureDeclareThrowsException", "unchecked" })
public class RedirectMessageInterceptor extends MethodFilterInterceptor {
    private static final long  serialVersionUID    = -1847557437429753540L;

    /**
     * Field errors.
     */
    public static final String FIELD_ERRORS_KEY    = "RedirectMessageInterceptor_FieldErrors";
    /**
     * Action errors.
     */
    public static final String ACTION_ERRORS_KEY   = "RedirectMessageInterceptor_ActionErrors";
    /**
     * Action messages.
     */
    public static final String ACTION_MESSAGES_KEY = "RedirectMessageInterceptor_ActionMessages";

    /**
     * {@inheritDoc}
     */
    @Override
    public String doIntercept(ActionInvocation invocation) throws Exception {
        Object action = invocation.getAction();
        if (action instanceof ValidationAware) {
            before(invocation, (ValidationAware) action);
        }

        String result = invocation.invoke();

        if (action instanceof ValidationAware) {
            after(invocation, (ValidationAware) action);
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    private void before(ActionInvocation invocation, ValidationAware validationAware) throws Exception {
        Map<String, ?> session = invocation.getInvocationContext().getSession();

        Collection<String> actionErrors = (Collection) session.remove(ACTION_ERRORS_KEY);
        if (actionErrors != null) {
            for (String error : actionErrors) {
                validationAware.addActionError(error);
            }
        }

        Collection<String> actionMessages = (Collection) session.remove(ACTION_MESSAGES_KEY);
        if (actionMessages != null) {
            for (String message : actionMessages) {
                validationAware.addActionMessage(message);
            }
        }

        Map<String, Collection<String>> fieldErrors = (Map) session.remove(FIELD_ERRORS_KEY);
        if (fieldErrors != null) {
            for (Map.Entry<String, Collection<String>> fieldError : fieldErrors.entrySet()) {
                for (String message : fieldError.getValue()) {
                    validationAware.addFieldError(fieldError.getKey(), message);
                }
            }
        }
    }

    private void after(ActionInvocation invocation, ValidationAware validationAware) throws Exception {
        Result result = invocation.getResult();

        if (result instanceof ServletRedirectResult || result instanceof ServletActionRedirectResult) {
            Map<String, Object> session = invocation.getInvocationContext().getSession();

            Collection<String> actionErrors = validationAware.getActionErrors();
            if (actionErrors != null) {
                session.put(ACTION_ERRORS_KEY, actionErrors);
            }

            Collection<String> actionMessages = validationAware.getActionMessages();
            if (actionMessages != null) {
                session.put(ACTION_MESSAGES_KEY, actionMessages);
            }

            Map<String, List<String>> fieldErrors = validationAware.getFieldErrors();
            if (fieldErrors != null) {
                session.put(FIELD_ERRORS_KEY, fieldErrors);
            }
        }
    }
}