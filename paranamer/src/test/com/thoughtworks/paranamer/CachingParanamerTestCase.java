package com.thoughtworks.paranamer;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

import junit.framework.TestCase;

public class CachingParanamerTestCase extends TestCase {
    private Method method;
    private Constructor constructor;
    private Paranamer paranamer;
    private int count = 0;

    protected void setUp() throws Exception {
        method = String.class.getMethod("toString", new Class[0]);
        constructor = String.class.getConstructor(new Class[] {String.class});

        paranamer = new Paranamer() {

            public String[] lookupParameterNames(Method method) {
                count++;
                return new String[]{"foo","bar"};
            }

            public String[] lookupParameterNames(Constructor constructor) {
                return new String[0];
            }

            public int areParameterNamesAvailable(ClassLoader classLoader, String className, String ctorOrMethodName) {
                return -1;  
            }
        };
    }


     public void testLookupOfParameterNamesForMethod() {
        Paranamer cachingParanamer = new CachingParanamer(paranamer);
        String[] paramNames = cachingParanamer.lookupParameterNames((Method)null);
        assertEquals(Arrays.asList(new String[]{"foo","bar"}), Arrays.asList(paramNames));
        assertEquals(1, count);

        // cache hit
        paramNames = cachingParanamer.lookupParameterNames((Method)null);
        assertEquals(Arrays.asList(new String[]{"foo","bar"}), Arrays.asList(paramNames));
        assertEquals(1, count);
    }
    
}
