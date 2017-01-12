package com.fiveamsolutions.nci.commons.kmplot.util;

/**
 * KM plot exception class.
 */
@SuppressWarnings("serial")
public class KMException extends Exception {

    /**
     * Default constructor.
     */
    public KMException() {
        //do nothing.
    }

    /**
     * @param message the message.
     * @param cause the cause.
     */
    public KMException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause the cause.
     */
    public KMException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message the message.
     */
    public KMException(String message) {
        super(message);
    }
}
