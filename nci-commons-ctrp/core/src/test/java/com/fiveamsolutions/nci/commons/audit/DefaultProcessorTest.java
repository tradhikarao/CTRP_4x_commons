/**
 * The software subject to this notice and license includes both human readable
 * source code form and machine readable, binary, object code form. The po
 * Software was developed in conjunction with the National Cancer Institute
 * (NCI) by NCI employees and 5AM Solutions, Inc. (5AM). To the extent
 * government employees are authors, any rights in such works shall be subject
 * to Title 17 of the United States Code, section 105.
 *
 * This po Software License (the License) is between NCI and You. You (or
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
 * its rights in the po Software to (i) use, install, access, operate,
 * execute, copy, modify, translate, market, publicly display, publicly perform,
 * and prepare derivative works of the po Software; (ii) distribute and
 * have distributed to and by third parties the po Software and any
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


package com.fiveamsolutions.nci.commons.audit;

import com.fiveamsolutions.nci.commons.util.HibernateHelper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author gax
 */
public class DefaultProcessorTest {

    /**
     * Test of isAuditableEntity method, of class DefaultProcessor.
     */
    @Test
    public void testIsAuditableEntity() {
        DefaultProcessor instance = new DefaultProcessor();
        // NULL
        Object o = null;        
        boolean result = instance.isAuditableEntity(o);
        assertEquals(false, result);

        // non-Auditable.
        o = "foo";
        result = instance.isAuditableEntity(o);
        assertEquals(false, result);

        // non-Auditable.
        o = new Auditable() {
            private static final long serialVersionUID = 1L;

            public Long getId() {
                return 1L;
            }
        };
        result = instance.isAuditableEntity(o);
        assertEquals(true, result);
    }

    /**
     * Test of isAuditableClass method, of class DefaultProcessor.
     */
    @Test
    public void testIsAuditableClass() {
        DefaultProcessor instance = new DefaultProcessor();

        @SuppressWarnings("rawtypes")
        Class clazz = null;        
        try {
            instance.isAuditableClass(clazz);
            fail("null check not needed");
        } catch (NullPointerException e) {
            // expected
        }

        clazz = Integer.class;
        boolean result = instance.isAuditableClass(clazz);
        assertEquals(false, result);
        
        clazz = Auditable.class;
        result = instance.isAuditableClass(clazz);
        assertEquals(true, result);

        abstract class SubAuditable implements Auditable {
            private static final long serialVersionUID = 1L;
        };
        clazz = SubAuditable.class;
        result = instance.isAuditableClass(clazz);
        assertEquals(true, result);
    }

    /**
     * Test of newAuditLogRecord method, of class DefaultProcessor.
     */
    @Test
    public void testNewAuditLogRecord() {
        Object entity = null;
        Serializable key = 1L;
        AuditType type = null;
        String entityName = "";
        String tableName = "";
        String username = "";
        DefaultProcessor instance = new DefaultProcessor();
        AuditLogRecord result = instance.newAuditLogRecord(entity, key, type, entityName, tableName, username);
        assertEquals(AuditLogRecord.class, result.getClass());        
    }

    /**
     * Test of processDetail method, of class DefaultProcessor.
     */
    @Test
    public void testProcessDetail_7args() {
        DefaultProcessor instance = new DefaultProcessor();
        
        @SuppressWarnings("deprecation")
        AuditLogRecord record = new AuditLogRecord();
        Object entity = null;
        Serializable key = null;
        String property = "";
        String columnName = "";
        Object oldVal = null;
        Object newVal = "foo";
        instance.processDetail(record, entity, key, property, columnName, oldVal, newVal);
        assertEquals(1, record.getDetails().size());
        AuditLogDetail d = record.getDetails().iterator().next();
        assertNull(d.getOldValue());
        assertEquals("foo", newVal);
    }

    /**
     * Test of processDetail method, of class DefaultProcessor.
     */
    @Test
    public void testProcessDetail_3args() {
        DefaultProcessor instance = new DefaultProcessor();
        instance.setHibernateHelper(new HibernateHelper());
        instance.getHibernateHelper().initialize();
        @SuppressWarnings("deprecation")
        AuditLogDetail detail = new AuditLogDetail();
        Object oldVal = 1L;
        Object newVal = 2L;        
        instance.processDetail(detail, oldVal, newVal);
        assertEquals("1", detail.getOldValue());
        assertEquals("2", detail.getNewValue());
    }

    /**
     * Test of processDetailCollection method, of class DefaultProcessor.
     */
    @Test
    public void testProcessDetailCollection() {
        DefaultProcessor instance = new DefaultProcessor();
        instance.setHibernateHelper(new HibernateHelper());
        instance.getHibernateHelper().initialize();

        // collection of plain objects
        @SuppressWarnings("deprecation")
        AuditLogDetail detail = new AuditLogDetail();
        Collection<?> oldVal = null;
        Collection<?> newVal = Arrays.asList("foo",  null, "bar,", "");
        instance.processDetailCollection(detail, oldVal, newVal);
        assertNull(detail.getOldValue());
        assertEquals("foo,,bar\\,,\"\"" , detail.getNewValue());
        

        // collection of persistent objects
        ArrayList<DummyInvoice> c = new ArrayList<DummyInvoice>(5);
        for (int i = 0; i < 5; i++) {
            DummyInvoice d = new DummyInvoice();
            d.setId(new Long(i));
            c.add(d);
        }
        oldVal = c;
        newVal = null;
        instance.processDetailCollection(detail, oldVal, newVal);
        assertEquals("0,1,2,3,4" , detail.getOldValue());
        assertNull(detail.getNewValue());
    }

