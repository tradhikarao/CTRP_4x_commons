package gov.nih.nci.coppa.services.grid.faults;

import java.util.Calendar;

import org.apache.axis.Constants;
import org.apache.axis.types.URI;
import org.apache.axis.utils.JavaUtils;
import org.globus.util.I18n;
import org.globus.wsrf.utils.AnyHelper;
import org.globus.wsrf.utils.Resources;
import org.oasis.wsrf.faults.BaseFaultType;
import org.oasis.wsrf.faults.BaseFaultTypeDescription;
import org.oasis.wsrf.faults.BaseFaultTypeErrorCode;
import org.w3c.dom.Element;

/**
 * THIS CLASS WAS TAKEN DIRECTLY FROM caGrid v1.2 and modified to allow for easy BaseFaultType population; see toFault
 * method.
 *
 * This class provides convenience functions around BaseFault API. It also provides a common way of including stack
 * traces with Faults. A stack trace of a Fault is added as a chained BaseFault with an error code dialect attribute set
 * to {@link #STACK_TRACE STACK_TRACE}. A regular Java exception is automatically converted into a BaseFault with the
 * description of exception message and with a chained BaseFault with {@link #STACK_TRACE STACK_TRACE} error code
 * dialect.
 *
 * @author smatyas
 */
@SuppressWarnings("PMD")
public final class CoppaFaultHelper {

    /**
     * Stack trace error code URI.
     */
    public static final URI STACK_TRACE;

    /**
     * Exception error code URI.
     */
    public static final URI EXCEPTION;

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    static {
        try {
            STACK_TRACE = new URI("http://www.globus.org/fault/stacktrace");
            EXCEPTION = new URI("http://www.globus.org/fault/exception");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private final BaseFaultType fault;

    /**
     * Creates <code>FaultHelper</code> with a fault. If the fault contains a stack trace it will be automatically
     * converted into a chained BaseFault with an error code dialect attribute set to set to {@link #STACK_TRACE
     * STACK_TRACE}.
     *
     * @param fault fault
     */
    public CoppaFaultHelper(BaseFaultType fault) {
        this(fault, true);
    }

    /**
     * Creates <code>FaultHelper</code> with a fault.
     *
     * @param fault fault
     * @param convertStackTrace if true and if the fault contains a stack trace it will be automatically converted into
     *            a chained BaseFault with an error code dialect attribute set to set to {@link #STACK_TRACE
     *            STACK_TRACE}.
     */
    public CoppaFaultHelper(BaseFaultType fault, boolean convertStackTrace) {
        if (fault == null) {
            throw new IllegalArgumentException(i18n.getMessage("nullArgument", "fault"));
        }
        this.fault = fault;
        if (convertStackTrace) {
            addStackTraceFault();
        }
        // add timestamp automatically if not set
        if (this.fault.getTimestamp() == null) {
            this.fault.setTimestamp(Calendar.getInstance());
        }
    }

    /**
     * Sets the description of the fault.
     *
     * @param description the new description of the fault.
     */
    public void setDescription(String description) {
        setDescription((description == null) ? null : new String[] {description});
    }

    /**
     * Sets the description of the fault.
     *
     * @param description the new descriptions of the fault.
     */
    public void setDescription(String[] description) {
        BaseFaultTypeDescription[] desc = null;
        if (description != null) {
            desc = new BaseFaultTypeDescription[description.length];
            for (int i = 0; i < description.length; i++) {
                desc[i] = new BaseFaultTypeDescription(description[i]);
            }
        }
        this.fault.setDescription(desc);
    }

    private void addFaultCause(BaseFaultType faultCause) {
        BaseFaultType[] cause = this.fault.getFaultCause();
        BaseFaultType[] newCause = null;
        if (cause == null) {
            newCause = new BaseFaultType[1];
        } else {
            newCause = new BaseFaultType[cause.length + 1];
            System.arraycopy(cause, 0, newCause, 0, cause.length);
        }
        newCause[newCause.length - 1] = faultCause;
        this.fault.setFaultCause(newCause);
    }

    private void addStackTraceFault() {
        // check if stack trace fault is already added
        Element stackElement = this.fault.lookupFaultDetail(Constants.QNAME_FAULTDETAIL_STACKTRACE);
        if (stackElement == null) {
            return;
        }
        // remove SOAP details stack entry
        this.fault.removeFaultDetail(Constants.QNAME_FAULTDETAIL_STACKTRACE);

        String message = this.fault.getClass().getName();
        String stackTrace = stackElement.getFirstChild().getNodeValue();

        // add stack trace fault
        addFaultCause(createStackFault(message, stackTrace));
    }

    private void addStackTraceFault(Throwable exception) {
        String message = exception.getClass().getName();
        String stackTrace = JavaUtils.stackToString(exception);

        // add stack trace fault
        addFaultCause(createStackFault(message, stackTrace));
    }

    private static BaseFaultType createStackFault(String message, String stackTrace) {
        BaseFaultType stackFault = new BaseFaultType();
        BaseFaultTypeErrorCode errorCode = new BaseFaultTypeErrorCode();
        errorCode.setDialect(STACK_TRACE);
        errorCode.set_any(AnyHelper.toText(stackTrace));
        stackFault.setErrorCode(errorCode);

        if (message != null && message.length() > 0) {
            BaseFaultTypeDescription[] desc = new BaseFaultTypeDescription[1];
            desc[0] = new BaseFaultTypeDescription(message);
            stackFault.setDescription(desc);
        }

        stackFault.setTimestamp(Calendar.getInstance());

        return stackFault;
    }

    /**
     * @param <E> type of fault
     * @param fault the actual fault, non-null value
     * @param exception the actual exception to convert to a type <E>
     * @return a populated <E> fault setting all BaseFaultType values
     */
    public static <E extends BaseFaultType> E toFault(E fault, Throwable exception) {
        CoppaFaultHelper helper = new CoppaFaultHelper(fault, false);
        helper.setDescription(exception.getMessage());
        helper.addStackTraceFault(exception);
        return fault;
    }

}
