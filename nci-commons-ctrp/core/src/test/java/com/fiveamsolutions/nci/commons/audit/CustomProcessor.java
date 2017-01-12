
package com.fiveamsolutions.nci.commons.audit;

import java.util.Date;

/**
 *
 * @author gax
 */
public class CustomProcessor extends DefaultProcessor {

    
    @Override
    protected void processDetail(AuditLogDetail detail, Object oldVal, Object newVal) {
        if (oldVal instanceof Date || newVal instanceof Date) {
            String o = oldVal == null ? null : Long.toString(((Date)oldVal).getTime());
            String n = newVal == null ? null : Long.toString(((Date)newVal).getTime());
            detail.setMessage("date.change.event");
            super.processDetailString(detail, o, n);
        } else {
            super.processDetail(detail, oldVal, newVal);
        }
    }



}
