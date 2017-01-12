package com.fiveamsolutions.nci.commons.ejb;

import org.hibernate.transaction.JNDITransactionManagerLookup;

/**
 * A {@link TransactionManagerLookup} lookup strategy for JBoss AS 7.
 * 
 * @author dkrylov
 * 
 */
public class JBossAS7TransactionManagerLookup extends
        JNDITransactionManagerLookup {
    /*
     * (non-Javadoc)
     * 
     * @see org.hibernate.transaction.JNDITransactionManagerLookup#getName()
     */
    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "java:jboss/TransactionManager";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.hibernate.transaction.TransactionManagerLookup#getUserTransactionName
     * ()
     */
    /**
     * {@inheritDoc}
     */
    public String getUserTransactionName() {
        return "java:jboss/UserTransaction";
    }
}
