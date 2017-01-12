package com.fiveamsolutions.nci.commons.search;

/**
 * Validation exceptions for SearchCriteria.
 * @author smatyas
 */
public class SearchCriteriaValidationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * @param msg error message
     */
    public SearchCriteriaValidationException(String msg) {
        super(msg);
    }
}