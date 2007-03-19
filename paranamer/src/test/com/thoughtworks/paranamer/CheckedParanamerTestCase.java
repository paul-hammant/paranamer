package com.thoughtworks.paranamer;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

public class CheckedParanamerTestCase extends AbstractQDoxParanamerTestCase {

    public void testCheckedMethodRetrievalFailure() throws IOException {
        try {
            new CheckedParanamer().checkedMethodLookup(Paranamer.class.getClassLoader(), "com.thoughtworks.paranamer.QdoxParanamerGenerator","generate","hello,goodbye");
            fail("shoulda barfed");
        } catch (ParanamerException e) {
            // expected
        }
    }

    public void testMethodCanBeRetrievedByParameterNamesViaCheckedLookup() throws IOException, NoSuchMethodException, ParanamerException {
        Method method = new CheckedParanamer().checkedMethodLookup(Paranamer.class.getClassLoader(), "com.thoughtworks.paranamer.DefaultParanamer", "lookupMethod", "classLoader,className,methodName,paramNames");
        assertEquals(DefaultParanamer.class.getMethod("lookupMethod", new Class[]{ClassLoader.class, String.class, String.class, String.class}), method);
    }

    public void testCheckedConstructorRetrievalFailure() throws IOException {
        try {
            new CheckedParanamer().checkedConstructorLookup(Paranamer.class.getClassLoader(), "com.thoughtworks.paranamer.ParanamerException","sdsdsdsd");
            fail("shoulda barfed");
        } catch (ParanamerException e) {
            // expected
        }
    }

    public void testConstructorCanBeRetrievedByParameterNamesViaCheckedLookup() throws IOException, NoSuchMethodException, ParanamerException {
        Constructor ctor = new CheckedParanamer().checkedConstructorLookup(Paranamer.class.getClassLoader(), "com.thoughtworks.paranamer.ParanamerException", "message");
        assertEquals(ParanamerException.class.getConstructor(new Class[]{String.class}), ctor);
    }

}
