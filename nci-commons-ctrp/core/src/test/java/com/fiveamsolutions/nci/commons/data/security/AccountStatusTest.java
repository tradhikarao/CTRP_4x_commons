package com.fiveamsolutions.nci.commons.data.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Account status unit tests.
 * 
 * @author jstephens
 */
public class AccountStatusTest {

    /**
     * Allows active to active transition.
     */
    @Test
    public void allowsActiveToActiveTransition() {
        assertTrue(AccountStatus.ACTIVE.allowsTransition(AccountStatus.ACTIVE));
    }
    
    /**
     * Allows active to inactive transition.
     */
    @Test
    public void allowsActiveToInactiveTransition() {
        assertTrue(AccountStatus.ACTIVE.allowsTransition(AccountStatus.INACTIVE));
    }
    
    /**
     * Allows inactive to inactive transition.
     */
    @Test
    public void allowsInactiveToInactiveTransition() {
        assertTrue(AccountStatus.INACTIVE.allowsTransition(AccountStatus.INACTIVE));
    }
    
    /**
     * Allows inactive to active transition.
     */
    @Test
    public void allowsInactiveToActiveTransition() {
        assertTrue(AccountStatus.INACTIVE.allowsTransition(AccountStatus.ACTIVE));
    }
    
    /**
     *  Allows pending to pending transition.
     */
    @Test
    public void allowsPendingToPendingTransition() {
        assertTrue(AccountStatus.PENDING.allowsTransition(AccountStatus.PENDING));
    }
    
    /**
     *  Allows pending to active transition.
     */
    @Test
    public void allowsPendingToActiveTransition() {
        assertTrue(AccountStatus.PENDING.allowsTransition(AccountStatus.ACTIVE));
    }
    
    /**
     *  Allows pending to inactive transition.
     */
    @Test
    public void allowsPendingToInactiveTransition() {
        assertTrue(AccountStatus.PENDING.allowsTransition(AccountStatus.INACTIVE));
    }
    
    /**
     * Does not allow active to pending transition.
     */
    @Test
    public void doesNotAllowActiveToPendingTransition() {
        assertFalse(AccountStatus.ACTIVE.allowsTransition(AccountStatus.PENDING));
    }
    
    /**
     * Does not allow inactive to pending transition.
     */
    @Test
    public void doesNotAllowInactiveToPendingTransition() {
        assertFalse(AccountStatus.INACTIVE.allowsTransition(AccountStatus.PENDING));
    }
    
}
