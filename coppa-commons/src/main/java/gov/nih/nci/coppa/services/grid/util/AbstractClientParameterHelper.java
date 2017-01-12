/**
 * The software subject to this notice and license includes both human readable
 * source code form and machine readable, binary, object code form. The po-grid
 * Software was developed in conjunction with the National Cancer Institute 
 * (NCI) by NCI employees and 5AM Solutions, Inc. (5AM). To the extent 
 * government employees are authors, any rights in such works shall be subject 
 * to Title 17 of the United States Code, section 105. 
 *
 * This po-grid Software License (the License) is between NCI and You. You (or 
 * Your) shall mean a person or an entity, and all other entities that control, 
 * are controlled by, or are under common control with the entity. Control for 
 * purposes of this definition means (i) the direct or indirect power to cause 
 * the direction or management of such entity, whether by contract or otherwise,
 * or (ii) ownership of fifty percent (50%) or more of the outstanding shares, 
 * or (iii) beneficial ownership of such entity. 
 *
 * This License is granted provided that You agree to the conditions described 
 * below. NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up, 
 * no-charge, irrevocable, transferable and royalty-free right and license in 
 * its rights in the po-grid Software to (i) use, install, access, operate, 
 * execute, copy, modify, translate, market, publicly display, publicly perform,
 * and prepare derivative works of the po-grid Software; (ii) distribute and 
 * have distributed to and by third parties the po-grid Software and any 
 * modifications and derivative works thereof; and (iii) sublicense the 
 * foregoing rights set out in (i) and (ii) to third parties, including the 
 * right to license such rights to further third parties. For sake of clarity, 
 * and not by way of limitation, NCI shall have no right of accounting or right 
 * of payment from You or Your sub-licensees for the rights granted under this 
 * License. This License is granted at no charge to You.
 *
 * Your redistributions of the source code for the Software must retain the 
 * above copyright notice, this list of conditions and the disclaimer and 
 * limitation of liability of Article 6, below. Your redistributions in object 
 * code form must reproduce the above copyright notice, this list of conditions 
 * and the disclaimer of Article 6 in the documentation and/or other materials 
 * provided with the distribution, if any. 
 *
 * Your end-user documentation included with the redistribution, if any, must 
 * include the following acknowledgment: This product includes software 
 * developed by 5AM and the National Cancer Institute. If You do not include 
 * such end-user documentation, You shall include this acknowledgment in the 
 * Software itself, wherever such third-party acknowledgments normally appear.
 *
 * You may not use the names "The National Cancer Institute", "NCI", or "5AM" 
 * to endorse or promote products derived from this Software. This License does 
 * not authorize You to use any trademarks, service marks, trade names, logos or
 * product names of either NCI or 5AM, except as required to comply with the 
 * terms of this License. 
 *
 * For sake of clarity, and not by way of limitation, You may incorporate this 
 * Software into Your proprietary programs and into any third party proprietary 
 * programs. However, if You incorporate the Software into third party 
 * proprietary programs, You agree that You are solely responsible for obtaining
 * any permission from such third parties required to incorporate the Software 
 * into such third party proprietary programs and for informing Your 
 * sub-licensees, including without limitation Your end-users, of their 
 * obligation to secure any required permissions from such third parties before 
 * incorporating the Software into such third party proprietary software 
 * programs. In the event that You fail to obtain such permissions, You agree 
 * to indemnify NCI for any claims against NCI by such third parties, except to 
 * the extent prohibited by law, resulting from Your failure to obtain such 
 * permissions. 
 *
 * For sake of clarity, and not by way of limitation, You may add Your own 
 * copyright statement to Your modifications and to the derivative works, and 
 * You may provide additional or different license terms and conditions in Your 
 * sublicenses of modifications of the Software, or any derivative works of the 
 * Software as a whole, provided Your use, reproduction, and distribution of the
 * Work otherwise complies with the conditions stated in this License.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, 
 * (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, 
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO 
 * EVENT SHALL THE NATIONAL CANCER INSTITUTE, 5AM SOLUTIONS, INC. OR THEIR 
 * AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.nih.nci.coppa.services.grid.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class used to extract runtime parameters. 
 * All clients have -runMethods and -url
 * Each client can define it's own set of other parameters.
 * 
 * Call setLocalArgs first, then call setupParams.
 * 
 * sample ant command might look like this:
 * ant runPatientClient -Dargs="-getId 12 -runMethods testThis,testThat"
 * 
 * @author ludetc
 *
 */
