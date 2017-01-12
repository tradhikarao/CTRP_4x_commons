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
package com.fiveamsolutions.nci.commons.util;

import java.io.Serializable;
import java.util.Iterator;

import org.hibernate.EntityMode;
import org.hibernate.Interceptor;
import org.hibernate.Transaction;
import org.hibernate.type.Type;

/**
 * Implements the Composite pattern for hibernate interceptors.  For each interceptor method,
 * composite interceptor calls its children in the order provided upon construction.  For
 * methods that return anything besides void, see the individual method documentation for
 * how the return value is calculated.
 *
 * <p>In the degenerate case of a single child, this class always passes through the parameters
 * without modification and returns the values from the child without modification.
 *
 * @see http://en.wikipedia.org/wiki/Composite_pattern
 */
@SuppressWarnings({"PMD.ExcessiveParameterList", "PMD.TooManyMethods" })
public class CompositeInterceptor implements Interceptor, Serializable {

    private static final long serialVersionUID = 1L;
    private final Interceptor[] children;

    /**
     * Constructs a new composite interceptor, delegating to the provided
     * children in the provided order.
     *
     * @param children the list of children
     */
    public CompositeInterceptor(Interceptor... children) {
        if (children == null || children.length == 0) {
            throw new IllegalArgumentException("must specify at least one child");
        }
        this.children = children;
    }

    /**
     * {@inheritDoc}
     */
    public void afterTransactionBegin(Transaction arg0) {
        for (Interceptor i : children) {
            i.afterTransactionBegin(arg0);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void afterTransactionCompletion(Transaction arg0) {
        for (Interceptor i : children) {
            i.afterTransactionCompletion(arg0);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void beforeTransactionCompletion(Transaction arg0) {
        for (Interceptor i : children) {
            i.beforeTransactionCompletion(arg0);
        }
    }

    /**
     * Calls the children's findDirty methods, in order, looking for the first non-null result.
     * Once a non null result is found, <em>the remaining children's findDirty methods will
     * <b>not</b> be called.</em>
     *
     * @param entity entity
     * @param id id
     * @param currentState currentState
     * @param previousState previousState
     * @param propertyNames propertyNames
     * @param types types
     *
     * @return first non-null result from children, or null if all children returned null
     */
    @SuppressWarnings("PMD.ReturnEmptyArrayRatherThanNull")
    public int[] findDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
                           String[] propertyNames, Type[] types) {
        for (Interceptor i : children) {
            int[] tmp = i.findDirty(entity, id, currentState, previousState, propertyNames, types);
            if (tmp != null) {
                return tmp;
            }
        }
        return null;
    }

    /**
     * Gets the entity from the <em>first</em> child interceptor.
     *
     * @param entityName entity name
     * @param id id
     *
     * @return children[0].getEntity()
     */
    public Object getEntity(String entityName, Serializable id) {
        return children[0].getEntity(entityName, id);
    }

    /**
     * Gets the entity name from the <em>first</em> child interceptor.
     *
     * @param object object
     *
     * @return children[0].getEntityName()
     */
    public String getEntityName(Object object) {
        return children[0].getEntityName(object);
    }

    /**
     * Calls the children, in order, looking for the first non-null return value.  If a non
     * null value is returned, that object is the result of this method, and <em>the remaining
     * children's instantiate methods will not be called.</em>
     *
     * @param entityName entityName
     * @param entityMode entityMode
     * @param id id
     * @return instance from first child to return non-null, or null if all children return null
     */
    public Object instantiate(String entityName, EntityMode entityMode, Serializable id) {
        for (Interceptor i : children) {
            Object tmp = i.instantiate(entityName, entityMode, id);
            if (tmp != null) {
                return tmp;
            }
        }
        return null;
    }

    /**
     * Calls the children, in order, looking for the first non-null return value.  If a non
     * null value is returned, that Boolean is the result of this method, and <em>the remaining
     * children's isTransient methods will not be called.</em>
     *
     * @param entity entity
     * @return isTransient result from first child to return non-null, or null if all children return null
     */
    public Boolean isTransient(Object entity) {
        for (Interceptor i : children) {
            Boolean tmp = i.isTransient(entity);
            if (tmp != null) {
                return tmp;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void onCollectionRecreate(Object arg0, Serializable arg1) {
        for (Interceptor i : children) {
            i.onCollectionRecreate(arg0, arg1);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onCollectionRemove(Object arg0, Serializable arg1) {
        for (Interceptor i : children) {
            i.onCollectionRemove(arg0, arg1);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onCollectionUpdate(Object arg0, Serializable arg1) {
        for (Interceptor i : children) {
            i.onCollectionUpdate(arg0, arg1);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onDelete(Object arg0, Serializable arg1, Object[] arg2, String[] arg3, Type[] arg4) {
        for (Interceptor i : children) {
            i.onDelete(arg0, arg1, arg2, arg3, arg4);
        }
    }

    /**
     * Calls the children's onFlushDirty methods, in order.
     *
     * @param entity entity
     * @param id id
     * @param currentState currentState
     * @param previousState previousState
     * @param propertyNames propertyNames
     * @param types types
     *
     * @return true if <em>any</em> child has modified the state in any way
     */
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
            String[] propertyNames, Type[] types) {
        boolean result = false;
        for (Interceptor i : children) {
            result |= i.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
        }

        return result;
    }

    /**
     * Calls the children's onLoad methods, in order.
     *
     * @param entity entity
     * @param id id
     * @param state state
     * @param propertyNames propertyNames
     * @param types types
     *
     * @return true if <em>any</em> child has modified the state in any way
     */
    public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        boolean result = false;
        for (Interceptor i : children) {
            result |= i.onLoad(entity, id, state, propertyNames, types);
        }

        return result;
    }

    /**
     * Calls the children's onPrepareStatement, in order, chaining the resultant sql.
     * IE, for two children, does the equlivent of
     * <code>return children[1].onPrepareStatement(children[0].onPrepareStatement(arg0));</code>
     *
     * @param sql original sql
     * @return modified sql
     */
    @SuppressWarnings("PMD.UseStringBufferForStringAppends") // PMD incorrectly thinks we're doing concat here
    public String onPrepareStatement(String sql) {
        String result = sql;
        for (Interceptor i : children) {
            result = i.onPrepareStatement(result);
        }
        return result;
    }

    /**
     * Calls the children's onSave methods, in order.
     *
     * @param entity entity
     * @param id id
     * @param state state
     * @param propertyNames propertyNames
     * @param types types
     *
     * @return true if <em>any</em> child has modified the state in any way
     */
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        boolean result = false;
        for (Interceptor i : children) {
            result |= i.onSave(entity, id, state, propertyNames, types);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
    public void postFlush(Iterator arg0) {
        for (Interceptor i : children) {
            i.postFlush(arg0);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
    public void preFlush(Iterator arg0) {
        for (Interceptor i : children) {
            i.preFlush(arg0);
        }
    }

}
