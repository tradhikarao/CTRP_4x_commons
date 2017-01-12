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
package com.fiveamsolutions.nci.commons.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.junit.Test;

import com.fiveamsolutions.nci.commons.data.persistent.PersistentObject;
import com.fiveamsolutions.nci.commons.search.SearchableUtilsTest.E.EResult;
import com.fiveamsolutions.nci.commons.service.GenericSearchService;

/**
 * Tests for the @Searchable framework.
 */
@SuppressWarnings({"unused", "unchecked" })
public class SearchableUtilsTest {

    @Test
    public void testNull() throws Exception {
        assertFalse(SearchableUtils.hasSearchableCriterion(null));
    }

    @Test
    public void noMethods() throws Exception {
        assertFalse(SearchableUtils.hasSearchableCriterion(new Object()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void badGetter() throws Exception {
        SearchableUtils.hasSearchableCriterion(new Object() {
           @Searchable
           public void getSomething() {}
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void getterThrowsException() throws Exception {
        SearchableUtils.hasSearchableCriterion(new Object() {
           @Searchable
           public String getSomething() {
               throw new IllegalArgumentException();
           }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void getterThrowsExceptionForSearch() throws Exception {
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(new Object() {
           @Searchable
           public String getSomething() {
               throw new IllegalArgumentException();
           }
        }, sc);
    }

    @Test
    public void testEmptyStrings() throws Exception {
        Object o = new Object() {
            @Searchable
            public String getNull() {
                return null;
            }
            @Searchable
            public String getEmpty() {
                return "";
            }
            @Searchable
            public String getBlank() {
                return " \t";
            }
        };
        assertFalse(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(o, sc);
        assertEquals(0, selectClause.length());
        assertEquals(0, whereClause.length());
        assertEquals(0, params.size());
        assertEquals(SearchableUtils.WHERE, sc.getSavedState());
    }

    @Test
    public void testThreeInsensitiveSimpleFields() throws Exception {
        Object o = new Object() {
            @Searchable
            public String getA() {
                return "a";
            }
            @Searchable(matchMode = Searchable.MATCH_MODE_START, caseSensitive = false)
            public String getB() {
                return "b";
            }
            @Searchable(matchMode = Searchable.MATCH_MODE_CONTAINS, caseSensitive = false)
            public String getC() {
                return "c";
            }
        };
        assertTrue(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(o, sc);
        assertEquals(0, selectClause.length());
        assertTrue(whereClause.length() > 0);
        assertTrue(whereClause.indexOf("lower(obj.a)") > 0);
        assertTrue(whereClause.indexOf("lower(obj.b)") > 0);
        assertTrue(whereClause.indexOf("lower(obj.c)") > 0);
        assertTrue(whereClause.indexOf("= :obj_a") > 0);
        assertTrue(whereClause.indexOf("like :obj_b") > 0);
        assertTrue(whereClause.indexOf("like :obj_c") > 0);
        assertEquals(params.get("obj_a"), "a");
        assertEquals(params.get("obj_b"), "b%");
        assertEquals(params.get("obj_c"), "%c%");
        assertEquals(SearchableUtils.AND, sc.getSavedState());
    }

    @Test
    public void testExactField() throws Exception {
        Object o = new Object() {
            @Searchable(matchMode = Searchable.MATCH_MODE_EXACT)
            public String getB() {
                return "AT";
            }

            @Searchable(matchMode = Searchable.MATCH_MODE_EXACT, caseSensitive = true)
            public String getBC() {
                return "AT";
            }
        };
        assertTrue(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(o, sc);
        assertEquals(0, selectClause.length());
        assertTrue(whereClause.length() > 0);
        assertTrue(whereClause.indexOf("= :obj_b") > 0);
        assertTrue(whereClause.indexOf("= :obj_bC") > 0);

        assertEquals(params.get("obj_b"), "at");
        assertEquals(params.get("obj_bC"), "AT");

        assertEquals(SearchableUtils.AND, sc.getSavedState());
    }

    private void notEqualsTestHelper(Object o, String operator) {
        assertTrue(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(o, sc);
        assertEquals(0, selectClause.length());
        assertTrue(whereClause.length() > 0);
        assertTrue(whereClause.indexOf(operator + " :obj_b") > 0);
        assertEquals(params.get("obj_b"), Integer.valueOf(1));
        assertEquals(SearchableUtils.AND, sc.getSavedState());
    }

    @Test
    public void testLessThanField() throws Exception {
        Object o = new Object() {
            @Searchable(matchMode = Searchable.MATCH_MODE_LESS)
            public Integer getB() {
                return 1;
            }
        };
        notEqualsTestHelper(o, "<");
    }

    @Test
    public void testLessThanOrEqualField() throws Exception {
        Object o = new Object() {
            @Searchable(matchMode = Searchable.MATCH_MODE_LESS_OR_EQUAL)
            public Integer getB() {
                return 1;
            }
        };
        notEqualsTestHelper(o, "<=");
    }

    @Test
    public void testGreaterThanField() throws Exception {
        Object o = new Object() {
            @Searchable(matchMode = Searchable.MATCH_MODE_GREATER)
            public Integer getB() {
                return 1;
            }
        };
        notEqualsTestHelper(o, ">");
    }

    @Test
    public void testGreaterThanOrEqualField() throws Exception {
        Object o = new Object() {
            @Searchable(matchMode = Searchable.MATCH_MODE_GREATER_OR_EQUAL)
            public Integer getB() {
                return 1;
            }
        };
        notEqualsTestHelper(o, ">=");
    }

    @Test
    public void testNotEqualField() throws Exception {
        Object o = new Object() {
            @Searchable(matchMode = Searchable.MATCH_MODE_NOT_EQUAL)
            public Integer getB() {
                return 1;
            }
        };
        notEqualsTestHelper(o, "<>");
    }

    @Test
    public void testLikeOnNonStringField() throws Exception {
        Object o = new Object() {
            @Searchable(matchMode = Searchable.MATCH_MODE_START)
            public Integer getB() {
                return 1;
            }
        };
        notEqualsTestHelper(o, "=");
    }

    @Test
    public void testThreeSensitiveSimpleFields() throws Exception {
        Object o = new Object() {
            @Searchable(caseSensitive = true)
            public String getA() {
                return "a";
            }
            @Searchable(matchMode = Searchable.MATCH_MODE_START, caseSensitive = true)
            public String getB() {
                return "b";
            }
            @Searchable(matchMode = Searchable.MATCH_MODE_CONTAINS, caseSensitive = true)
            public String getC() {
                return "c";
            }
        };
        assertTrue(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(o, sc);
        assertEquals(0, selectClause.length());
        assertTrue(whereClause.length() > 0);
        assertTrue(whereClause.indexOf("lower(obj.a)") == -1);
        assertTrue(whereClause.indexOf("lower(obj.b)") == -1);
        assertTrue(whereClause.indexOf("lower(obj.c)") == -1);
        assertTrue(whereClause.indexOf("= :obj_a") > 0);
        assertTrue(whereClause.indexOf("like :obj_b") > 0);
        assertTrue(whereClause.indexOf("like :obj_c") > 0);
        assertEquals(params.get("obj_a"), "a");
        assertEquals(params.get("obj_b"), "b%");
        assertEquals(params.get("obj_c"), "%c%");
        assertEquals(SearchableUtils.AND, sc.getSavedState());
    }

    @Test
    public void testContainsAndSensitiveSimpleField() throws Exception {
        Object sensObj = new Object() {
            @Searchable(matchMode = Searchable.MATCH_MODE_CONTAINS, caseSensitive = true)
            public String getA() {
                return "a";
            }
        };
        assertTrue(SearchableUtils.hasSearchableCriterion(sensObj));
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(sensObj, sc);
        assertEquals(0, selectClause.length());
        assertTrue(whereClause.length() > 0);
        assertTrue(whereClause.indexOf("lower") == -1);
        assertEquals(params.get("obj_a"), "%a%");
        assertEquals(SearchableUtils.AND, sc.getSavedState());
    }

    @Test
    public void testOmitAndNested() throws Exception {
        Outer6 o6 = new Outer6();
        assertTrue(SearchableUtils.hasSearchableCriterion(o6));
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(o6, sc);
        assertEquals(18, selectClause.length());
        assertTrue(selectClause.indexOf("join obj.g obj_g") > 0);
        assertTrue(whereClause.length() > 0);
        assertTrue(whereClause.indexOf("lower(obj_g.boo.foo) = :obj_g_boo_foo") > 0);
        assertEquals(params.get("obj_g_boo_foo"), "test");
        assertEquals(SearchableUtils.AND, sc.getSavedState());
    }

    @Test
    public void testContainsAndInSensitiveSimpleField() throws Exception {
        Object sensObj = new Object() {
            @Searchable(matchMode = Searchable.MATCH_MODE_CONTAINS, caseSensitive = false)
            public String getA() {
                return "a";
            }
        };
        assertTrue(SearchableUtils.hasSearchableCriterion(sensObj));
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(sensObj, sc);
        assertEquals(0, selectClause.length());
        assertTrue(whereClause.length() > 0);
        assertTrue(whereClause.indexOf("lower") >= 0);
        assertEquals(params.get("obj_a"), "%a%");
        assertEquals(SearchableUtils.AND, sc.getSavedState());
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEmptyCollectionField() throws Exception {
        Object o = new Object() {
            
            @Searchable
            public Collection getNoFields() {
                return Collections.emptyList();
            }
            @Searchable(fields = {})
            public Collection getEmptyFields() {
                return Collections.emptyList();
            }
            @Searchable(fields = {"foo", "bar"})
            public Collection getNull() {
                return null;
            }
            @Searchable(fields = {"foo", "bar"})
            public Collection getEmpty() {
                return Collections.emptyList();
            }
            @Searchable(fields = {"foo"})
            public Collection getNullInCollection() {
                return Collections.singleton(new Object() {
                    public String getFoo() {
                        return null;
                    }
                });
            }
            @Searchable(fields = {"foo"})
            public Collection getEmptyInCollection() {
                return Collections.singleton(new Object() {
                    public String getFoo() {
                        return "";
                    }
                });
            }

            @Searchable(nested = true)
            public Collection getNestedEmptyList() {
                return Collections.emptyList();
            }
            @Searchable(nested = true)
            public Collection getNestedNull() {
                return null;
            }
            @Searchable(nested = true)
            public Collection getNullInNestedCollection() {
                return Collections.singleton(new Object() {
                    @Searchable
                    public String getFoo() {
                        return null;
                    }
                });
            }
            @Searchable(nested = true)
            public Collection getEmptyInNestedCollection() {
                return Collections.singleton(new Object() {
                    @Searchable
                    public String getFoo() {
                        return "";
                    }
                });
            }

        };
        assertFalse(SearchableUtils.hasSearchableCriterion(o));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoFields() throws Exception {
        Object o = new Object() {
            @SuppressWarnings("rawtypes")
            @Searchable
            public Collection getNoFields() {
                return Collections.emptyList();
            }
        };
        assertFalse(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(o, sc);
    }

    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("rawtypes")
    public void testNoFieldsOrNestedWithOmitJoinClause() throws Exception {
        Object oNest = new Object() {
            @Searchable (isHibernateComponent = true, nested = true)
            public Collection getNoFields() {
                return Collections.emptyList();
            }
        };
        Object oField = new Object() {
            @Searchable (isHibernateComponent = true, fields = "id")
            public Collection getNoFields() {
                return Collections.emptyList();
            }
        };

        Object oBad = new Object() {
            @Searchable (isHibernateComponent = true)
            public Collection getNoFields() {
                return Collections.emptyList();
            }
        };
        assertFalse(SearchableUtils.hasSearchableCriterion(oNest));
        assertFalse(SearchableUtils.hasSearchableCriterion(oField));
        assertFalse(SearchableUtils.hasSearchableCriterion(oBad));

        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(oNest, sc);
        SearchableUtils.iterateAnnotatedMethods(oField, sc);
        SearchableUtils.iterateAnnotatedMethods(oBad, sc);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEmptyCol() throws Exception {
        Object o = new Object() {
            @Searchable(fields = {"foo", "bar"})
            public Collection getEmpty() {
                return Collections.emptyList();
            }

            @Searchable(nested=true)
            public Collection getNestedEmpty() {
                return Collections.emptyList();
            }
        };
        assertFalse(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(o, sc);
        assertEquals(0, selectClause.length());
        assertEquals(0, whereClause.length());
        assertEquals(0, params.size());
        assertEquals(SearchableUtils.WHERE, sc.getSavedState());
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEmptyColValue() throws Exception {
        Object o = new Object() {
            @Searchable(fields = {"foo"})
            public Collection getEmptyInCollection() {
                return Collections.singleton(new Object() {
                    public String getFoo() {
                        return "";
                    }
                });
            }

            @Searchable(nested = true)
            public Collection getEmptyInNestedCollection() {
                return Collections.singleton(new Object() {
                    @Searchable
                    public String getFoo() {
                        return "";
                    }
                });
            }
        };
        assertFalse(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(o, sc);
        assertEquals(0, selectClause.length());
        assertEquals(0, whereClause.length());
        assertEquals(0, params.size());
        assertEquals(SearchableUtils.WHERE, sc.getSavedState());
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testMixedColValue() throws Exception {
        final String result = "Full";
        Object o = new Object() {
            @Searchable(fields = {"foo"})
            public Collection getEmptyInCollection() {
                return Collections.singleton(new Object() {
                    public String getFoo() {
                        return "";
                    }
                });
            }

            @Searchable(nested = true)
            public Collection getEmptyInNestedCollection() {
                return Collections.singleton(new Object() {
                    @Searchable
                    public String getFoo() {
                        return "";
                    }
                });
            }
            
            @Searchable(nested = true)
            public Collection getFullInNestedCollection() {
                return Collections.singleton(new Object() {
                    @Searchable
                    public String getFoo() {
                        return result;
                    }
                });
            }
        };
        assertTrue(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(o, sc);
        assertTrue(selectClause.length() > 0);
        assertEquals(" join obj.fullInNestedCollection obj_fullInNestedCollection", selectClause.toString());
        assertTrue(whereClause.length() > 0);
        assertEquals(" WHERE  (  lower(obj_fullInNestedCollection.foo)  in (:obj_fullInNestedCollection_foo) ) ", whereClause.toString());
        assertEquals(1, params.size());
        Set<?> objects = (Set<?>) params.get(params.keySet().iterator().next());
        assertEquals(1, objects.size());
        for (Object obj : objects) {
            String next = (String) objects.iterator().next();
            assertFalse(result.equals(next));
            assertEquals(result.toLowerCase(), next);
        }

        assertEquals(SearchableUtils.AND, sc.getSavedState());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooManyEls() throws Exception {
        Object o = new Object() {
            @Searchable(fields = {"foo"})
            @SuppressWarnings("rawtypes")
            public Collection getBigCollection() {
                Collection<Object> result = new HashSet<Object>();
                for (int i = 0; i < GenericSearchService.MAX_IN_CLAUSE_SIZE + 1; ++i) {
                    result.add(new Object() {
                       public String getFoo() {
                           return "foo";
                       }
                    });
                }
                return result;
            }
        };
        assertTrue(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(o, sc);
    }

    @Test
    public void testCollectionMatchModeExact() throws Exception {
        final String result = "iAmATest";
        Object o = new Object() {
            @Searchable(fields = "foo")
            @SuppressWarnings("rawtypes")
            public Collection getSomething() {
                return Collections.singleton(new Object() {
                    public Object getFoo() {
                        return result;
                    }
                });
            }
        };
        assertTrue(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(o, sc);
        assertTrue(whereClause.length() > 0);
        assertEquals(" WHERE  (  lower(obj_something.foo)  in (:obj_something_foo) ) ", whereClause.toString());
        assertTrue(selectClause.length() > 0);
        assertEquals(1, params.size());
        Set<?> objects = (Set<?>) params.get(params.keySet().iterator().next());
        assertEquals(1, objects.size());
        for (Object obj : objects) {
            String next = (String) objects.iterator().next();
            assertFalse(result.equals(next));
            assertEquals(result.toLowerCase(), next);
        }
    }

    @Test
    public void testCollectionMatchModeExactCaseSensitive() throws Exception {
        final String result = "iAmATest";
        Object o = new Object() {
            @Searchable(fields = "foo", caseSensitive = true)
            @SuppressWarnings("rawtypes")
            public Collection getSomething() {
                return Collections.singleton(new Object() {
                    public Object getFoo() {
                        return result;
                    }
                });
            }
        };
        assertTrue(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(o, sc);
        assertTrue(whereClause.length() > 0);
        assertEquals(" WHERE  (  obj_something.foo  in (:obj_something_foo) ) ", whereClause.toString());
        assertTrue(selectClause.length() > 0);
        assertEquals(1, params.size());
        Set<?> objects = (Set<?>) params.get(params.keySet().iterator().next());
        assertEquals(1, objects.size());
        for (Object obj : objects) {
            String next = (String) objects.iterator().next();
            assertEquals(result, next);
        }
    }

    @Test
    public void testNestedCollectionMatchModeExact() throws Exception {
        final String result = "iamatest";
        Object o = new Object() {
            @Searchable(nested = true)
            @SuppressWarnings("rawtypes")
            public Collection getSomething() {
                return Collections.singleton(new Object() {
                    @Searchable
                    public Object getFoo() {
                        return result;
                    }
                });
            }
        };
        assertTrue(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(o, sc);
        assertTrue(whereClause.length() > 0);
        assertTrue(selectClause.length() > 0);
        assertEquals(1, params.size());
        Set<?> objects = (Set<?>) params.get(params.keySet().iterator().next());
        assertEquals(1, objects.size());
        for (Object obj : objects) {
            String next = (String) objects.iterator().next();
            assertTrue(next.startsWith(result));
        }
    }

    @Test
    public void testCollectionMatchModeStart() throws Exception {
        final String result = "iamatest";
        Object o = new Object() {
            @Searchable(fields = "foo", matchMode = Searchable.MATCH_MODE_START)
            @SuppressWarnings("rawtypes")
            public Collection getSomethingElse() {
                Collection<Object> resultCol = new HashSet<Object>();
                for (int i = 0; i < 3; ++i) {
                    resultCol.add(new Object() {
                        public Object getFoo() {
                            return result;
                        }
                    });
                }
                return resultCol;
            }
        };
        assertTrue(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(o, sc);
        assertTrue(whereClause.length() > 0);
        assertTrue(selectClause.length() > 0);
        assertEquals(params.size(), 1);
        String param =  (String) params.get(params.keySet().iterator().next());
        assertEquals(result + "%", param);
    }

    @Test
    public void testNestedCollectionMatchModeStart() throws Exception {
        final String result = "iamatest";
        Object o = new Object() {
            @Searchable(nested = true)
            @SuppressWarnings("rawtypes")
            public Collection getSomethingElse() {
                Collection<Object> resultCol = new HashSet<Object>();
                for (int i = 0; i < 3; ++i) {
                    resultCol.add(new Object() {
                        @Searchable(matchMode = Searchable.MATCH_MODE_START)
                        public Object getFoo() {
                            return result;
                        }
                    });
                }
                return resultCol;
            }
        };
        assertTrue(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(o, sc);
        assertTrue(whereClause.length() > 0);
        assertTrue(selectClause.length() > 0);
        assertEquals(params.size(), 1);
        String param =  (String) params.get(params.keySet().iterator().next());
        assertEquals(result + "%", param);
    }

    @Test
    public void testCollectionMatchModeContains() throws Exception {
        final String result = "iamatest";
        Object o = new Object() {
            @Searchable(fields = "foo", matchMode = Searchable.MATCH_MODE_CONTAINS)
            @SuppressWarnings("rawtypes")
            public Collection getSomethingElse() {
                Collection<Object> resultCol = new HashSet<Object>();
                for (int i = 0; i < 3; ++i) {
                    resultCol.add(new Object() {
                        public Object getFoo() {
                            return result;
                        }
                    });
                }
                return resultCol;
            }
        };
        assertTrue(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(o, sc);
        assertTrue(whereClause.length() > 0);
        assertTrue(selectClause.length() > 0);
        assertEquals(params.size(), 1);
        String param =  (String) params.get(params.keySet().iterator().next());
        assertEquals("%" + result + "%", param);
    }

    @Test
    public void testNestedCollectionMatchModeContains() throws Exception {
        final String result = "iamatest";
        Object o = new Object() {
            @Searchable(nested = true)
            @SuppressWarnings("rawtypes")
            public Collection getSomethingElse() {
                Collection<Object> resultCol = new HashSet<Object>();
                for (int i = 0; i < 3; ++i) {
                    resultCol.add(new Object() {
                        @Searchable(matchMode = Searchable.MATCH_MODE_CONTAINS)
                        public Object getFoo() {
                            return result;
                        }
                    });
                }
                return resultCol;
            }
        };
        assertTrue(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(o, sc);
        assertTrue(whereClause.length() > 0);
        assertTrue(selectClause.length() > 0);
        assertEquals(params.size(), 1);
        String param =  (String) params.get(params.keySet().iterator().next());
        assertEquals("%" + result + "%", param);
    }

    @Test
    public void nonCollectionWithFields() throws Exception {
        Outer1 o = new Outer1();
        assertFalse(SearchableUtils.hasSearchableCriterion(o));
    }

    @Test(expected = IllegalArgumentException.class)
    public void nonCollectionException() throws Exception {
        // the new Object class isn't visible, thus foo isn't a bean property
        SearchableUtils.hasSearchableCriterion(new Object() {
           @Searchable(fields = "foo")
           public Object getA() {
               return new Object() {
                   public Object getFoo() {
                       return null;
                   }
                   public void setFoo(Object foo) {}
               };
           }
        });
    }

    @Test
    public void testPersistentAsField() throws Exception {
        Outer2 o = new Outer2();
        assertTrue(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(o, sc);
        assertTrue(whereClause.length() > 0);
        assertStringExistsOnce(whereClause, "obj.e.foo = :obj_e_foo");
        assertEquals(0, selectClause.length());
        assertEquals(params.size(), 1);
        assertEquals(o.getE().result.getId(), ((EResult) params.get("obj_e_foo")).getId());
    }

    @Test
    public void testNestedPersistentAsField() throws Exception {
        // make sure if there's a nested PersistentObject with an ID, either alone or in a collection,
        // that only the ID is used in the query and all the other fields are ignored
        Outer2Nested o = new Outer2Nested();
        assertTrue(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(o, sc);
        assertTrue(selectClause.length() > 0);
        assertStringExistsOnce(selectClause, "join obj.e obj_e");
        assertStringExistsOnce(selectClause, "join obj.es obj_es");
        assertStringExistsOnce(selectClause, "join obj_es.foo obj_es_foo");
        assertEquals(-1, selectClause.indexOf("join obj_e.foo obj_e_foo"));

        assertTrue(whereClause.length() > 0);
        assertStringExistsOnce(whereClause, "obj_e.foo.id = :obj_e_foo_id");
        assertStringExistsOnce(whereClause, "obj_es_foo.id  in (:obj_es_foo_id)");
        assertEquals(-1, whereClause.indexOf("string"));
        assertEquals(-1, whereClause.indexOf("anotherString"));
        assertEquals(params.size(), 2);
        assertEquals(1L, params.get("obj_e_foo_id"));
        Collection<Long> ids = (Collection<Long>) params.get("obj_es_foo_id");
        assertEquals(1, ids.size());
        assertEqualsInCollection(1L, ids);
    }

    @Test
    public void testNestedPersistentWithoutIdAsField() throws Exception {
        // make sure if there's a nested PersistentObject without an ID, either alone or in a collection,
        // that the ID is ignored in the query and all the other fields are used
        Outer2Nested o = new Outer2Nested();
        o.getE().getFoo().id = null;
        assertTrue(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(o, sc);
        assertTrue(selectClause.length() > 0);
        assertStringExistsOnce(selectClause, "join obj.e obj_e");
        assertStringExistsOnce(selectClause, "join obj.es obj_es");
        assertStringExistsOnce(selectClause, "join obj_e.foo obj_e_foo");
        assertStringExistsOnce(selectClause, "join obj_es.foo obj_es_foo");

        assertTrue(whereClause.length() > 0);
        assertEquals(-1, whereClause.indexOf("obj_e.foo.id"));
        assertStringExistsOnce(whereClause, "obj_es_foo.id  in (:obj_es_foo_id)");
        assertStringExistsOnce(whereClause, "lower(obj_e_foo.string) like :obj_e_foo_string");
        assertStringExistsOnce(whereClause, "obj_e_foo.anotherString = :obj_e_foo_anotherString");
        assertEquals(params.size(), 3);
        assertEquals("%a string%", params.get("obj_e_foo_string"));
        assertEquals("another string", params.get("obj_e_foo_anotherString"));
        Collection<Long> ids = (Collection<Long>) params.get("obj_es_foo_id");
        assertEquals(1, ids.size());
        assertEqualsInCollection(1L, ids);
    }

    @Test
    public void testNonPersistentAsField() throws Exception {
        Outer3 o = new Outer3();
        assertTrue(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(o, sc);
        assertTrue(whereClause.length() > 0);
        assertEquals(0, selectClause.length());
        assertEquals(params.size(), 1);
        assertEquals("test%", params.get(params.keySet().iterator().next()));
    }

    @Test
    public void testNonPersistentAsContainsFieldSensitive() throws Exception {
        Outer4 o = new Outer4();
        assertTrue(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(o, sc);
        assertTrue(whereClause.length() > 0);
        assertTrue(whereClause.indexOf("lower") == -1);
        assertEquals(0, selectClause.length());
        assertEquals(params.size(), 1);
        assertEquals("%test%", params.get(params.keySet().iterator().next()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNestedAndFields() throws Exception {
        class BadClass {
            @Searchable(nested = true, fields={"foo"})
            public NestedInner getNestedInner() {
                return new NestedInner();
            }
        }
        BadClass bad = new BadClass();
        SearchCallback sc = new SearchCallback(new StringBuffer(), new StringBuffer(), new HashMap<String, Object>());
        SearchableUtils.iterateAnnotatedMethods(bad, sc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCollectionNestedAndFields() throws Exception {
        class BadClass {
            @Searchable(nested = true, fields={"foo"})
            public Collection<NestedInner> getNestedInner() {
                return Collections.singleton(new NestedInner());
            }
        }
        BadClass bad = new BadClass();
        SearchCallback sc = new SearchCallback(new StringBuffer(), new StringBuffer(), new HashMap<String, Object>());
        SearchableUtils.iterateAnnotatedMethods(bad, sc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNestedNestedAndFields() throws Exception {
        class BadInnerClass {
            @Searchable(nested = true, fields={"foo"})
            public NestedInner getNestedInner() {
                return new NestedInner();
            }
        }

        class BadClass {
            @Searchable(nested = true)
            public BadInnerClass getBadInnerClass() {
                return new BadInnerClass();
            }
        }

        BadClass bad = new BadClass();
        SearchCallback sc = new SearchCallback(new StringBuffer(), new StringBuffer(), new HashMap<String, Object>());
        SearchableUtils.iterateAnnotatedMethods(bad, sc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNestedCollectionNestedAndFields() throws Exception {
        class BadInnerClass {
            @Searchable(nested = true, fields={"foo"})
            public NestedInner getNestedInner() {
                return new NestedInner();
            }
        }

        class BadClass {
            @Searchable(nested = true)
            public Collection<BadInnerClass> getBadInnerClass() {
                return Collections.singleton(new BadInnerClass());
            }
        }

        BadClass bad = new BadClass();
        SearchCallback sc = new SearchCallback(new StringBuffer(), new StringBuffer(), new HashMap<String, Object>());
        SearchableUtils.iterateAnnotatedMethods(bad, sc);
    }

    @Test
    public void testSimpleNestedWithNoCriteria() throws Exception {
        class InnerNoCriteria {
            private String foo = null;
            @Searchable
            public String getFoo() {
                return foo;
            }

            public void setFoo(String foo) {
                this.foo = foo;
            }

            @Searchable
            public String getBar() {
                return null;
            }

        }

        class OuterNoCriteria {
            private InnerNoCriteria innerNoCriteria = new InnerNoCriteria();
            @Searchable(nested = true)
            public InnerNoCriteria getInnerNoCriteria() {
                return innerNoCriteria;
            }

            @Searchable
            public String getBaz() {
                return null;
            }

            public void setInnerNoCriteria(InnerNoCriteria innerNoCriteria) {
                this.innerNoCriteria = innerNoCriteria;
            }

        }

        OuterNoCriteria o = new OuterNoCriteria();
        assertFalse(SearchableUtils.hasSearchableCriterion(o));

        o.getInnerNoCriteria().setFoo("foo");
        assertTrue(SearchableUtils.hasSearchableCriterion(o));

        o.setInnerNoCriteria(null);
        assertFalse(SearchableUtils.hasSearchableCriterion(o));
    }

    @Test
    public void testNestedCollectionWithNoCriteria() throws Exception {
        class InnerNoCriteria {
            private String foo = null;
            @Searchable
            public String getFoo() {
                return foo;
            }

            public void setFoo(String foo) {
                this.foo = foo;
            }

            @Searchable
            public String getBar() {
                return null;
            }

        }

        class OuterNoCriteria {
            private Set<InnerNoCriteria> innerNoCriteria = new HashSet<InnerNoCriteria>();
            {
                innerNoCriteria.add(new InnerNoCriteria());
                innerNoCriteria.add(new InnerNoCriteria());
                innerNoCriteria.add(new InnerNoCriteria());
            }
            @Searchable(nested = true)
            public Set<InnerNoCriteria> getInnerNoCriteria() {
                return innerNoCriteria;
            }

            public void setInnerNoCriteria(Set<InnerNoCriteria> innerNoCriteria) {
                this.innerNoCriteria = innerNoCriteria;
            }

            @Searchable
            public String getBaz() {
                return null;
            }
        }

        OuterNoCriteria o = new OuterNoCriteria();
        assertFalse(SearchableUtils.hasSearchableCriterion(o));

        Iterator<InnerNoCriteria> iterator = o.getInnerNoCriteria().iterator();
        iterator.next();
        // set a criterion in the middle of the collection
        iterator.next().setFoo("foo");
        assertTrue(SearchableUtils.hasSearchableCriterion(o));

        o.getInnerNoCriteria().clear();
        assertFalse(SearchableUtils.hasSearchableCriterion(o));

        o.setInnerNoCriteria(null);
        assertFalse(SearchableUtils.hasSearchableCriterion(o));
    }

    @Test
    public void testSimpleNested() throws Exception {
        NestedOuter o = new NestedOuter();
        assertTrue(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(o, sc);
        assertTrue(whereClause.length() > 0);
        assertTrue(whereClause.indexOf("lower(obj_nestedInner.foo)") > 0);
        assertEquals(-1, whereClause.indexOf("lower(obj_nestedInner.bar)"));
        assertTrue(whereClause.indexOf("lower(obj.foo)") > 0);
        assertTrue(whereClause.indexOf("lower(obj_nestedInner_nestedNestedInner.qux)") > 0);
        assertTrue(whereClause.indexOf("like :obj_nestedInner_foo") > 0);
        assertTrue(whereClause.indexOf("= :obj_nestedInner_bar") > 0);
        assertTrue(whereClause.indexOf("= :obj_foo") > 0);
        assertTrue(whereClause.indexOf("= :obj_nestedInner_nestedNestedInner_qux") > 0);


        assertTrue(selectClause.indexOf("join obj.nestedInner obj_nestedInner") > 0);
        assertTrue(selectClause.indexOf("join obj_nestedInner.nestedNestedInner obj_nestedInner_nestedNestedInner") > 0);
        assertEquals(params.size(), 4);
        assertEquals("foo", params.get("obj_foo"));
        assertEquals("bar", params.get("obj_nestedInner_bar"));
        assertEquals("%inner foo%", params.get("obj_nestedInner_foo"));
        assertEquals("qux", params.get("obj_nestedInner_nestedNestedInner_qux"));
    }

    @Test
    public void testNestedCollections() throws Exception {
        class NestedNestedNestedCollectionInner {
            String suffix = "";
            public NestedNestedNestedCollectionInner() {
            }

            public NestedNestedNestedCollectionInner(String suffix) {
                this.suffix = suffix;
            }

            @Searchable(matchMode = Searchable.MATCH_MODE_START)
            public String getFoo() {
                return "foo" + suffix;
            }
        }

        class NestedNestedCollectionInner {
            @Searchable
            public String getBaz() {
                return "baz";
            }

            @Searchable
            public String getQux() {
                return "qux";
            }

            @Searchable(nested = true)
            public Collection<NestedNestedNestedCollectionInner> getNestedNestedNestedCollectionInner() {
                Set<NestedNestedNestedCollectionInner> set = new HashSet<NestedNestedNestedCollectionInner>();
                set.add(new NestedNestedNestedCollectionInner("1"));
                set.add(new NestedNestedNestedCollectionInner("2"));
                return set;
            }
        }

        class NestedCollectionInner {
            @Searchable(matchMode = Searchable.MATCH_MODE_CONTAINS)
            public String getFoo() {
                return "inner FOO";
            }

            @Searchable(caseSensitive = true)
            public String getBar() {
                return "bAr";
            }

            @Searchable(fields = {"foo", "bar"})
            public NestedInner getNestedInner() {
                return new NestedInner();
            }

            @Searchable(nested = true)
            public NestedNestedCollectionInner getNestedNestedCollectionInner() {
                return new NestedNestedCollectionInner();
            }

            @Searchable(nested = true)
            public Collection<NestedNestedCollectionInner> getNestedNestedCollectionInnerCollection() {
                return Collections.singleton(new NestedNestedCollectionInner());
            }

            @Searchable(fields = {"baz"})
            public Collection<NestedNestedCollectionInner> getNestedNestedCollectionInnerCollFields() {
                return Collections.singleton(new NestedNestedCollectionInner());
            }

        }

        class NestedCollectionOuter {
            @Searchable(nested = true)
            public Set<NestedCollectionInner> getNestedCollectionInner() {
                return Collections.singleton(new NestedCollectionInner());
            }

            @Searchable
            public String getFoo() {
                return "foo";
            }
        }

        NestedCollectionOuter o = new NestedCollectionOuter();
        assertTrue(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(o, sc);
        assertTrue(whereClause.length() > 0);
        assertEquals(params.size(), 14);

        assertStringExistsOnce(whereClause, "lower(obj.foo)");
        assertStringExistsOnce(whereClause, "= :obj_foo");
        assertEquals("foo", params.get("obj_foo"));

        assertStringExistsOnce(selectClause, "join obj.nestedCollectionInner obj_nestedCollectionInner");
        assertStringExistsOnce(whereClause, " lower(obj_nestedCollectionInner.foo) ");
        assertStringExistsOnce(whereClause, " like :obj_nestedCollectionInner_foo_0 ");
        assertEquals("%inner foo%", params.get("obj_nestedCollectionInner_foo_0"));
        assertStringExistsOnce(whereClause, " obj_nestedCollectionInner.bar ");
        assertStringExistsOnce(whereClause, " in (:obj_nestedCollectionInner_bar) ");
        assertEqualsInCollection("bAr", (Collection<?>) params.get("obj_nestedCollectionInner_bar"));

        assertStringExistsOnce(selectClause, "join obj_nestedCollectionInner.nestedInner obj_nestedCollectionInner_nestedInner");
        assertStringExistsOnce(whereClause, " lower(obj_nestedCollectionInner_nestedInner.foo) ");
        assertStringExistsOnce(whereClause, " in (:obj_nestedCollectionInner_nestedInner_foo) ");
        assertEqualsInCollection("inner foo", (Collection<?>) params.get("obj_nestedCollectionInner_nestedInner_foo"));
        assertStringExistsOnce(whereClause, " lower(obj_nestedCollectionInner_nestedInner.bar) ");
        assertStringExistsOnce(whereClause, " in (:obj_nestedCollectionInner_nestedInner_bar) ");
        assertEqualsInCollection("bar", (Collection<?>) params.get("obj_nestedCollectionInner_nestedInner_bar"));
        assertEquals(-1, selectClause.indexOf("obj_nestedCollectionInner_nestedInner_nestedNestedInner"));

        // NestedCollectionInner.getNestedNestedCollectionInner()
        assertStringExistsOnce(selectClause, "join obj_nestedCollectionInner.nestedNestedCollectionInner obj_nestedCollectionInner_nestedNestedCollectionInner");
        assertStringExistsOnce(whereClause, " lower(obj_nestedCollectionInner_nestedNestedCollectionInner.baz) ");
        assertStringExistsOnce(whereClause, " in (:obj_nestedCollectionInner_nestedNestedCollectionInner_baz) ");
        assertEqualsInCollection("baz", (Collection<?>) params.get("obj_nestedCollectionInner_nestedNestedCollectionInner_baz"));
        assertStringExistsOnce(whereClause, " lower(obj_nestedCollectionInner_nestedNestedCollectionInner.qux) ");
        assertStringExistsOnce(whereClause, " in (:obj_nestedCollectionInner_nestedNestedCollectionInner_qux) ");
        assertEqualsInCollection("qux", (Collection<?>) params.get("obj_nestedCollectionInner_nestedNestedCollectionInner_qux"));

        assertStringExistsOnce(selectClause, "join obj_nestedCollectionInner_nestedNestedCollectionInner.nestedNestedNestedCollectionInner obj_nestedCollectionInner_nestedNestedCollectionInner_nestedNestedNestedCollectionInner");
        assertStringExistsOnce(whereClause, " ( lower(obj_nestedCollectionInner_nestedNestedCollectionInner_nestedNestedNestedCollectionInner.foo) like :obj_nestedCollectionInner_nestedNestedCollectionInner_nestedNestedNestedCollectionInner_foo_0 ");
        assertStringExistsOnce(whereClause, "   OR   lower(obj_nestedCollectionInner_nestedNestedCollectionInner_nestedNestedNestedCollectionInner.foo) like :obj_nestedCollectionInner_nestedNestedCollectionInner_nestedNestedNestedCollectionInner_foo_1 )");
        Object foo0 = params.get("obj_nestedCollectionInner_nestedNestedCollectionInner_nestedNestedNestedCollectionInner_foo_0");
        Object foo1 = params.get("obj_nestedCollectionInner_nestedNestedCollectionInner_nestedNestedNestedCollectionInner_foo_1");
        assertEqualsXor("foo1%", "foo2%", foo0, foo1);
        // end NestedCollectionInner.getNestedNestedCollectionInner()

        // NestedCollectionInner.getNestedNestedCollectionInnerCollection
        assertStringExistsOnce(selectClause, "join obj_nestedCollectionInner.nestedNestedCollectionInnerCollection obj_nestedCollectionInner_nestedNestedCollectionInnerCollection");
        assertStringExistsOnce(whereClause, " lower(obj_nestedCollectionInner_nestedNestedCollectionInnerCollection.baz) ");
        assertStringExistsOnce(whereClause, " in (:obj_nestedCollectionInner_nestedNestedCollectionInnerCollection_baz) ");
        assertEqualsInCollection("baz", (Collection<?>) params.get("obj_nestedCollectionInner_nestedNestedCollectionInnerCollection_baz"));
        assertStringExistsOnce(whereClause, " lower(obj_nestedCollectionInner_nestedNestedCollectionInnerCollection.qux) ");
        assertStringExistsOnce(whereClause, " in (:obj_nestedCollectionInner_nestedNestedCollectionInnerCollection_qux) ");
        assertEqualsInCollection("qux", (Collection<?>) params.get("obj_nestedCollectionInner_nestedNestedCollectionInnerCollection_qux"));

        assertStringExistsOnce(selectClause, "join obj_nestedCollectionInner_nestedNestedCollectionInnerCollection.nestedNestedNestedCollectionInner obj_nestedCollectionInner_nestedNestedCollectionInnerCollection_nestedNestedNestedCollectionInner");
        assertStringExistsOnce(whereClause, " ( lower(obj_nestedCollectionInner_nestedNestedCollectionInnerCollection_nestedNestedNestedCollectionInner.foo) like :obj_nestedCollectionInner_nestedNestedCollectionInnerCollection_nestedNestedNestedCollectionInner_foo_0 ");
        assertStringExistsOnce(whereClause, "   OR   lower(obj_nestedCollectionInner_nestedNestedCollectionInnerCollection_nestedNestedNestedCollectionInner.foo) like :obj_nestedCollectionInner_nestedNestedCollectionInnerCollection_nestedNestedNestedCollectionInner_foo_1 )");
        foo0 = params.get("obj_nestedCollectionInner_nestedNestedCollectionInnerCollection_nestedNestedNestedCollectionInner_foo_0");
        foo1 = params.get("obj_nestedCollectionInner_nestedNestedCollectionInnerCollection_nestedNestedNestedCollectionInner_foo_1");
        assertEqualsXor("foo1%", "foo2%", foo0, foo1);

        // NestedCollectionInner.getNestedNestedCollectionInnerCollFields - only "baz" should be included
        assertStringExistsOnce(selectClause, "join obj_nestedCollectionInner.nestedNestedCollectionInnerCollFields obj_nestedCollectionInner_nestedNestedCollectionInnerCollFields");
        assertStringExistsOnce(whereClause, " lower(obj_nestedCollectionInner_nestedNestedCollectionInnerCollFields.baz) ");
        assertStringExistsOnce(whereClause, " in (:obj_nestedCollectionInner_nestedNestedCollectionInnerCollFields_baz) ");
        assertEqualsInCollection("baz", (Collection<?>) params.get("obj_nestedCollectionInner_nestedNestedCollectionInnerCollFields_baz"));
        assertEquals(-1, whereClause.indexOf(" lower(obj_nestedCollectionInner_nestedNestedCollectionInnerCollFields.qux) "));
        assertEquals(-1, whereClause.indexOf(" in (:obj_nestedCollectionInner_nestedNestedCollectionInnerCollFields_qux) "));
        assertNull(params.get("obj_nestedCollectionInner_nestedNestedCollectionInnerCollFields_qux"));

        assertEquals(-1, whereClause.indexOf("join obj_nestedCollectionInner_nestedNestedCollectionInnerCollFields.nestedNestedNestedCollectionInner obj_nestedCollectionInner_nestedNestedCollectionInnerCollFields_nestedNestedNestedCollectionInner"));
        assertEquals(-1, whereClause.indexOf(" ( lower(obj_nestedCollectionInner_nestedNestedCollectionInnerCollFields_nestedNestedNestedCollectionInner.foo) like :obj_nestedCollectionInner_nestedNestedCollectionInnerCollFields_nestedNestedNestedCollectionInner_foo_0 "));
        assertNull(params.get("obj_nestedCollectionInner_nestedNestedCollectionInnerCollFields_nestedNestedNestedCollectionInner_foo_0"));
        assertEquals(-1, whereClause.indexOf("   OR   lower(obj_nestedCollectionInner_nestedNestedCollectionInnerCollFields_nestedNestedNestedCollectionInner.foo) like :obj_nestedCollectionInner_nestedNestedCollectionInnerCollFields_nestedNestedNestedCollectionInner_foo_1 )"));
        assertNull(params.get("obj_nestedCollectionInner_nestedNestedCollectionInnerCollFields_nestedNestedNestedCollectionInner_foo_1"));
        // end NestedCollectionInner.getNestedNestedCollectionInnerCollFields
    }

    private void assertEqualsXor(String expected1, String expected2, Object object1, Object object2) {
        assertTrue(expected1.equals(object1) || expected1.equals(object2));
        assertTrue(expected2.equals(object1) || expected2.equals(object2));
    }

    private static void assertStringExistsOnce(StringBuffer source, String subString) {
        int indexOf = source.indexOf(subString);
        assertTrue(indexOf >= 0);
        assertEquals(-1, source.indexOf(subString, indexOf + 1));
    }

    private static void assertEqualsInCollection(Object expected, Collection<?> values) {
        assertEquals(1, values.size());
        assertEquals(expected, values.iterator().next());
    }

    // depending on the memory configuration when the test is run, you may get an OutOfMemoryError or a
    // StackOverflowError, both of which extend VirtualMachineError
    @Test(expected = VirtualMachineError.class)
    public void testInfiniteNested() throws Exception {
        NestedInner o = new InfiniteDepthNestedInner();
        assertTrue(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        SearchCallback sc = new SearchCallback(whereClause, new StringBuffer(), new HashMap<String, Object>());
        SearchableUtils.iterateAnnotatedMethods(o, sc);
    }


    // depending on the memory configuration when the test is run, you may get an OutOfMemoryError or a
    // StackOverflowError, both of which extend VirtualMachineError
    @Test(expected = VirtualMachineError.class)
    public void testInfiniteNestedCollection() throws Exception {
        InfiniteNestedCollectionA o = new InfiniteNestedCollectionA();
        assertTrue(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        SearchCallback sc = new SearchCallback(whereClause, new StringBuffer(), new HashMap<String, Object>());
        SearchableUtils.iterateAnnotatedMethods(o, sc);
    }

    class InfiniteNestedCollectionA {
        @Searchable(nested = true)
        public Collection<? extends Object> getInfiniteNestedCollectionB() {
            return Collections.singleton(new InfiniteNestedCollectionB());
        }

        @Searchable
        public String getFoo() {
            return "foo";
        }
    }

    class InfiniteNestedCollectionB {
        @Searchable(nested = true)
        public InfiniteNestedCollectionA getInfiniteNestedCollectionA() {
            return new InfiniteNestedCollectionA();
        }

        @Searchable
        public String getFoo() {
            return "foo";
        }
    }


    @Test
    public void testInfiniteNestedCollectionIsTerminatedInCollection() throws Exception {
        class MyFiniteNestedCollectionA {
            Set<? extends Object> coll;
            @Searchable(nested = true)
            public Collection<? extends Object> getMyInfiniteNestedCollectionB() {
                return coll;
            }

            @Searchable
            public String getFoo() {
                return "foo";
            }
        }

        class MyFiniteNestedCollectionB {
            @Searchable(nested = true)
            public MyFiniteNestedCollectionA getInfiniteNestedCollectionA() {
                return new MyFiniteNestedCollectionA();
            }

            @Searchable
            public String getFoo() {
                return "foo";
            }
        }

        MyFiniteNestedCollectionA o = new MyFiniteNestedCollectionA();
        o.coll = Collections.singleton(new MyFiniteNestedCollectionB());
        assertTrue(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        SearchCallback sc = new SearchCallback(whereClause, new StringBuffer(), new HashMap<String, Object>());
        SearchableUtils.iterateAnnotatedMethods(o, sc);
    }

    @Test
    public void testInfiniteNestedCollectionIsTerminatedOutOfCollection() throws Exception {
        FiniteNestedCollectionA o = new FiniteNestedCollectionA();
        assertTrue(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        SearchCallback sc = new SearchCallback(whereClause, new StringBuffer(), new HashMap<String, Object>());
        SearchableUtils.iterateAnnotatedMethods(o, sc);
    }

    static class FiniteNestedCollectionA {
        @Searchable(nested = true)
        public Collection<? extends Object> getInfiniteNestedCollectionB() {
            return Collections.singleton(new FiniteNestedCollectionB());
        }

        @Searchable
        public String getFoo() {
            return "foo";
        }
    }

    static FiniteNestedCollectionA infiniteNestedCollectionA = new FiniteNestedCollectionA();

    static class FiniteNestedCollectionB {
        @Searchable(nested = true)
        public Object getInfiniteNestedCollectionA() {
            return infiniteNestedCollectionA;
        }

        @Searchable
        public String getFoo() {
            return "foo";
        }
    }


    @Test
    public void testGetQuery() throws Exception {
        // test creating query w/out order by clause
        SearchableUtils.setCallback(new SearchableUtils.QueryCallback() {
            @Override
            public Query doQueryInteraction(Session session, Map<String, Object> params, StringBuffer selectClause) {
                assertNull(session);
                assertTrue(params.size() > 0);
                assertTrue(selectClause.toString().contains("DISTINCT obj FROM"));
                assertTrue(selectClause.toString().contains(Outer3.class.getName()));
                assertFalse(selectClause.toString().contains("COUNT"));
                return null;
            }
        });
        SearchableUtils.getQueryBySearchableFields(new Outer3(), false, null, null);

        // test create count query w/ order by clause
        SearchableUtils.setCallback(new SearchableUtils.QueryCallback() {
            @Override
            public Query doQueryInteraction(Session session, Map<String, Object> params, StringBuffer selectClause) {
                assertNull(session);
                assertTrue(params.size() > 0);
                assertTrue(selectClause.toString().contains("COUNT (DISTINCT obj) FROM"));
                assertTrue(selectClause.toString().contains(Outer3.class.getName()));
                assertTrue(selectClause.toString().contains("COUNT"));
                assertTrue(selectClause.toString().contains("foobar"));
                return null;
            }
        });
        SearchableUtils.getQueryBySearchableFields(new Outer3(), true, "foobar", null);

        // rest create query w/ order by clause, resulting in addition of sortfield to select clause
        SearchableUtils.setCallback(new SearchableUtils.QueryCallback() {
            @Override
            public Query doQueryInteraction(Session session, Map<String, Object> params, StringBuffer selectClause) {
                assertNull(session);
                assertTrue(params.size() > 0);
                assertTrue(selectClause.toString().contains("DISTINCT obj, foobar FROM"));
                assertTrue(selectClause.toString().contains(Outer3.class.getName()));
                assertFalse(selectClause.toString().contains("COUNT"));
                assertTrue(selectClause.toString().contains("foobar"));
                return null;
            }
        });
        SearchableUtils.getQueryBySearchableFields(new Outer3(), false, "foobar", null);

        // test create query with multiple order by clauses and join
        SearchableUtils.setCallback(new SearchableUtils.QueryCallback() {
            @Override
            public Query doQueryInteraction(Session session, Map<String, Object> params, StringBuffer selectClause) {
                assertNull(session);
                assertTrue(params.size() > 0);
                assertTrue(selectClause.toString().contains("DISTINCT obj, f.id , foobar.name FROM"));
                assertTrue(selectClause.toString().contains(Outer3.class.getName()));
                assertTrue(selectClause.toString().contains(" LEFT JOIN foobar f"));
                assertTrue(selectClause.toString().contains(" ORDER BY f.id ASC, foobar.name DESC"));
                return null;
            }
        });
        SearchableUtils.getQueryBySearchableFields(new Outer3(), false, " ORDER BY f.id ASC, foobar.name DESC",
                " LEFT JOIN foobar f",  null, null);
        
        //test create query with group by clause and mimum count threshold
        SearchableUtils.setCallback(new SearchableUtils.QueryCallback() {
            @Override
            public Query doQueryInteraction(Session session, Map<String, Object> params, StringBuffer selectClause) {
                assertNull(session);
                assertTrue(params.size() > 0);
                assertTrue(selectClause.toString().contains(Outer3.class.getName()));
                assertTrue(selectClause.toString().contains(" GROUP BY obj.foo HAVING COUNT(obj) > 5"));
                return null;
            }
        });
        SearchableUtils.getQueryBySearchableFields(new Outer3(), false, null,
                "GROUP BY obj.foo",  null, null, null, 5, SearchableUtils.GREATER_THAN);
        
        // test create query with explicitly specified object attributes
        SearchableUtils.setCallback(new SearchableUtils.QueryCallback() {
            @Override
            public Query doQueryInteraction(Session session, Map<String, Object> params, StringBuffer selectClause) {
                assertNull(session);
                assertTrue(params.size() > 0);
                assertTrue(selectClause.toString().contains("SELECT DISTINCT obj.firstName , obj.lastName, f.id , foobar.name FROM"));
                assertTrue(selectClause.toString().contains(Outer3.class.getName()));
                assertTrue(selectClause.toString().contains(" LEFT JOIN foobar f"));
                assertTrue(selectClause.toString().contains(" ORDER BY f.id ASC, foobar.name DESC"));
                return null;
            }
        });
        SearchableUtils.getQueryBySearchableFields(new Outer3(),
                Arrays.asList(new String[] { "firstName", "lastName" }), false,
                " ORDER BY f.id ASC, foobar.name DESC", "",
                " LEFT JOIN foobar f", null, null);
        
        SearchableUtils.setCallback(new SearchableUtils.QueryCallback() {
            @Override
            public Query doQueryInteraction(Session session, Map<String, Object> params, StringBuffer selectClause) {
                assertNull(session);
                assertTrue(params.size() > 0);
                assertTrue(selectClause.toString().contains("SELECT DISTINCT obj.firstName , obj.lastName FROM"));
                assertTrue(selectClause.toString().contains(Outer3.class.getName()+" obj "));
                assertTrue(selectClause.toString().contains(" LEFT JOIN foobar f"));                
                return null;
            }
        });
        SearchableUtils.getQueryBySearchableFields(new Outer3(),
                Arrays.asList(new String[] { "firstName", "lastName" }), false,
                "", "",
                " LEFT JOIN foobar f", null, null);
        
        
    }

    @Test
    public void testOmitJoinClause() throws Exception {
        Outer5 o = new Outer5();

        assertTrue(SearchableUtils.hasSearchableCriterion(o));
        StringBuffer whereClause = new StringBuffer();
        StringBuffer selectClause = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchCallback sc = new SearchCallback(whereClause, selectClause, params);
        SearchableUtils.iterateAnnotatedMethods(o, sc);
        assertEquals(-1, selectClause.indexOf("obj.f"));


        assertTrue(whereClause.length() > 0);
        assertTrue(whereClause.indexOf("lower(obj.f.foo)") > 0);
        assertTrue(whereClause.indexOf("= :obj_f_foo") > 0);


        assertEquals(params.size(), 1);
        assertEquals("test", params.get("obj_f_foo"));
    }


    public static class Outer1 {
        @Searchable(fields = "foo")
        public Object getA() {
            return new A();
        }
        @Searchable(fields = "foo")
        public Object getB() {
            return new B();
        }
        @Searchable(fields = "foo")
        public Object getC() {
            return new C();
        }
        @Searchable(fields = "foo")
        public Object getD() {
            return new D();
        }
    }

    public static class Outer2 {
        @Searchable(fields = "foo")
        public E getE() {
            return new E();
        }
    }

    public static class Outer2Nested {
        private E e = new E();
        private Collection<E> es = Collections.singleton(new E());
        @Searchable(nested = true)
        public E getE() {
            return e;
        }

        @Searchable(nested = true)
        public Collection<E> getEs() {
            return es;
        }
    }

    public static class Outer3 {
        @Searchable(fields = "foo", matchMode = Searchable.MATCH_MODE_START)
        public Object getF() {
            return new F();
        }
    }

    public static class Outer4 {
        @Searchable(fields = "foo", matchMode = Searchable.MATCH_MODE_CONTAINS, caseSensitive = true)
        public Object getF() {
            return new F();
        }
    }

    public static class Outer5 {
        @Searchable(fields = "foo", isHibernateComponent = true)
        public F getF() {
            return new F();
        }
    }

    public static class Outer6 {
        @Searchable(nested = true)
        public G getG() {
            return new G();
        }
    }


    public static class A {
        public Object getFoo() {
            return null;
        }
        public void setFoo(Object foo) {}
    }

    public static class B {
        public Object getFoo() {
            return new PersistentObject() {
                private static final long serialVersionUID = 1L;
                public Long getId() {
                    return null;
                }
            };
        }
        public void setFoo(Object foo) {}
    }

    public static class C {
        public Object getFoo() {
            return "";
        }
        public void setFoo(Object foo) {}
    }

    public static class D {
        public Object getFoo() {
            return " \t";
        }
        public void setFoo(Object o) {}
    }

    public static class E {
        public static class EResult implements PersistentObject {
            private static final long serialVersionUID = 1L;
            private Long id = 1L;
            private String string = "a string";
            private String anotherString = "another string";
            public Long getId() {
                return id;
            }

            @Searchable(matchMode = Searchable.MATCH_MODE_CONTAINS)
            public String getString() {
                return string;
            }

            @Searchable(caseSensitive = true)
            public String getAnotherString() {
                return anotherString;
            }

        };
        private EResult result = new EResult();
        @Searchable(nested = true)
        public EResult getFoo() {
            return result;
        }
        public void setFoo(EResult foo) {}
    }

    public static class F {
        @Searchable
        public Object getFoo() {
            return "test";
        }
        public void setFoo(Object o) {}
    }

    public static class G {
        @Searchable(fields = "foo", isHibernateComponent = true)
        public F getBoo() {
            return new F();
        }
        public void setBoo(Object o) {}

    }

    public static class NestedOuter {
        @Searchable(nested = true)
        public NestedInner getNestedInner() {
            return nestedInner;
        }

        @Searchable
        public String getFoo() {
            return "foo";
        }
    }

    // SearchCallback checks for loops in nested objects using ==, so instantiate a specific instance of nestedInner
    // to create such a loop.
    private static NestedInner nestedInner = new NestedInner();

    public static class NestedInner {
        @Searchable(matchMode = Searchable.MATCH_MODE_CONTAINS)
        public String getFoo() {
            return "inner foo";
        }

        @Searchable(caseSensitive = true)
        public String getBar() {
            return "bar";
        }

        @Searchable(nested = true)
        public NestedNestedInner getNestedNestedInner() {
            return new NestedNestedInner();
        }

    }

    public static class NestedNestedInner {
        @Searchable
        public String getQux() {
            return "qux";
        }

        @Searchable(nested = true)
        public NestedInner getNestedInner() {
            return nestedInner;
        }
    }

    // Since SearchCallback uses == to detect loops, create an object loop using new instances that aren't ==.
    // This loop should cause SearchCallback to recurse endlessly, leading to an out of memory error
    public static class InfiniteDepthNestedInner extends NestedInner {
        @Override
        @Searchable(nested = true)
        public NestedNestedInner getNestedNestedInner() {
            return new InfiniteDepthNestedNestedInner();
        }

    }

    public static class InfiniteDepthNestedNestedInner extends NestedNestedInner{
        @Override
        @Searchable(nested = true)
        public NestedInner getNestedInner() {
            return new InfiniteDepthNestedInner();
        }
    }
}
