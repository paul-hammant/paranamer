package com.thoughtworks.paranamer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

import junit.framework.TestCase;

public class CachingParanamerTestCase extends TestCase {
    private Paranamer paranamer;
    private int count = 0;

    protected void setUp() throws Exception {
        paranamer = new Paranamer() {

            public String[] lookupParameterNames(Method method) {
                count++;
                return new String[]{"foo","bar"};
            }

            public String[] lookupParameterNames(Constructor constructor) {
                return new String[0];
            }

            public int areParameterNamesAvailable(ClassLoader classLoader, String className, String name) {
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
