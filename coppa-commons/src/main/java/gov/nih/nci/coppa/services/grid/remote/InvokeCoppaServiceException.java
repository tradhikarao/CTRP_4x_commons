package gov.nih.nci.coppa.services.grid.remote;

/**
 * Exception thrown if there is a problem calling a remote EJB.
 */
public class InvokeCoppaServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * @param message error message
     */
    public InvokeCoppaServiceException(String message) {
        super(message);
    }

    /**
     * @param cause underlying cause of the exception
     */
    public InvokeCoppaServiceException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message error message
     * @param cause underlying cause of the exception
     */
    public InvokeCoppaServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
