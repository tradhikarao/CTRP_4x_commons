/**
 * NOTICE: This software  source code and any of  its derivatives are the
 * confidential  and  proprietary   information  of  5AM Solutions, Inc.,
 * Inc. (such source  and its derivatives are hereinafter  referred to as
 * "Confidential Information"). The  Confidential Information is intended
 * to be  used exclusively by  individuals or entities that  have entered
 * into either  a non-disclosure agreement or license  agreement (or both
 * of   these  agreements,   if  applicable)  with  5AM  Solutions,  Inc.
 * ("5AM")   regarding   the  use   of  the   Confidential   Information.
 * Furthermore,  the  Confidential  Information  shall be  used  only  in
 * accordance  with   the  terms   of  such  license   or  non-disclosure
 * agreements.   All  parties using  the  Confidential Information  shall
 * verify that their  intended use of the Confidential  Information is in
 * compliance  with and  not in  violation of  any applicable  license or
 * non-disclosure  agreements.  Unless expressly  authorized  by  5AM  in
 * writing, the Confidential Information  shall not be printed, retained,
 * copied, or  otherwise disseminated,  in part or  whole.  Additionally,
 * any party using the Confidential  Information shall be held liable for
 * any and  all damages incurred  by  5AM  due  to any disclosure  of the
 * Confidential  Information (including  accidental disclosure).   In the
 * event that  the applicable  non-disclosure or license  agreements with
 * 5AM  have  expired, or  if  none  currently  exists,  all  copies   of
 * Confidential Information in your  possession, whether in electronic or
 * printed  form, shall be  destroyed  or  returned to  5AM  immediately.
 * 5AM  makes  no  representations  or warranties  hereby regarding   the
 * suitability  of  the   Confidential  Information,  either  express  or
 * implied,  including  but not  limited  to  the  implied warranties  of
 * merchantability,    fitness    for    a   particular    purpose,    or
 * non-infringement. 5AM  shall not be liable for  any  damages  suffered
 * by  licensee as  a result  of  using, modifying  or distributing  this
 * Confidential Information.  Please email [info@5amsolutions.com]   with
 * any questions regarding the use of the Confidential Information.
 */
package com.fiveamsolutions.nci.commons.web.listener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.servlet.ServletContextEvent;

import org.junit.Test;
import org.springframework.mock.web.MockServletContext;

import com.fiveamsolutions.nci.commons.util.MailUtils;

/**
 * @author ddasgupta
 *
 */
public class MailEnabledContextListenerTest {

    @Test
    public void testListener() {
        MailEnabledContextListener mecl = new MailEnabledContextListener();
        mecl.contextInitialized(getEvent("true", "from@example.com"));
        assertTrue(MailUtils.isMailEnabled());
        assertEquals("from@example.com", MailUtils.getFromAddress());
        mecl.contextInitialized(getEvent("false", ""));
        assertFalse(MailUtils.isMailEnabled());
        assertEquals("noreply@5amsolutions.com", MailUtils.getFromAddress());
        mecl.contextDestroyed(getEvent("true", ""));
    }

    private ServletContextEvent getEvent(String mailEnabled, String fromAddress) {
        MockServletContext context = new MockServletContext();
        context.addInitParameter("mailEnabled", mailEnabled);
        context.addInitParameter("fromAddress", fromAddress);
        return new ServletContextEvent(context);
    }
}
