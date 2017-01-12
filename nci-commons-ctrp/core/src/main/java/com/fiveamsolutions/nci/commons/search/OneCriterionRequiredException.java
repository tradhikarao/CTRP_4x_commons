package com.fiveamsolutions.nci.commons.search;

/**
 * Thrown by service api methods when a criterion is missing and expected.
 */
public class OneCriterionRequiredException extends SearchCriteriaValidationException {
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor sets the default message.
     */
    public OneCriterionRequiredException() {
        super("At least one criterion must be provided");
    }
}
