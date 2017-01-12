package com.fiveamsolutions.nci.commons.web.struts2.interceptor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Map;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;

import com.fiveamsolutions.nci.commons.data.security.AbstractUser;
import com.fiveamsolutions.nci.commons.util.MailUtils;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.ExceptionHolder;
import com.opensymphony.xwork2.interceptor.ExceptionMappingInterceptor;

/**
 * An interceptor that sends helpdesk emails in case of an unexpected exception.
 * 
 * @author vsemenov
 *
 */
public abstract class AbstractExceptionMailerInterceptor extends ExceptionMappingInterceptor {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(AbstractExceptionMailerInterceptor.class);
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void publishException(ActionInvocation invocation, ExceptionHolder exceptionHolder) {
        super.publishException(invocation, exceptionHolder);

        String helpDeskEmail = getHelpdeskEmail(invocation);
        Map<String, Object> session = invocation.getInvocationContext().getSession();
        session.put("globalException", exceptionHolder.getException());

        StringWriter sw = new StringWriter();
        exceptionHolder.getException().printStackTrace(new PrintWriter(sw));
        String date = new Date().toString();
        HttpServletRequest request = ServletActionContext.getRequest();
        String user = request.getRemoteUser();
        String url = request.getRequestURL().toString();
        String parameters = getParameterString();
        String[] args = {date, user, url, parameters, sw.toString() };
        String body = MessageFormat.format(getUnhandledErrorTemplateString(invocation), (Object[]) args);
        LOG.error(body);

        AbstractUser u = getUser();
        u.setEmail(helpDeskEmail);
        try {
            MailUtils.sendEmail(u, getEmailTitle(invocation), body, body);
        } catch (MessagingException me) {
            LOG.error(getErrorSendingEmail(invocation), me);
        }
    }
    
    @SuppressWarnings("rawtypes")
    private String getParameterString() {
        HttpServletRequest request = ServletActionContext.getRequest();
        StringBuffer parametersStringBuffer = new StringBuffer();
        for (Object entryObject : request.getParameterMap().entrySet()) {
            Map.Entry entry = (Map.Entry) entryObject;
            parametersStringBuffer.append(entry.getKey());
            parametersStringBuffer.append(" = ");
            parametersStringBuffer.append(StringUtils.join((Object[]) entry.getValue(), ","));
            parametersStringBuffer.append(" \n ");
        }
        return parametersStringBuffer.toString();
    }
    
    /**
     * @return - the abstract user whos email will be used for the unexpected error email.
     */
    public abstract AbstractUser getUser();

    /**
     * @param invocation - the action invocation for accessing project properties.
     * @return - the helpdesk email(does not need to come from the invocation, can be a db field).
     */
    public abstract String getHelpdeskEmail(ActionInvocation invocation);

    /**
     * @param invocation - the action invocation for accessing project properties.
     * @return - the string with wildcards that'll get filled with the stacktrace and other details.
     */
    public abstract String getUnhandledErrorTemplateString(ActionInvocation invocation);

    /**
     * @param invocation - the action invocation for accessing project properties.
     * @return - the title of the error email, should probably contain the project title.
     */
    public abstract String getEmailTitle(ActionInvocation invocation);

    /**
     * @param invocation - the action invocation for accessing project properties.
     * @return - the text to log if there is a problem sending the unhandled exception email.
     */
    public abstract String getErrorSendingEmail(ActionInvocation invocation);
}
