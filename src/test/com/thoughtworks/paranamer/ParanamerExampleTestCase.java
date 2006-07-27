package com.thoughtworks.paranamer;

import junit.framework.TestCase;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;


public class ParanamerExampleTestCase extends AbstractParanamerTestCase {

    // An example of a test that looks something up by it's parameter names

    public void testMethodCanBeRetrievedByParameterNames() throws IOException, NoSuchMethodException {
        Method method = new ParanamerImpl().lookupMethod(Paranamer.class.getClassLoader(), "com.thoughtworks.paranamer.ParanamerImpl", "lookupMethod", "classLoader,className,methodName,paramNames");
        assertEquals(ParanamerImpl.class.getMethod("lookupMethod", new Class[]{ClassLoader.class, String.class, String.class, String.class}), method);
    }

    // An example of a test that looks a method up based on class/method only

    public void testParamerNameChoicesCanBeRetrievedForAMethodName() throws IOException, NoSuchMethodException {
        String[] paramNames = new ParanamerImpl().lookupParameterNames(Paranamer.class.getClassLoader(), "com.thoughtworks.paranamer.ParanamerImpl", "lookupMethod");
        assertEquals(2, paramNames.length);
        assertEquals("classLoader,c,m,p", paramNames[0]);
        assertEquals("classLoader,className,methodName,paramNames", paramNames[1]);
    }

    public void testParamerNameChoicesCannotBeRetrievedForAMissingMethodName() throws IOException, NoSuchMethodException {
        String[] paramNames = new ParanamerImpl().lookupParameterNames(Paranamer.class.getClassLoader(), "com.thoughtworks.paranamer.ParanamerImpl", "smdnfmsndf");
        assertEquals(0, paramNames.length);
    }


    // An example of a test that looks a method up by it's OLD parameter names
    // These were encoded via a doclet tag on the method in question:
    //
    //   @previousParamNames clazz,cmapn
    public void testMethodCanBeRetrievedByParameterNamesPreviouslyUsed() throws IOException, NoSuchMethodException {
        Method method = new ParanamerImpl().lookupMethod(Paranamer.class.getClassLoader(), "com.thoughtworks.paranamer.ParanamerImpl", "lookupMethod", "classLoader,c,m,p");
        assertEquals(ParanamerImpl.class.getMethod("lookupMethod", new Class[]{ClassLoader.class, String.class, String.class, String.class}), method);
    }

    // don't forget, you can copy the lookup() code into your project as it's public domain, if you want to use
    // the Paranamer technology without an extra jar.

    // An example of a test that looks a constructor up by it's parameter names

    public void testConstructorCanBeRetrievedByParameterNames() throws IOException, NoSuchMethodException {
        Constructor ctor = new ParanamerImpl().lookupConstructor(Paranamer.class.getClassLoader(), "com.thoughtworks.paranamer.ParanamerException", "message");
        assertEquals(ParanamerException.class.getConstructor(new Class[]{String.class}), ctor);
    }



}
