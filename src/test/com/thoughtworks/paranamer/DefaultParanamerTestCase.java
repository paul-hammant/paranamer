package com.thoughtworks.paranamer;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

public class DefaultParanamerTestCase extends TestCase {

    private DefaultParanamer paranamer;
    
    protected void setUp() throws Exception {
        paranamer = new DefaultParanamer();
    }

    public void testLookupMethodReturnsNullIfMethodNotFound()
            throws IOException {
        Object method = paranamer.lookupMethod(
                Paranamer.class.getClassLoader(),
                "com.thoughtworks.paranamer.QdoxParanamerGenerator",
                "generate", "hello,goodbye");
        assertNull(method);
    }

    public void testMLookupMethodReturnsNullIfClassNotFound()
            throws IOException {
        Object method = paranamer.lookupMethod(
                Paranamer.class.getClassLoader(), "paranamer.Footle",
                "generate", "sourcePath,rootPackage");
        assertNull(method);
    }

    public void testLookupMethodEndsWithUnknownClass() throws IOException {
        Object method = paranamer.lookupMethod(
                Paranamer.class.getClassLoader(), "foo.Bar", "generate",
                "hello,goodbye");
        assertNull(method);
    }

    public void testLookupFailsIfResourceMissing() throws IOException {
        Paranamer paranamer = new DefaultParanamer("/inexistent/resource");
        try {
            paranamer.lookupMethod(Paranamer.class.getClassLoader(),
                    "com.thoughtworks.paranamer.QdoxParanamerGenerator",
                    "generate", "sourcePath,rootPackage");
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    public void testMethodWithNoArgsCanBeRetrievedByParameterNames()
            throws IOException, NoSuchMethodException {
        Method method = paranamer.lookupMethod(
                Paranamer.class.getClassLoader(),
                "com.thoughtworks.paranamer.ParanamerGeneratorMojo", "execute", "");
        assertEquals(ParanamerGeneratorMojo.class.getMethod("execute", new Class[0]),
                method);
    }

    public void testMethodWithNoArgsCanBeRetrievedAndShowNoParameterNames()
            throws IOException, NoSuchMethodException {
        String[] choices = paranamer.lookupParameterNames(Paranamer.class
                .getClassLoader(), "com.thoughtworks.paranamer.ParanamerGeneratorMojo",
                "execute");
        assertEquals(0, choices.length);
    }


}