public abstract class AbstractClientParameterHelper {

    private final Map<String, String> params = new HashMap<String, String>();
    private List<String> validValues;
    private final List<Method> runMethods = new ArrayList<Method>();

    /**
     * Call this method first to set up valid parameters.
     * @param localArgs valid values
     */
    public void setLocalArgs(String[] localArgs) {
        validValues = new ArrayList<String>(Arrays.asList(localArgs));
        validValues.add("-url");
        validValues.add("-runMethods");
    }

    /**
     * Setup parameters from arguments.
     * @param args runtime args
     */
    public void setupParams(String[] args) {
        checkArgs(args);
        
        for (int i = 0; i < args.length; i = i + 2) {   
            if (!validValues.contains(args[i])) {
                usage("not a valid argument: " + args[i]);
            }

            if (args[i].equals("-runMethods")) {
                String[] methodNames = args[i + 1].split(",");
                namesToMethods(methodNames);
            }

            params.put(args[i], args[i + 1]);
        }       

        checkMethods();
    }

    private void namesToMethods(String[] methodNames) {
        for (String methodName : methodNames) {
            try {
                Method method = getClientClass().getDeclaredMethod(methodName, getClientClass());
                runMethods.add(method);
            } catch (SecurityException e) {
                throw new IllegalArgumentException("incorrect method name: " + methodName, e);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("incorrect method name: " + methodName, e);
            }
        }
    }

    private void checkMethods() {
        if (runMethods.isEmpty()) {
            Method[] methods = getClientClass().getDeclaredMethods();
            for (Method method : methods) {                
                if (method.getAnnotation(GridTestMethod.class) != null) {
                    runMethods.add(method);
                }
            }
        }
    }
    
    private void checkArgs(String[] args) {
        if (validValues == null) {
            throw new IllegalArgumentException("call setLocalArgs first"); 
        }

        if ((args.length & 1) == 1 || (args.length == 0)) {            
            usage("unexpected number of argument: " + args.length);
        }
    }

    /**
     * return the client class. 
     * 
     * @return runtime class
     */
    protected abstract Class getClientClass();

    /**
     * Return an arg, or pass back ifMissing no value is found.
     * 
     * @param arg name of the argument
     * @param ifMissing value to return if none found
     * @return runtime value
     */
    public String getArgument(String arg, String ifMissing) {
        if (!params.keySet().contains(arg)) {
            return ifMissing;
        }
        return params.get(arg);
    }

    /**
     * return an argument.
     * @param arg name of the argument
     * @return runtime value.
     * @Throw IllegalArgumentDescription if value is found for arg.
     */
    public String getArgument(String arg) {
        if (!params.keySet().contains(arg)) {
            throw new IllegalArgumentException("Missing runtime parameter: " + arg);
        }
        return params.get(arg);
    }

    /**
     * which methods should be run.
     * 
     * @return an array of Method
     */
    public Method[] getRunMethods() {
        return runMethods.toArray(new Method[runMethods.size()]);
    }

    @SuppressWarnings("PMD.SystemPrintln")
    private void usage(String errorMessage) {
        System.out.println(getClientClass().getName() + " usage");
        System.out.println("-url <service url>");
        System.out.println("-runMethods <methodNames> defaults to all method");
        for (String value : validValues) {
            System.out.println(value);
        }
        throw new IllegalArgumentException(errorMessage);
    }

}
