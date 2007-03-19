package com.thoughtworks.paranamer;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ParanamerExampleTestCase extends AbstractQDoxParanamerTestCase {

    // An example of a test that looks something up by it's parameter names
    public void testMethodCanBeRetrievedByParameterNames() throws IOException, NoSuchMethodException {
        Method method = new DefaultParanamer().lookupMethod(Paranamer.class.getClassLoader(), "com.thoughtworks.paranamer.DefaultParanamer", "lookupMethod", "classLoader,className,methodName,paramNames");
        assertEquals(DefaultParanamer.class.getMethod("lookupMethod", new Class[]{ClassLoader.class, String.class, String.class, String.class}), method);
    }

    // An example of a test that looks a method up based on class/method only
    public void testParamerNameChoicesCanBeRetrievedForAMethodName() throws IOException, NoSuchMethodException {
        String[] paramNames = new DefaultParanamer().lookupParameterNames(Paranamer.class.getClassLoader(), "com.thoughtworks.paranamer.DefaultParanamer", "lookupMethod");
        assertEquals(2, paramNames.length);
        assertEquals("classLoader,c,m,p", paramNames[0]);
        assertEquals("classLoader,className,methodName,paramNames", paramNames[1]);
    }

    public void testParamerNameChoicesCannotBeRetrievedForAMissingMethodName() throws IOException, NoSuchMethodException {
        String[] paramNames = new DefaultParanamer().lookupParameterNames(Paranamer.class.getClassLoader(), "com.thoughtworks.paranamer.DefaultParanamer", "smdnfmsndf");
        assertEquals(0, paramNames.length);
    }


    // An example of a test that looks a method up by it's OLD parameter names
    // These were encoded via a doclet tag on the method in question:
    //
    //   @previousParamNames clazz,cmapn
    public void testMethodCanBeRetrievedByParameterNamesPreviouslyUsed() throws IOException, NoSuchMethodException {
        Method method = new DefaultParanamer().lookupMethod(Paranamer.class.getClassLoader(), "com.thoughtworks.paranamer.DefaultParanamer", "lookupMethod", "classLoader,c,m,p");
        assertEquals(DefaultParanamer.class.getMethod("lookupMethod", new Class[]{ClassLoader.class, String.class, String.class, String.class}), method);
    }

    // don't forget, you can copy the lookup() code into your project as it's public domain, if you want to use
    // the Paranamer technology without an extra jar.

    // An example of a test that looks a constructor up by it's parameter names
    public void testConstructorCanBeRetrievedByParameterNames() throws IOException, NoSuchMethodException {
        Constructor ctor = new DefaultParanamer().lookupConstructor(Paranamer.class.getClassLoader(), "com.thoughtworks.paranamer.ParanamerException", "message");
        assertEquals(ParanamerException.class.getConstructor(new Class[]{String.class}), ctor);
    }

}
