package com.thoughtworks.paranamer;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

public class UncheckedParanamerTestCase extends AbstractParanamerTestCase {

    public void testUncheckedMethodRetrievalFailure() throws IOException {
        try {
            new UncheckedParanamer().uncheckedMethodLookup(Paranamer.class.getClassLoader(), "com.thoughtworks.paranamer.ParanamerGeneration", "generate", "hello,goodbye");
            fail("shoulda barfed");
        } catch (ParanamerRuntimeException e) {
            // expected
        }
    }

    public void testMethodCanBeRetrievedByParameterNamesViaUnCheckedLookup() throws IOException, NoSuchMethodException, ParanamerException {
        Method method = new UncheckedParanamer().uncheckedMethodLookup(Paranamer.class.getClassLoader(), "com.thoughtworks.paranamer.ParanamerImpl", "lookupMethod", "classLoader,className,methodName,paramNames");
        assertEquals(ParanamerImpl.class.getMethod("lookupMethod", new Class[]{ClassLoader.class, String.class, String.class, String.class}), method);
    }

    public void testCheckedConstructorRetrievalFailure() throws IOException {
        try {
            new UncheckedParanamer().uncheckedConstructorLookup(Paranamer.class.getClassLoader(), "com.thoughtworks.paranamer.ParanamerException","sdsdsdsd");
            fail("shoulda barfed");
        } catch (ParanamerRuntimeException e) {
            // expected
        }
    }

    public void testConstructorCanBeRetrievedByParameterNamesViaCheckedLookup() throws IOException, NoSuchMethodException, ParanamerException {
        Constructor ctor = new UncheckedParanamer().uncheckedConstructorLookup(Paranamer.class.getClassLoader(), "com.thoughtworks.paranamer.ParanamerException", "message");
        assertEquals(ParanamerException.class.getConstructor(new Class[]{String.class}), ctor);
    }

}
