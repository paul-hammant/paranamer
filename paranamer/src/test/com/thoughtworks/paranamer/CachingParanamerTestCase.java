package com.thoughtworks.paranamer;

import java.lang.reflect.Method;
import java.lang.reflect.AccessibleObject;
import java.util.Arrays;

import junit.framework.TestCase;

public class CachingParanamerTestCase extends TestCase {
    private Paranamer paranamer;
    private int count = 0;

    protected void setUp() throws Exception {
        paranamer = new Paranamer() {

            public String[] lookupParameterNames(AccessibleObject methodOrCtor) {
                count++;
                return new String[]{"foo","bar"};
            }

            public int areParameterNamesAvailable(Class clazz, String ctorOrMethodName) {
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
