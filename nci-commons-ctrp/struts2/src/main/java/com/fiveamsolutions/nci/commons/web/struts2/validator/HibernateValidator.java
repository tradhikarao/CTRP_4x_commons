/**
 * The software subject to this notice and license includes both human readable
 * source code form and machine readable, binary, object code form. The caarray-war
 * Software was developed in conjunction with the National Cancer Institute
 * (NCI) by NCI employees and 5AM Solutions, Inc. (5AM). To the extent
 * government employees are authors, any rights in such works shall be subject
 * to Title 17 of the United States Code, section 105.
 *
 * This caarray-war Software License (the License) is between NCI and You. You (or
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
 * its rights in the caarray-war Software to (i) use, install, access, operate,
 * execute, copy, modify, translate, market, publicly display, publicly perform,
 * and prepare derivative works of the caarray-war Software; (ii) distribute and
 * have distributed to and by third parties the caarray-war Software and any
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
package com.fiveamsolutions.nci.commons.web.struts2.validator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;

import com.fiveamsolutions.nci.commons.util.HibernateHelper;
import com.fiveamsolutions.nci.commons.validator.MultipleCriteriaMessageInterpolator;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

/**
 * Class to provide hibernate validator support in Struts 2.
 *
 * @author Scott Miller
 */
@SuppressWarnings("PMD.TooManyMethods")
public class HibernateValidator extends FieldValidatorSupport {
    private String resourceKeyBase;
    private String conditionalExpression;
    private String includes;
    private Set<String> parsedIncludes;
    private String excludes;
    private Set<String> parsedExcludes;

    /**
     * {@inheritDoc}
     */
    public void validate(Object object) throws ValidationException {
        ValueStack stack = ActionContext.getContext().getValueStack();
        if (StringUtils.isNotBlank(getConditionalExpression())
                && !(Boolean) stack.findValue(getConditionalExpression())) {
            return;
        }
        Object value = getFieldValue(getFieldName(), object);
        stack.push(object);
        if (value instanceof Collection<?>) {
            Collection<?> coll = (Collection<?>) value;
            Object[] array = coll.toArray();
            validateArrayElements(array, getFieldName());
        } else if (value instanceof Object[]) {
            Object[] array = (Object[]) value;
            validateArrayElements(array, getFieldName());
        } else {
            validateObject(getFieldName(), value);
        }
        stack.pop();
    }

    private static String simplifyFieldName(String name) {
        if (name == null) {
            return name;
        }
        int start = name.indexOf('[');
        if (start >= 0) {
            int end = name.indexOf(']', start);
            return name.substring(0, start) + simplifyFieldName(name.substring(end + 1));
        }
        return name;
    }

    private void addFieldError(String fieldName, InvalidValue message) {
        StringBuffer errorField = new StringBuffer(fieldName);
        StringBuffer errorFieldKey = new StringBuffer(fieldName);
        if (StringUtils.isNotBlank(getResourceKeyBase())) {
            errorFieldKey.setLength(0); // clear
            errorFieldKey.append(getResourceKeyBase());
        }
        errorField.append('.').append(message.getPropertyPath());
        errorFieldKey.append('.').append(message.getPropertyPath());
        String msg = StringUtils.replace(message.getMessage(), "(fieldName)",
                getValidatorContext().getText(errorFieldKey.toString(), message.getPropertyPath()));
        String errorFieldName = errorField.toString();
        getValidatorContext().addFieldError(errorFieldName, msg);

    }

    private void convertMessages(String fieldName, InvalidValue[] validationMessages) {
        for (InvalidValue message : validationMessages) {
            convertMessage(fieldName, message);
        }
    }

    private void convertMessage(String fieldName, InvalidValue message) {
        if (message.getMessage().startsWith(MultipleCriteriaMessageInterpolator.HEADER)) {
            convertMultipleCriteriaMessages(fieldName, message.getMessage());
        } else if (isFieldError(message)) {
            convertFieldError(fieldName, message);
        } else {
            getValidatorContext().addActionError(message.getMessage());
        }
    }

    private boolean isFieldError(InvalidValue message) {
        return StringUtils.isNotBlank(message.getPropertyName());
    }

    private void convertFieldError(String fieldName, InvalidValue message) {
        String path = message.getPropertyPath();
        String simpleName = simplifyFieldName(path);
        if ((parsedIncludes == null || parsedIncludes.contains(simpleName))
                && (parsedExcludes == null || !parsedExcludes.contains(simpleName))) {
            addFieldError(fieldName, message);
        }
    }

    private void convertMultipleCriteriaMessages(String fieldName, String combinedMessage) {
        String[] messages = StringUtils.splitByWholeSeparator(combinedMessage,
                MultipleCriteriaMessageInterpolator.MESSAGE_SEPARATOR);
        //start from 1, because the message at index 0 is actually the header
        for (int i = 1; i < messages.length; i++) {
            if (StringUtils.isNotBlank(messages[i])) {
                String[] messageParts = StringUtils.splitByWholeSeparator(messages[i],
                        MultipleCriteriaMessageInterpolator.FIELD_SEPARATOR);
                InvalidValue iv = new InvalidValue(messageParts[1], null, messageParts[0], null, null);
                addFieldError(fieldName, iv);
            }
        }
    }

    private void validateArrayElements(Object[] array, String fieldName) {
        for (int i = 0; i < array.length; i++) {
            Object o = array[i];
            validateObject(fieldName + "[" + i + "]", o);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void validateObject(String fieldName, Object o) {
        if (o != null) {
            ClassValidator classValidator = HibernateHelper.getClassValidator(o);
            InvalidValue[] validationMessages = classValidator.getInvalidValues(o);
            if (validationMessages.length > 0) {
                convertMessages(fieldName, validationMessages);
            }
        }
    }

    /**
     * @return the resourceKeyBase
     */
    public String getResourceKeyBase() {
        return this.resourceKeyBase;
    }

    /**
     * @param resourceKeyBase the resourceKeyBase to set
     */
    public void setResourceKeyBase(String resourceKeyBase) {
        this.resourceKeyBase = resourceKeyBase;
    }

    /**
     * @return the conditionalExpression
     */
    public String getConditionalExpression() {
        return this.conditionalExpression;
    }

    /**
     * @param conditionalExpression the conditionalExpression to set
     */
    public void setConditionalExpression(String conditionalExpression) {
        this.conditionalExpression = conditionalExpression;
    }

    /**
     * @return the includes
     */
    public String getIncludes() {
        return includes;
    }

    /**
     * @param includes A comma seperated list of property names to validate.
     */
    public void setIncludes(String includes) {
        this.includes = includes;
        this.parsedIncludes = parseList(includes);
    }

    /**
     * @return the excludes list
     */
    public String getExcludes() {
        return excludes;
    }

    /**
     * @param excludes A comma seperated list of property names to exclude from validation.
     */
    public void setExcludes(String excludes) {
        this.excludes = excludes;
        this.parsedExcludes = parseList(excludes);
    }

    private static Set<String> parseList(String list) {
        if (StringUtils.isBlank(list)) {
            return null;
        }
        HashSet<String> names = new HashSet<String>();
        StringTokenizer st = new StringTokenizer(list, ",");
        while (st.hasMoreTokens()) {
            String n = st.nextToken().trim();
            if (StringUtils.isNotBlank(n)) {
                names.add(n);
            }
        }
        return !names.isEmpty() ? names : null;
    }

}