    /**
     * Test of processDetailMap method, of class DefaultProcessor.
     */
    @Test
    public void testProcessDetailMap() {

        DefaultProcessor instance = new DefaultProcessor();
        instance.setHibernateHelper(new HibernateHelper());
        instance.getHibernateHelper().initialize();
        
        // map of plain objects
        Map<String, String> m = new LinkedHashMap<String, String>();
        StringBuffer expected = new StringBuffer();
        m.put("aa", "1");expected.append("(aa,1)");
        m.put("bb", "2");expected.append(",(bb,2)");
        m.put("cc", "3");expected.append(",(cc,3)");
        m.put("()", "()");expected.append(",(\\(\\),\\(\\))");
        m.put(")(", ")(");expected.append(",(\\)\\(,\\)\\()");
        @SuppressWarnings("deprecation")
        AuditLogDetail detail = new AuditLogDetail();
        Map<?, ?> oldVal = null;
        Map<?, ?> newVal = m;
        instance.processDetailMap(detail, oldVal, newVal);
        assertNull(detail.getOldValue());
        assertEquals(expected.toString() , detail.getNewValue());


        // map of persistent objects
        Map<DummyInvoice, DummyLineItem> c = new LinkedHashMap<DummyInvoice, DummyLineItem>(5);
        for (int i = 0; i < 5; i++) {
            DummyInvoice di = new DummyInvoice();
            di.setId(new Long(i));
            DummyLineItem dl = new DummyLineItem();
            dl.setId(new Long(i + 100));
            c.put(di, dl);
        }
        oldVal = c;
        newVal = null;
        instance.processDetailMap(detail, oldVal, newVal);
        assertEquals("(0,100),(1,101),(2,102),(3,103),(4,104)" , detail.getOldValue());
        assertNull(detail.getNewValue());
    }

    /**
     * Test of processDetailString method, of class DefaultProcessor.
     */
    @Test
    public void testProcessDetailString() {
        @SuppressWarnings("deprecation")
        AuditLogDetail detail = new AuditLogDetail();
        String oldVal = null;
        String newVal = "foo";
        DefaultProcessor instance = new DefaultProcessor();
        instance.processDetailString(detail, oldVal, newVal);
        assertNull(detail.getOldValue());
        assertEquals("foo", detail.getNewValue());
    }

    /**
     * Test of getId method, of class DefaultProcessor.
     */
    @Test
    public void testGetId() {
        DefaultProcessor instance = new DefaultProcessor();
        instance.setHibernateHelper(new HibernateHelper());
        instance.getHibernateHelper().initialize();
        
        DummyInvoice entity = new DummyInvoice();
        entity.setId(1L);
        Long expResult = 1L;
        Long result = instance.getId(entity);
        assertEquals(expResult, result);
    }

    /**
     * Test of isPersistent method, of class DefaultProcessor.
     */
    @Test
    public void testIsPersistent() {
        DefaultProcessor instance = new DefaultProcessor();
        instance.setHibernateHelper(new HibernateHelper());
        instance.getHibernateHelper().initialize();
        
        Object entity = null;        
        boolean result = instance.isPersistent(entity);
        assertEquals(false, result);

        entity = 1L;
        result = instance.isPersistent(entity);
        assertEquals(false, result);


        entity = new DummyInvoice();
        result = instance.isPersistent(entity);
        assertEquals(true, result);
    }

    /**
     * Test of processDetailObject method, of class DefaultProcessor.
     */
    @Test
    public void testProcessDetailObject() {
        DefaultProcessor instance = new DefaultProcessor();
        instance.setHibernateHelper(new HibernateHelper());
        instance.getHibernateHelper().initialize();

        @SuppressWarnings("deprecation")
        AuditLogDetail detail = new AuditLogDetail();
        Object oldVal = null;
        Object newVal = null;
        instance.processDetailObject(detail, oldVal, newVal);        
        assertNull(detail.getOldValue());
        assertNull(detail.getOldValue());

        oldVal = 1L;
        newVal = 2L;
        instance.processDetailObject(detail, oldVal, newVal);
        assertEquals("1", detail.getOldValue());
        assertEquals("2", detail.getNewValue());

        oldVal = new DummyInvoice();
        ((DummyInvoice)oldVal).setId(3L);
        newVal = new DummyInvoice();
        ((DummyInvoice)newVal).setId(4L);
        instance.processDetailObject(detail, oldVal, newVal);
        assertEquals("3", detail.getOldValue());
        assertEquals("4", detail.getNewValue());
    }

    @Test
    public void testEscapeUnescape() {
        String[] vals = {null, "", "\"", ",", "foo", "()", "\\", "\"()\\", "\\", "\\c"};
        StringBuffer sb = new StringBuffer();

        for (String v : vals) {
            sb.setLength(0);
            DefaultProcessor.escape(sb, v);
            String u = DefaultProcessor.unescape(sb.toString());
            assertEquals(v, u);
        }
    }
}