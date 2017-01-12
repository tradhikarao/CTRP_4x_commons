package com.fiveamsolutions.nci.commons.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.fiveamsolutions.nci.commons.data.search.PageSortParams;
import com.fiveamsolutions.nci.commons.data.search.SortCriterion;

public class PageSortParamsTest {

    PageSortParams<Person> params;

    @Before
    public void init() {
        params = new PageSortParams<Person>(1, 2, PersonSortCriterion.PERSON_FULLNAME.getOrderByList(), true);
        assertEquals(1, params.getPageSize());
        assertEquals(2, params.getIndex());
        assertEquals(PersonSortCriterion.PERSON_FULLNAME.getOrderByList(), params.getSortCriteria());
        assertEquals(true, params.isDesc());
        assertNull(params.getDynamicSortCriteria());
    }

    @Test
    public void testConstructor() {
        params = new PageSortParams<Person>(1, 2, PersonSortCriterion.PERSON_FIRSTNAME.getOrderByList(), true);
        assertEquals(1, params.getPageSize());
        assertEquals(2, params.getIndex());
        assertEquals(Collections.singletonList(PersonSortCriterion.PERSON_FIRSTNAME), params.getSortCriteria());
        assertEquals(true, params.isDesc());
        assertNull(params.getDynamicSortCriteria());
    }

    @Test
    public void testConstructor2() {
        params = new PageSortParams<Person>(1, 2, (SortCriterion<Person>) null, true);
        assertEquals(1, params.getPageSize());
        assertEquals(2, params.getIndex());
        assertEquals(null, params.getSortCriteria());
        assertEquals(true, params.isDesc());
        assertNull(params.getDynamicSortCriteria());
    }

    @Test
    public void testConstructor3() {
        params = new PageSortParams<Person>(1, 2, PersonSortCriterion.PERSON_FULLNAME.getOrderByList(), true);
        assertEquals(1, params.getPageSize());
        assertEquals(2, params.getIndex());
        assertEquals(PersonSortCriterion.PERSON_FULLNAME.getOrderByList(), params.getSortCriteria());
        assertEquals(true, params.isDesc());
        assertNull(params.getDynamicSortCriteria());
    }

    @Test
    public void testConstructor4() {
        params = new PageSortParams<Person>(1, 2, PersonSortCriterion.PERSON_FULLNAME.getOrderByList(), true,
                Collections.singletonList("test"));
        assertEquals(1, params.getPageSize());
        assertEquals(2, params.getIndex());
        assertEquals(PersonSortCriterion.PERSON_FULLNAME.getOrderByList(), params.getSortCriteria());
        assertEquals(true, params.isDesc());
        assertNotNull(params.getDynamicSortCriteria());
        assertFalse(params.getDynamicSortCriteria().isEmpty());
        assertEquals(1, params.getDynamicSortCriteria().size());
        assertEquals("test", params.getDynamicSortCriteria().get(0));
    }

    @Test
    public void testSetPageSize() {
        params.setPageSize(10);
        assertEquals(10, params.getPageSize());
    }

    @Test
    public void testSetIndex() {
        params.setIndex(10);
        assertEquals(10, params.getIndex());
    }

    @Test
    public void testSetDesc() {
        params.setDesc(true);
        assertEquals(true, params.isDesc());
    }

    @Test
    public void testSetDynamicSortCriteria() {
        params.setDynamicSortCriteria(Collections.singletonList("test"));
        assertNotNull(params.getDynamicSortCriteria());
        assertFalse(params.getDynamicSortCriteria().isEmpty());
        assertEquals(1, params.getDynamicSortCriteria().size());
        assertEquals("test", params.getDynamicSortCriteria().get(0));
    }

    public static class Person {

    }

    public static enum PersonSortCriterion implements SortCriterion<Person> {
        PERSON_FIRSTNAME, PERSON_FULLNAME;
        private final List<SortCriterion<Person>> l = new ArrayList<SortCriterion<Person>>();

        public List<SortCriterion<Person>> getOrderByList() {
            if (l.isEmpty()) {
                l.add(this);
            }
            return l;
        }

        public String getOrderField() {
            return null;
        }

        public String getLeftJoinAlias() {
            return null;
        }

        public String getLeftJoinField() {
            return null;
        }
    }
}
