package com.fiveamsolutions.nci.commons.audit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.hibernate.collection.PersistentList;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

import com.fiveamsolutions.nci.commons.util.HibernateHelper;
import com.fiveamsolutions.nci.commons.util.UsernameHolder;

/**
 *
 * @author gax
 */
public class AuditLogInterceptorTest {

    private static final Logger LOG = Logger.getLogger(AuditLogInterceptorTest.class);
    protected static SimpleNamingContextBuilder contextBuilder = new SimpleNamingContextBuilder();
    static {
        try {
            contextBuilder.activate();
        } catch (Exception e) {
            throw new Error(e);
        }
    }
    private AuditLogInterceptor audit;
    private HibernateHelper helper;
    protected Transaction transaction;
    private String oldUser;

    @Before
    @SuppressWarnings("deprecation")
    public void setUp() throws SQLException {
        oldUser = UsernameHolder.getUser();
        audit = new AuditLogInterceptor();
        helper = new HibernateHelper(null, audit);
        helper.initialize();
        audit.setHibernateHelper(helper);
        transaction = helper.beginTransaction();
        Statement s = helper.getCurrentSession().connection().createStatement();
        try {
            s.execute("drop sequence AUDIT_ID_SEQ");
            s.execute("drop table if exists dual_AUDIT_ID_SEQ");
        } catch (SQLException e) {
            // expected
        }
        transaction.commit();

        transaction = helper.beginTransaction();
        s = helper.getCurrentSession().connection().createStatement();
        s.execute("create sequence AUDIT_ID_SEQ");
        s.execute("create table dual_AUDIT_ID_SEQ(test boolean)");
        transaction.commit();

        SchemaExport se = new SchemaExport(helper.getConfiguration());
        se.drop(false, true);
        se.create(false, true);

        transaction = helper.beginTransaction();
    }


    @After
    public final void tearDown() {
        UsernameHolder.setUser(oldUser);
        try {
            transaction.commit();
        } catch (Exception e) {
            helper.rollbackTransaction(transaction);
        }
    }

    @Test
    public void collectionUpdate() {
        UsernameHolder.setUser("me");

        DummyInvoice i = new DummyInvoice();
        i.setOrderDate(new Date());
        DummyLineItem l = new DummyLineItem("Dummy Item", 1.0, 1.0);
        l.setInvoice(i);
        i.getItems().add(l);
        helper.getCurrentSession().save(i);
        helper.getCurrentSession().flush();

        DummyLineItem l2 = new DummyLineItem("Dummy Item", 1.0, 1.0);
        l2.setInvoice(i);
        i.getItems().add(l2);
        helper.getCurrentSession().update(i);

        helper.getCurrentSession().flush();
        helper.getCurrentSession().clear();


        List<AuditLogRecord> alr = find(DummyInvoice.class, i.getId());
        assertDetail(alr, AuditType.UPDATE, "items", i.getItems().get(0).getId().toString(),
                i.getItems().get(0).getId() + "," + i.getItems().get(1).getId());

    }

    @SuppressWarnings("unchecked")
    public List<AuditLogRecord> find(Class<?> type, Long entityId) {
        List<AuditLogRecord> result = findAuditLogRecords(type, entityId);
        assertTrue(!result.isEmpty());
        return result;
    }


    /**
     * @param type
     * @param entityId
     * @return
     * @throws HibernateException
     */
    private List<AuditLogRecord> findAuditLogRecords(Class<?> type,
            Long entityId) throws HibernateException {
        String str = "FROM " + AuditLogRecord.class.getName() + " alr "
                     + "WHERE alr.entityName = :entityName "
                     + "  AND alr.entityId = :entityId";
        Query q = helper.getCurrentSession().createQuery(str);
        q.setLong("entityId", entityId);
        q.setString("entityName", type.getSimpleName());
        List<AuditLogRecord> result = q.list();
        return result;
    }

    public static void assertDetail(List<AuditLogRecord> alr, AuditType auditType,
            String attribute, String oldVal, String newVal) {
        LOG.debug(String.format("record scan: %s, %s, %s", attribute, oldVal, newVal));
        for (AuditLogRecord r : alr) {
            LOG.debug("examining record: " + r);
            if (auditType == null || r.getType().equals(auditType)) {
                LOG.debug("correct audit type found");
                for (AuditLogDetail ald : r.getDetails()) {
                    LOG.debug(ald.getAttribute() + " " + ald.getOldValue() + " " + ald.getNewValue());
                    if (ald.getAttribute().equals(attribute)
                            && ObjectUtils.equals(ald.getOldValue(), oldVal)
                            && ObjectUtils.equals(ald.getNewValue(), newVal)) {
                        LOG.debug("Correct details found");
                        return;
                    }
                }
            }
        }
        fail("detail not found");
    }

