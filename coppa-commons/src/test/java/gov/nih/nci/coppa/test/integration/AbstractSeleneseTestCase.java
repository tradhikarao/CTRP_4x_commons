/**
 * The software subject to this notice and license includes both human readable
 * source code form and machine readable, binary, object code form. The coppa-commons
 * Software was developed in conjunction with the National Cancer Institute
 * (NCI) by NCI employees and 5AM Solutions, Inc. (5AM). To the extent
 * government employees are authors, any rights in such works shall be subject
 * to Title 17 of the United States Code, section 105.
 *
 * This coppa-commons Software License (the License) is between NCI and You. You (or
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
 * its rights in the coppa-commons Software to (i) use, install, access, operate,
 * execute, copy, modify, translate, market, publicly display, publicly perform,
 * and prepare derivative works of the coppa-commons Software; (ii) distribute and
 * have distributed to and by third parties the coppa-commons Software and any
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
package gov.nih.nci.coppa.test.integration;

import org.junit.Before;

import com.thoughtworks.selenium.SeleneseTestCase;

/**
 * Base selenium class for integration tests. You need to call setSeleniumPort, setServerHostname, setBrowser and
 * setServerPort prior to calling setup.
 *
 * @author Abraham J. Evans-EL <aevansel@5amsolutions.com>
 */
public abstract class AbstractSeleneseTestCase extends SeleneseTestCase {
    private static final int PAGE_TIMEOUT_SECONDS = 180;
    private static final int SECONDS_PER_MINUTE = 1000;
    private int serverPort;
    private String serverHostname;
    private String seleniumPort;
    private String browser;

    @Override
    @Before
    public void setUp() throws Exception {
        System.setProperty("selenium.port", getSeleniumPort());
        String hostname = getServerHostname();
        int port = getServerPort();
        String browserString = getBrowser();
        if (port == 0) {
            super.setUp("http://" + hostname, browserString);
        } else {
            super.setUp("http://" + hostname + ":" + port, browserString);

        }
        selenium.setTimeout(toMillisecondsString(PAGE_TIMEOUT_SECONDS));
    }

    /**
     * Converts seconds to milliseconds.
     * @param seconds the number of seconds to convert
     * @return the number of milliseconds in the give seconds
     */
    protected static String toMillisecondsString(long seconds) {
        return String.valueOf(seconds * SECONDS_PER_MINUTE);
    }

    /**
     * Waits for the page to load.
     */
    protected void waitForPageToLoad() {
        selenium.waitForPageToLoad(toMillisecondsString(PAGE_TIMEOUT_SECONDS));
    }

    /**
     * Waits for the given element to load.
     * @param id the id of the element to wait for
     * @param timeoutSeconds the number seconds to wait for
     */
    protected void waitForElementById(String id, int timeoutSeconds) {
        selenium.waitForCondition("selenium.browserbot.getCurrentWindow().document.getElementById('"
                + id + "');", toMillisecondsString(timeoutSeconds));
    }

    /**
     * Click and wait for a page to load.
     * @param locator the locator of the element to click
     */
    protected void clickAndWait(String locator) {
        selenium.click(locator);
        waitForPageToLoad();
    }

    /**
     * Click and pause to allow for ajax to complete.
     * @param locator the locator of the element to click
     */
    protected void clickAndWaitAjax(String locator) {
        selenium.click(locator);
        //This pause is to allow for any js associated with an anchor to complete execution
        //before moving on.
        pause(SECONDS_PER_MINUTE / 2);
    }

    /**
     * Click the save button and wait for the resulting page to load.
     */
    protected void clickAndWaitSaveButton() {
        clickAndWait("save_button");
    }

    /**
     * @return the serverPort
     */
    public int getServerPort() {
        return this.serverPort;
    }

    /**
     * @param serverPort the serverPort to set
     */
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * @return the seleniumPort
     */
    public String getSeleniumPort() {
        return this.seleniumPort;
    }

    /**
     * @param seleniumPort the seleniumPort to set
     */
    public void setSeleniumPort(String seleniumPort) {
        this.seleniumPort = seleniumPort;
    }

    /**
     * @return the browser
     */
    public String getBrowser() {
        return this.browser;
    }

    /**
     * @param browser the browser to set
     */
    public void setBrowser(String browser) {
        this.browser = browser;
    }

    /**
     * @return the serverHostname
     */
    public String getServerHostname() {
        return this.serverHostname;
    }

    /**
     * @param serverHostname the serverHostname to set
     */
    public void setServerHostname(String serverHostname) {
        this.serverHostname = serverHostname;
    }
}
