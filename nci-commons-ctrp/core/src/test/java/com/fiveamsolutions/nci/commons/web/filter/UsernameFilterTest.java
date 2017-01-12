/**
 * The software subject to this notice and license includes both human readable
 * source code form and machine readable, binary, object code form. The nci-commons-core
 * Software was developed in conjunction with the National Cancer Institute
 * (NCI) by NCI employees and 5AM Solutions, Inc. (5AM). To the extent
 * government employees are authors, any rights in such works shall be subject
 * to Title 17 of the United States Code, section 105.
 *
 * This nci-commons-core Software License (the License) is between NCI and You. You (or
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
 * its rights in the nci-commons-core Software to (i) use, install, access, operate,
 * execute, copy, modify, translate, market, publicly display, publicly perform,
 * and prepare derivative works of the nci-commons-core Software; (ii) distribute and
 * have distributed to and by third parties the nci-commons-core Software and any
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
package com.fiveamsolutions.nci.commons.web.filter;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.fiveamsolutions.nci.commons.util.UsernameHolder;

/**
 * Tests the username filter.
 */
public class UsernameFilterTest {

    /**
     * Tests the filter without setting the init param.
     * @throws Exception on error
     */
    @Test
    public void testFilterNoParam() throws Exception {
        final MockFilterConfig filterConfig = new MockFilterConfig();
        runTest(filterConfig, false);
    }

    /**
     * Tests the filter with the init param set to true.
     * @throws Exception on error
     */
    @Test
    public void testFilterCaseSensitive() throws Exception {
        final MockFilterConfig filterConfig = new MockFilterConfig();
        filterConfig.addInitParameter("caseSensitive", "true");
        runTest(filterConfig, true);
    }

    /**
     * Tests the filter with the init param set to false.
     * @throws Exception on error
     */
    @Test
    public void testFilterCaseInsensitive() throws Exception {
        final MockFilterConfig filterConfig = new MockFilterConfig();
        filterConfig.addInitParameter("caseSensitive", "false");
        runTest(filterConfig, false);
    }

    private void runTest(final MockFilterConfig filterConfig, final boolean caseSensitive) throws IOException,
            ServletException {
        assertEquals(UsernameHolder.ANONYMOUS_USERNAME, UsernameHolder.getUser());
        UsernameFilter uf = new UsernameFilter();
        uf.init(filterConfig);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();
        uf.doFilter(request, response, chain);
        assertEquals(UsernameHolder.ANONYMOUS_USERNAME, UsernameHolder.getUser());

        request.setRemoteUser("test");
        chain = new FilterChain() {
            @Override
            public void doFilter(ServletRequest arg0, ServletResponse arg1)
                    throws IOException, ServletException {
                assertEquals("test", UsernameHolder.getUser());
            }
        };
        uf.doFilter(request, response, chain);
        assertEquals(UsernameHolder.ANONYMOUS_USERNAME, UsernameHolder.getUser());

        request.setRemoteUser("TEST");
        chain = new FilterChain() {            
            @Override
            public void doFilter(ServletRequest arg0, ServletResponse arg1)
                    throws IOException, ServletException {
                if (caseSensitive) {
                    assertEquals("TEST", UsernameHolder.getUser());
                } else {
                    assertEquals("test", UsernameHolder.getUser());
                }
            }
        };
        uf.doFilter(request, response, chain);
        assertEquals(UsernameHolder.ANONYMOUS_USERNAME, UsernameHolder.getUser());        

        request.setRemoteUser(null);
        chain = new FilterChain() {            
            @Override
            public void doFilter(ServletRequest arg0, ServletResponse arg1)
                    throws IOException, ServletException {
                assertEquals(UsernameHolder.ANONYMOUS_USERNAME, UsernameHolder.getUser());
            }
        };
        uf.doFilter(request, response, chain);
        assertEquals(UsernameHolder.ANONYMOUS_USERNAME, UsernameHolder.getUser());

        request.setRemoteUser(UsernameHolder.ANONYMOUS_USERNAME);
        chain = new FilterChain() {            
            @Override
            public void doFilter(ServletRequest arg0, ServletResponse arg1)
                    throws IOException, ServletException {
                assertEquals(UsernameHolder.ANONYMOUS_USERNAME, UsernameHolder.getUser());
            }
        };
        uf.doFilter(request, response, chain);
        assertEquals(UsernameHolder.ANONYMOUS_USERNAME, UsernameHolder.getUser());
        uf.destroy();
    }

}