package com.thoughtworks.paranamer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

public class PositionalParanamerTestCase {

    Paranamer paranamer;

    @Before
    public void setUp() throws Exception {
        paranamer = new PositionalParanamer();
    }

    @Test
    public void testRetrievesParameterNamesFromAMethod()
            throws SecurityException, NoSuchMethodException {
        Method method = Clazz.class.getMethod("singleString",
                new Class[] { String.class });
        assertEquals("arg0", paranamer.lookupParameterNames(method)[0]);
    }

    @Test
    public void testRetrievesParameterNamesFromAConstructor()
            throws SecurityException, NoSuchMethodException {
        Constructor<?> ctor = Clazz.class.getConstructor(String.class);
        assertEquals("arg0", paranamer.lookupParameterNames(ctor)[0]);
    }

    @Test
    public void testRetrievesParameterNamesFromAMethodWithoutParameters()
            throws SecurityException, NoSuchMethodException {
        Method method = Clazz.class.getMethod("noParameters", new Class[0]);
        assertArrayEquals(new String[] {},
                paranamer.lookupParameterNames(method));
    }

    @Test
    public void testRetrievesParameterNamesFromMethodWithDoubleMixedInTheParameters()
            throws SecurityException, NoSuchMethodException {
        Method method = Clazz.class.getMethod("mixedParameters", new Class[] {
                double.class, String.class });
        assertArrayEquals(new String[] { "arg0", "arg1" },
                paranamer.lookupParameterNames(method));
    }

    @Test
    public void testRetrievesParameterNamesFromMethodWithDoubleMixedInTheParametersAndCustomPrefix()
            throws SecurityException, NoSuchMethodException {
        paranamer = new PositionalParanamer("foo");
        Method method = Clazz.class.getMethod("mixedParameters", new Class[] {
                double.class, String.class });
        assertArrayEquals(new String[] { "foo0", "foo1" },
                paranamer.lookupParameterNames(method));
    }

    public static class Clazz {
        public Clazz(String foo) {
        }

        public void singleString(String s) {
        }

        public static void staticWithParameter(int i) {
        }

        public void noParameters() {
        }

        public void mixedParameters(double d, String s) {
        }
    }

}
