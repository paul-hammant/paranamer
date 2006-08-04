package com.thoughtworks.paranamer;

import junit.framework.TestCase;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.io.File;
import java.io.IOException;

public class CachingParanamerTestCase extends TestCase {

    Method method;
    Constructor ctor;
    Paranamer paranamer;
    int count = 0;

    protected void setUp() throws Exception {
        method = String.class.getMethod("toString", new Class[0]);
        ctor = String.class.getConstructor(new Class[] {String.class});

        paranamer = new Paranamer() {

            public Method lookupMethod(ClassLoader classLoader, String className, String methodName, String paramNames) {
                count++;
                return method;
            }

            public Constructor lookupConstructor(ClassLoader classLoader, String className, String paramNames) {
                count++;
                return ctor;
            }

            public String[] lookupParameterNames(ClassLoader classLoader, String className, String methodName) {
                return new String[] {"foo,bar"};
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

    public void testMethodNotCachedIfDiffeent() {
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
        assertEquals(ctor, c);
        assertEquals(1, count);
        c = cachingParanamer.lookupConstructor(this.getClass().getClassLoader(), "huey", "luis");
        assertEquals(ctor, c);
        assertEquals(1, count);
    }

    public void testConstructorNotCachedIfDiffeent() {
        Paranamer cachingParanamer = new CachingParanamer(paranamer);
        Constructor c = cachingParanamer.lookupConstructor(this.getClass().getClassLoader(), "huey", "luis");
        assertEquals(ctor, c);
        assertEquals(1, count);
        c = cachingParanamer.lookupConstructor(this.getClass().getClassLoader(), "huey", "horatio");
        assertEquals(ctor, c);
        assertEquals(2, count);
    }

    public void testConstructorForcedNullOnBogusLookup() {
        ctor = null;
        Paranamer cachingParanamer = new CachingParanamer(paranamer);
        Constructor c = cachingParanamer.lookupConstructor(this.getClass().getClassLoader(), "huey", "luis");
        assertNull(c);
    }


    public void testCanChainToDefaultImpl() throws IOException {
        //setup
        QdoxParanamerGenerator generator = new QdoxParanamerGenerator();
        String parameterSignatures = generator.generate(new File(".").getAbsolutePath() + "/src/java");
        generator.write(new File(".").getAbsolutePath() + "/target/test-classes/", parameterSignatures);

        Paranamer cachingParanamer = new CachingParanamer();
        Method m = cachingParanamer.lookupMethod(Paranamer.class.getClassLoader(), "com.thoughtworks.paranamer.DefaultParanamer", "lookupMethod", "classLoader,className,methodName,paramNames");
        assertNotNull(m);
    }

    public void testLookupOfParameterNames() {

        Paranamer cachingParanamer = new CachingParanamer(paranamer);
        String[] paramNameChoices = cachingParanamer.lookupParameterNames(Paranamer.class.getClassLoader(), "com.thoughtworks.paranamer.DefaultParanamer", "lookup");
        assertEquals(1, paramNameChoices.length);
        assertEquals("foo,bar", paramNameChoices[0]);
    }


}
