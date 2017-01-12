package gov.nih.nci.coppa.services.grid.faults;

import gov.nih.nci.coppa.services.grid.remote.InvokeCoppaServiceException;

import org.junit.Test;
import org.oasis.wsrf.faults.BaseFaultType;

public class CoppaFaultHelperTest {

    @Test
    public void testPOFaultHelperBaseFaultType() {
        CoppaFaultHelper faultHelper = new CoppaFaultHelper(new BaseFaultType());
    }

    @Test (expected = IllegalArgumentException.class)
    public void testPOFaultHelperBaseFaultType_NullParam() {
        CoppaFaultHelper faultHelper = new CoppaFaultHelper(null);
    }

    @Test
    public void testPOFaultHelperBaseFaultType_desc() {
        CoppaFaultHelper faultHelper = new CoppaFaultHelper(new BaseFaultType());
        faultHelper.setDescription("test string");
        faultHelper.setDescription(new String[] {"test", "string", "as", "array"});
        CoppaFaultHelper.toFault(new BaseFaultType(), new InvokeCoppaServiceException("const"));
        CoppaFaultHelper.toFault(new BaseFaultType(), new InvokeCoppaServiceException(new Exception("const")));
        CoppaFaultHelper.toFault(new BaseFaultType(),
                new InvokeCoppaServiceException("const", new Exception("const")));


    }

}