    @Test
    public void onCollectionUpdate() {
        AuditLogInterceptor interceptor = new AuditLogInterceptor();
        interceptor.onCollectionUpdate(null, null);
        Map<?, ?> m = callGetRecords(interceptor).get();
        assertTrue(m==null || m.isEmpty());

        PersistentList dummy = new PersistentList();
        dummy.setOwner(null);
        interceptor.onCollectionUpdate(dummy, null);
        m = callGetRecords(interceptor).get();
        assertTrue(m==null || m.isEmpty());
    }

    @SuppressWarnings("unchecked")
    private static ThreadLocal<Map<Object, AuditLogRecord>> callGetRecords(AuditLogInterceptor interceptor) {
        try {
            Field f = AuditLogInterceptor.class.getDeclaredField("records");
            f.setAccessible(true);
            return (ThreadLocal<Map<Object, AuditLogRecord>>) f.get(interceptor);
        } catch (NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        } catch (SecurityException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String callGetValueString(DefaultProcessor i, Collection<? extends Auditable> value) {
        try {
            Method m = DefaultProcessor.class.getDeclaredMethod("getValueString", Collection.class);
            m.setAccessible(true);
            return (String) m.invoke(i, value);
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        } catch (SecurityException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            Throwable t = ex.getTargetException();
            if(t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            throw new RuntimeException(t);
        }
    }
    private static String callGetValueString(DefaultProcessor i, Map<? extends Auditable, ? extends Auditable> value) {
        try {
            Method m = DefaultProcessor.class.getDeclaredMethod("getValueString", Map.class);
            m.setAccessible(true);
            return (String) m.invoke(i, value);
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        } catch (SecurityException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            Throwable t = ex.getTargetException();
            if(t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            throw new RuntimeException(t);
        }
    }
    private static boolean callequalsMap(Map<?, ?> a, Map<?, ?> b) {
        try {
            Method m = AuditLogInterceptor.class.getDeclaredMethod("equalsMap", Map.class, Map.class);
            m.setAccessible(true);
            return (Boolean) m.invoke(null, a, b);
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        } catch (SecurityException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            Throwable t = ex.getTargetException();
            if(t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            throw new RuntimeException(t);
        }
    }

    @Test
    public void getValueStringFromCollectionWithNull() {
        DefaultProcessor x = new DefaultProcessor();
        assertNull(callGetValueString(x, (Collection<Auditable>) null));
    }
    @Test
    public void getValueStringFromCollection() {
        DefaultProcessor x = new DefaultProcessor();
        x.setHibernateHelper(helper);

        class Foo implements Auditable {
            private static final long serialVersionUID = 1L;
            public Long getId() {
                return Long.MAX_VALUE;
            }
        }
        final Foo dummy = new Foo();
        ArrayList<Foo> list = new ArrayList<Foo>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Iterator<Foo> iterator() {
                return new Iterator<Foo>() {
                    public boolean hasNext() { return true; }
                    public Foo next() { return dummy; }
                    public void remove() { };
                };
            }

        };

        String s = callGetValueString(x, list);
        assertTrue(s.length() == 4000);
        assertTrue(s.endsWith("..."));
    }

    @Test
    public void getValueStringFromMapWithNull() {
        DefaultProcessor x = new DefaultProcessor();
        assertNull(callGetValueString(x, (Map<Auditable, Auditable>) null));
    }

    @Test
    public void getValueStringFromMap() {
        LinkedHashMap<DummyInvoice, DummyLineItem> map = new LinkedHashMap<DummyInvoice, DummyLineItem>();
        final DummyLineItem di1 = new DummyLineItem("Dummy Item1", 1.0, 1.0);
        final DummyLineItem di2 = new DummyLineItem("Dummy Item2", 2.0, 2.0);
        final DummyLineItem di3 = new DummyLineItem("Dummy Item3", 3.0, 3.0);
        DummyInvoice di = new DummyInvoice();
        di.getItems().add(di1);
        map.put(di, di1);
        di = new DummyInvoice();
        di.getItems().add(di2);
        map.put(di, di2);
        di = new DummyInvoice();
        di.getItems().add(di3);
        map.put(di, di3);
        for (Map.Entry<DummyInvoice, DummyLineItem> e : map.entrySet()) {
            helper.getCurrentSession().save(e.getKey());
            helper.getCurrentSession().save(e.getValue());
        }

        DefaultProcessor x = new DefaultProcessor();
        x.setHibernateHelper(helper);

        String s = callGetValueString(x, map);
        assertEquals("(1,1),(2,2),(3,3)", s);
    }

    @Test
    public void equalsMap() {
        assertTrue(callequalsMap(null, null));
        assertFalse(callequalsMap(null, Collections.emptyMap()));
        @SuppressWarnings("rawtypes")
        Map a = Collections.singletonMap("ak", "av");
        @SuppressWarnings("rawtypes")
        Map b = Collections.singletonMap("ak", "av");
        assertTrue(callequalsMap(a, b));

        b = Collections.singletonMap("bk", "bv");
        assertFalse(callequalsMap(a, b));
    }

    @Test
    public void customAuditDetailCustomProcessor(){
        CustomProcessor cp = new CustomProcessor();
        audit.setProcessor(cp);
        DummyInvoice di = new DummyInvoice();
        DummyLineItem li = new DummyLineItem("Dummy Item", 1.0, 1.0);
        di.getItems().add(li);
        long t = 123456L;
        di.setOrderDate(new Date(t));
        helper.getCurrentSession().save(di);
        helper.getCurrentSession().flush();
        List<AuditLogRecord> l = find(DummyInvoice.class, di.getId());
        assertEquals(1, l.size());
        AuditLogRecord r = l.get(0);
        for (AuditLogDetail d : r.getDetails()) {
            if (d.getAttribute().equals("orderDate")) {
                assertEquals("date.change.event", d.getMessage());
                assertEquals(Long.toString(t), d.getNewValue());
                return;
            }
        }
        fail("no entry for orderDate");
    }
    
    @Test
    public void deleteAuditDetailCustomProcessor(){
        CustomProcessor cp = new CustomProcessor();
        audit.setProcessor(cp);
        DummyInvoice di = new DummyInvoice();
        DummyLineItem li = new DummyLineItem("Dummy Item", 1.0, 1.0);
        di.getItems().add(li);
        DummyLineItem li2 = new DummyLineItem("Dummy Item2", 2.0, 2.0);
        di.getItems().add(li2);
        long t = 123456L;
        di.setOrderDate(new Date(t));
        helper.getCurrentSession().save(di);
        helper.getCurrentSession().flush();
        List<AuditLogRecord> l = find(DummyInvoice.class, di.getId());
        assertEquals(1, l.size());
        DummyLineItem lineItem = (DummyLineItem) helper.getCurrentSession().get(DummyLineItem.class, li.getId());
        assertEquals("Dummy Item", lineItem.getItem());
        helper.getCurrentSession().delete(di);
        helper.getCurrentSession().flush();
        assertNull(helper.getCurrentSession().get(DummyLineItem.class, li.getId()));
        l = find(DummyInvoice.class, di.getId());
        assertEquals(2, l.size());
        boolean isInvoiceLogged = false;
        for (AuditLogRecord alr : l) {
            if (alr.getType().equals(AuditType.DELETE)) {                
                // 1 detail for orderDate and 1 for items
                assertEquals(2, alr.getDetails().size());
                isInvoiceLogged = true;
            }
        }
        assertTrue("DummyInvoice and its fields have not been logged.", isInvoiceLogged);
        boolean isLineItemLogged = false;
        List<AuditLogRecord> itemL = find(DummyLineItem.class, li.getId());
        for (AuditLogRecord alDumLine : itemL) {
            assertEquals(2, itemL.size());
            if (alDumLine.getType().equals(AuditType.DELETE)) {                
                // 1 detail for name, 1 for quant, 1 for unit price.
                assertEquals(3, alDumLine.getDetails().size());
                isLineItemLogged = true;
            }
        }  
        assertTrue("DummyLineItem and its fields have not been logged.", isLineItemLogged);
        boolean isLineItemLogged2 = false;
        List<AuditLogRecord> itemL2 = find(DummyLineItem.class, li2.getId());
        for (AuditLogRecord alDumLine2 : itemL2) {
            assertEquals(2, itemL2.size());
            if (alDumLine2.getType().equals(AuditType.DELETE)) {                
                // 1 detail for name, 1 for quant, 1 for unit price.
                assertEquals(3, alDumLine2.getDetails().size());
                isLineItemLogged2 = true;
            }
        }  
        assertTrue("DummyLineItem2 and its fields have not been logged.", isLineItemLogged2);
    } 
    
    @Test
    public void deleteInvertedAuditDetailCustomProcessor(){
        CustomProcessor cp = new CustomProcessor();
        audit.setProcessor(cp);
        DummyInvertedInvoice di = new DummyInvertedInvoice();
        long t = 123456L;
        di.setOrderDate(new Date(t));
        helper.getCurrentSession().save(di);
        helper.getCurrentSession().flush();
        
        DummyInvertedLineItem li = new DummyInvertedLineItem("Dummy Item", 1.0, 1.0);
        li.setInvoice(di);
        helper.getCurrentSession().save(li);
        helper.getCurrentSession().flush();
    
        helper.getCurrentSession().delete(li);
        helper.getCurrentSession().flush();
        List<AuditLogRecord> itemL = find(DummyInvertedLineItem.class, li.getId());
        for (AuditLogRecord alDumLine : itemL) {
            assertEquals(2, itemL.size());
            if (alDumLine.getType().equals(AuditType.DELETE)) {                
                // 1 detail for name, 1 for quant, 1 for unit price, 1 for invoice.
                assertEquals(4, alDumLine.getDetails().size());
               
            }
        }  
    }
    
    @Test
    public void compositeUserTypeTest() {
        DummyCompositeEntity dummyEntity = new DummyCompositeEntity();
        DummyCompositeField dummyField = new DummyCompositeField();
        dummyField.setField1("beginning");
        dummyField.setField2(5);
        dummyEntity.setCompositeField(dummyField);
        helper.getCurrentSession().save(dummyEntity);
        helper.getCurrentSession().flush();

        assertDetail(find(DummyCompositeEntity.class, dummyEntity.getId()), AuditType.INSERT, "compositeField", null,
                getAuditString(dummyField));

        DummyCompositeField dummyField2 = new DummyCompositeField();
        dummyField2.setField1("newbeginning");
        dummyField2.setField2(6);
        dummyEntity.setCompositeField(dummyField2);
        helper.getCurrentSession().update(dummyEntity);
        helper.getCurrentSession().flush();

        assertDetail(find(DummyCompositeEntity.class, dummyEntity.getId()), AuditType.UPDATE, "compositeField",
                getAuditString(dummyField), getAuditString(dummyField2));
    }

    @Test
    public void compositeUserTypeCollectionTest() {
        DummyCompositeEntity dummyEntity = new DummyCompositeEntity();
        DummyCompositeField dummyField1 = new DummyCompositeField();
        dummyField1.setField1("beginning1");
        dummyField1.setField2(9);
        dummyField1.setField3(false);
        DummyCompositeField dummyField2 = new DummyCompositeField();
        dummyField2.setField1("beginning2");
        dummyField2.setField2(11);
        dummyEntity.setCompositeFields(new HashSet<DummyCompositeField>());
        dummyEntity.getCompositeFields().add(dummyField1);
        dummyEntity.getCompositeFields().add(dummyField2);
        helper.getCurrentSession().save(dummyEntity);
        helper.getCurrentSession().flush();

        assertDetail(find(DummyCompositeEntity.class, dummyEntity.getId()), AuditType.INSERT, "compositeFields", null,
                getAuditString(dummyEntity.getCompositeFields()));
    }
    
    @Test
    public void enableDisable() {
        AuditLogInterceptor interceptor = new AuditLogInterceptor();
        interceptor.disable();
        assertFalse(interceptor.isEnabled());
        interceptor.enable();
        assertTrue(interceptor.isEnabled());
    }
    
    @Test
    public void noAuditsWhenDisabled() {
        UsernameHolder.setUser("me");

        try {
            audit.disable();
            DummyInvoice i = new DummyInvoice();
            i.setOrderDate(new Date());
            DummyLineItem l = new DummyLineItem("Dummy Item", 1.0, 1.0);
            l.setInvoice(i);
            i.getItems().add(l);
            helper.getCurrentSession().save(i);
            helper.getCurrentSession().flush();
            helper.getCurrentSession().delete(i);
            helper.getCurrentSession().clear();
            List<AuditLogRecord> alr = findAuditLogRecords(DummyInvoice.class, i.getId());
            assertTrue(alr.isEmpty());           
        } finally {
            audit.enable();
        }

       

    }

    @SuppressWarnings("unchecked")
    private String getAuditString(DummyCompositeField dummyField) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("field1", dummyField.getField1());
        jsonObject.put("field2", dummyField.getField2());
        jsonObject.put("field3", dummyField.getField3());
        return jsonObject.toString();
    }

    private String getAuditString(Collection<DummyCompositeField> dummyFields) {
        StringBuffer sb = new StringBuffer();
        for (DummyCompositeField field : dummyFields) {
            // TODO When fixing NCIC-161 'field.toString()' should be replaced with 'getAuditString(field)'
            // (i.e. a concatenation of JSON strings).
            sb.append(field.toString()).append(',');
        }
        return sb.substring(0, sb.length() - 1);
    }
}
