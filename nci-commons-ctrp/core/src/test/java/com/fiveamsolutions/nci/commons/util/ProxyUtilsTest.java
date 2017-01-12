package com.fiveamsolutions.nci.commons.util;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.junit.Test;

import com.fiveamsolutions.nci.commons.data.persistent.PersistentObject;
import com.fiveamsolutions.nci.commons.data.security.ApplicationRole;


public class ProxyUtilsTest {
    @Test
    public void unEnhanceCGLIBClass() throws ClassNotFoundException {
        Object enhanced = enhance(TestPO.class);
        Class<?> unEnhanceCGLIBClass = ProxyUtils
                .unEnhanceCGLIBClass(enhanced.getClass());

        assertEquals(TestPO.class.getName(), unEnhanceCGLIBClass.getName());
    }

    @Test
    public void unEnhanceClass() throws ClassNotFoundException {
        Object enhanced = new TestPO();
        Class<?> unEnhanceCGLIBClass = ProxyUtils
                .unEnhanceCGLIBClass(enhanced.getClass());

        assertEquals(TestPO.class.getName(), unEnhanceCGLIBClass.getName());
    }

    @Test
    public void unEnhanceClassName() throws ClassNotFoundException {
        Object enhanced = new TestPO();
        String unEnhanceCGLIBClassName = ProxyUtils
                .unEnhanceCGLIBClassName(enhanced.getClass());

        assertEquals(TestPO.class.getName(), unEnhanceCGLIBClassName);
    }

    @Test
    public void unEnhanceJavassistClassName() throws ClassNotFoundException {
        Object enhanced = new TestProxy();
        Class<?> unEnhanceJavassistClass = ProxyUtils.unEnhanceJavassistClass(enhanced.getClass());
        assertEquals(TestPO.class.getName(), unEnhanceJavassistClass.getName());
    }

    @Test
    public void unEnhanceDirectClassName() throws ClassNotFoundException {
        Object unenhanced = new ApplicationRole();
        Class<?> unEnhanceJavassistClass = ProxyUtils.unEnhanceJavassistClass(unenhanced.getClass());
        assertEquals(ApplicationRole.class.getName(), unEnhanceJavassistClass.getName());
    }

    @Test
    public void unEnhanceAllCGLIBClassName() throws ClassNotFoundException {
        Object unenhanced = new TestPO();
        Class<?> unEnhanceClass = ProxyUtils.unEnhanceClass(unenhanced.getClass());
        assertEquals(TestPO.class.getName(), unEnhanceClass.getName());
    }

    @Test
    public void unEnhanceAllJavassistClassName() throws ClassNotFoundException {
        Object unenhanced = new TestProxy();
        Class<?> unEnhanceClass = ProxyUtils.unEnhanceClass(unenhanced.getClass());
        assertEquals(TestPO.class.getName(), unEnhanceClass.getName());
    }

    @Test
    public void unEnhanceAllDirectClassName() throws ClassNotFoundException {
        Object unenhanced = new ApplicationRole();
        Class<?> unEnhanceClass = ProxyUtils.unEnhanceClass(unenhanced.getClass());
        assertEquals(ApplicationRole.class.getName(), unEnhanceClass.getName());
    }



    private Object enhance(Class<?> class1) {
        Object create = Enhancer.create(class1, new Class[] {}, new MethodInterceptor() {

            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                Object retValFromSuper = null;
                try {
                    if (!Modifier.isAbstract(method.getModifiers())) {
                        retValFromSuper = proxy.invokeSuper(obj, args);
                    }
                } finally {

                }
                return retValFromSuper;
            }
        });
        return create;
    }

    private static class TestPO implements PersistentObject {

        private static final long serialVersionUID = 1L;

        public TestPO() {
            // do nothing
        }

        public Long getId() {
            return null;
        }
    }

    private static class TestProxy extends TestPO implements ProxyObject {

        private static final long serialVersionUID = 1L;

        public TestProxy() {
            // do nothing
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setHandler(MethodHandler arg0) {
            //do nothing
        }
    }
}
