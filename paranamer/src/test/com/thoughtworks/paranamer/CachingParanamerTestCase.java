package com.thoughtworks.paranamer;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

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

            public Method lookupMethod(ClassLoader classLoader, String className, String methodName, String paramNames) {
                count++;
                return method;
            }

            public Constructor lookupConstructor(ClassLoader classLoader, String className, String paramNames) {
                count++;
                return constructor;
            }

            public String[] lookupParameterNames(ClassLoader classLoader, String className, String methodName) {
                count++;
                return new String[] {"foo,bar"};
            }

            public String lookupParameterNamesForMethod(Method method) {
                count++;
                return "foo,bar";
            }

            public int isParameterNameDataAvailable(ClassLoader classLoader, String className, String ctorOrMethodName) {
                return -1;  
            }
        };
    }

    public void testMethodCachedOnLookup() {
        Paranamer cachingParanamer = new CachingParanamer(paranamer);
        Method m = cachingParanamer.lookupMethod(this.getClass().getClassLoader(), "huey", "duey", "luis");
        assertEquals(method, m);
        assertEquals(1, count);
        m = cachingParanamer.lookupMethod(this.getClass().getClassLoader(), "huey", "duey", "luis");
        assertEquals(method, m);
        assertEquals(1, count);
    }

    public void testMethodNotCachedIfDifferent() {
        Paranamer cachingParanamer = new CachingParanamer(paranamer);
        Method m = cachingParanamer.lookupMethod(this.getClass().getClassLoader(), "huey", "duey", "luis");
        assertEquals(method, m);
        assertEquals(1, count);
        m = cachingParanamer.lookupMethod(this.getClass().getClassLoader(), "huey", "duey", "horatio");
        assertEquals(method, m);
        assertEquals(2, count);
    }

    public void testMethodForcedNullOnBogusLookup() {
        method = null;
        Paranamer cachingParanamer = new CachingParanamer(paranamer);
        Method m = cachingParanamer.lookupMethod(this.getClass().getClassLoader(), "huey", "duey", "luis");
        assertNull(m);
    }

    public void testConstructorCachedOnLookup() {
        Paranamer cachingParanamer = new CachingParanamer(paranamer);
        Constructor c = cachingParanamer.lookupConstructor(this.getClass().getClassLoader(), "huey", "luis");
        assertEquals(constructor, c);
        assertEquals(1, count);
        c = cachingParanamer.lookupConstructor(this.getClass().getClassLoader(), "huey", "luis");
        assertEquals(constructor, c);
        assertEquals(1, count);
    }

    public void testConstructorNotCachedIfDiffeent() {
        Paranamer cachingParanamer = new CachingParanamer(paranamer);
        Constructor c = cachingParanamer.lookupConstructor(this.getClass().getClassLoader(), "huey", "luis");
        assertEquals(constructor, c);
        assertEquals(1, count);
        c = cachingParanamer.lookupConstructor(this.getClass().getClassLoader(), "huey", "horatio");
        assertEquals(constructor, c);
        assertEquals(2, count);
    }

    public void testConstructorForcedNullOnBogusLookup() {
        constructor = null;
        Paranamer cachingParanamer = new CachingParanamer(paranamer);
        Constructor c = cachingParanamer.lookupConstructor(this.getClass().getClassLoader(), "huey", "luis");
        assertNull(c);
    }

    public void testCanChainToDefaultImpl() throws IOException {
//        //setup
//        ParanamerGenerator generator = new QdoxParanamerGenerator();
//        String parameterSignatures = generator.generate(new File(".").getAbsolutePath() + "/src/java");
//        generator.write(new File(".").getAbsolutePath() + "/target/test-classes/", parameterSignatures);

        Paranamer cachingParanamer = new CachingParanamer();
        Method m = cachingParanamer.lookupMethod(Paranamer.class.getClassLoader(), "com.thoughtworks.paranamer.DefaultParanamer", "lookupMethod", "classLoader,className,methodName,paramNames");
        assertNotNull(m);
    }

    public void testLookupOfParameterNames() {
        Paranamer cachingParanamer = new CachingParanamer(paranamer);
        String[] paramNameChoices = cachingParanamer.lookupParameterNames(Paranamer.class.getClassLoader(), "com.thoughtworks.paranamer.DefaultParanamer", "lookup");
        assertEquals(1, paramNameChoices.length);
        assertEquals("foo,bar", paramNameChoices[0]);
        assertEquals(1, count);

        // cache hit
        paramNameChoices = cachingParanamer.lookupParameterNames(Paranamer.class.getClassLoader(), "com.thoughtworks.paranamer.DefaultParanamer", "lookup");
        assertEquals(1, paramNameChoices.length);
        assertEquals("foo,bar", paramNameChoices[0]);
        assertEquals(1, count);
    }

    public void testLookupOfParameterNamesForMethod() {
        Paranamer cachingParanamer = new CachingParanamer(paranamer);
        String paramNames = cachingParanamer.lookupParameterNamesForMethod(null);
        assertEquals("foo,bar", paramNames);
        assertEquals(1, count);

        // cache hit
        paramNames = cachingParanamer.lookupParameterNamesForMethod(null);
        assertEquals("foo,bar", paramNames);
        assertEquals(1, count);
    }
    
}
