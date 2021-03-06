/**
 * The software subject to this notice and license includes both human readable
 * source code form and machine readable, binary, object code form. The nci-commons
 * Software was developed in conjunction with the National Cancer Institute
 * (NCI) by NCI employees and 5AM Solutions, Inc. (5AM). To the extent
 * government employees are authors, any rights in such works shall be subject
 * to Title 17 of the United States Code, section 105.
 *
 * This nci-commons Software License (the License) is between NCI and You. You (or
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
 * its rights in the nci-commons Software to (i) use, install, access, operate,
 * execute, copy, modify, translate, market, publicly display, publicly perform,
 * and prepare derivative works of the nci-commons Software; (ii) distribute and
 * have distributed to and by third parties the nci-commons Software and any
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
package com.fiveamsolutions.nci.commons.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.junit.Before;
import org.junit.Test;

import com.fiveamsolutions.nci.commons.audit.DummyInvoice;
import com.fiveamsolutions.nci.commons.audit.DummyLineItem;

/**
 * @author smiller
 *
 */
public class HibernateHelperTest {

    private HibernateHelper hh;

    /**
     * test committing a transaction.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testCommit() {
        hh.openAndBindSession();

        List<DummyInvoice> results = hh.getCurrentSession().createQuery("from " + DummyInvoice.class.getName()).list();
        assertEquals(0, results.size());

        Transaction tx = hh.beginTransaction();
        DummyInvoice invoice = createDummyInvoice();
        hh.getCurrentSession().save(invoice);
        tx.commit();

        results = hh.getCurrentSession().createQuery("from " + DummyInvoice.class.getName()).list();
        assertEquals(1, results.size());

        hh.unbindAndCleanupSession();
    }

    private DummyInvoice createDummyInvoice() {
        DummyInvoice invoice = new DummyInvoice();
        invoice.setOrderDate(new Date());
        DummyLineItem li = new DummyLineItem("Dummy Item", 1.0, 1.0);
        invoice.getItems().add(li);
        return invoice;
    }

    /**
     * test rolling back a transaction.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testRollback() {
        hh.openAndBindSession();

        List<DummyInvoice> results = hh.getCurrentSession().createQuery("from " + DummyInvoice.class.getName()).list();
        assertEquals(0, results.size());

        Transaction tx = hh.beginTransaction();
        DummyInvoice invoice = createDummyInvoice();
        hh.getCurrentSession().save(invoice);

        hh.rollbackTransaction(tx);

        results = hh.getCurrentSession().createQuery("from " + DummyInvoice.class.getName()).list();
        assertEquals(0, results.size());

        hh.unbindAndCleanupSession();
    }

    @Test
    public void testBuildInClause() {
        List<String> items = new ArrayList<String>();
        for (int i = 0; i < HibernateHelper.MAX_IN_CLAUSE_LENGTH - 1; i++) {
            items.add("foo");
        }
        Map<String, List<? extends Serializable>> blocks = new HashMap<String, List<? extends Serializable>>();
        String hql = HibernateHelper.buildInClause(items, "bar", blocks);
        assertNotNull(blocks.get("block0"));
        assertNull(blocks.get("block1"));
        assertFalse(hql.contains("or"));

        items = new ArrayList<String>();
        for (int i = 0; i < HibernateHelper.MAX_IN_CLAUSE_LENGTH + 1; i++) {
            items.add("foo");
        }
        blocks = new HashMap<String, List<? extends Serializable>>();
        hql = HibernateHelper.buildInClause(items, "bar", blocks);
        assertNotNull(blocks.get("block0"));
        assertNotNull(blocks.get("block1"));
        assertTrue(hql.contains("or"));
    }

    @Before
    final public void initDbIfNeeded() throws HibernateException {
        initHelper();

        hh.openAndBindSession();

        Transaction tx = hh.beginTransaction();
        SchemaExport se = new SchemaExport(hh.getConfiguration());
        se.drop(false, true);
        se.create(false, true);
        tx.commit();

        hh.unbindAndCleanupSession();
    }

    /**
     * init the helper.
     */
    protected void initHelper() {
        hh = new CustomHibernateHelper();
        hh.initialize();
    }

    /**
     * @return the hh
     */
    public HibernateHelper getHelper() {
        return hh;
    }

    /**
     * @param hh the hh to set
     */
    public void setHelper(HibernateHelper hh) {
        this.hh = hh;
    }

    @Test
    public void testValidate() {
        DummyInvoice invoice = new DummyInvoice();
        hh.setBundleName("ValidatorMessages");
        Map<String, String[]> result = HibernateHelper.validate(invoice);
        assertEquals(1, result.size());
        assertEquals("may not be null or empty", result.get("items")[0]);

        DummyLineItem li = new DummyLineItem();
        result = HibernateHelper.validate(li);
        assertEquals(3, result.size());
        assertEquals("may not be null or empty", result.get("item")[0]);
        assertEquals("must be set", result.get("quantity")[0]);
        assertEquals("must be set", result.get("unitPrice")[0]);

        hh.setBundle(null);
        result = HibernateHelper.validate(li);
        assertEquals(3, result.size());
        assertEquals("may not be null or empty", result.get("item")[0]);
        assertEquals("must be set", result.get("quantity")[0]);
        assertEquals("must be set", result.get("unitPrice")[0]);

        hh.setBundle(new ListResourceBundle() {

            @Override
            protected Object[][] getContents() {
                return new Object[][] {
                    {"validator.notEmpty", "this field may not be empty"},
                    {"validator.notNull", "this field cannot be null"}
                };
            }
        });
        result = HibernateHelper.validate(li);
        assertEquals(3, result.size());
        assertEquals("this field may not be empty", result.get("item")[0]);
        assertEquals("this field cannot be null", result.get("quantity")[0]);
        assertEquals("this field cannot be null", result.get("unitPrice")[0]);

    }
}
