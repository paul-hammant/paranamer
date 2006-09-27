package com.thoughtworks.paranamer;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class UncheckedParanamerTestCase extends AbstractParanamerTestCase {

    public void testUncheckedMethodRetrievalFailure() throws IOException {
        try {
            new UncheckedParanamer()
                    .uncheckedMethodLookup(
                            Paranamer.class.getClassLoader(),
                            "com.thoughtworks.paranamer.QdoxParanamerGenerator",
                            "generate",
                            "hello,goodbye");
            fail("shoulda barfed");
        } catch (ParanamerRuntimeException expected) {
            // expected
        }
    }

    public void testMethodCanBeRetrievedByParameterNamesViaUnCheckedLookup() throws Exception {
        Method method = new UncheckedParanamer()
                .uncheckedMethodLookup(
                        Paranamer.class.getClassLoader(),
                        "com.thoughtworks.paranamer.DefaultParanamer",
                        "lookupMethod",
                        "classLoader,className,methodName,paramNames");

        Class[] parameterTypes = new Class[]{ClassLoader.class, String.class, String.class, String.class};
        assertEquals(DefaultParanamer.class.getMethod("lookupMethod", parameterTypes), method);
    }

    public void testCheckedConstructorRetrievalFailure() throws IOException {
        try {
            new UncheckedParanamer()
                    .uncheckedConstructorLookup(
                            Paranamer.class.getClassLoader(),
                            "com.thoughtworks.paranamer.ParanamerException",
                            "sdsdsdsd");
            fail("shoulda barfed");
        } catch (ParanamerRuntimeException expected) {
            // expected
        }
    }

    public void testConstructorCanBeRetrievedByParameterNamesViaCheckedLookup() throws Exception {
        Constructor ctor = new UncheckedParanamer()
                .uncheckedConstructorLookup(
                        Paranamer.class.getClassLoader(),
                        "com.thoughtworks.paranamer.ParanamerException",
                        "message");
        assertEquals(ParanamerException.class.getConstructor(new Class[]{String.class}), ctor);
    }

}
