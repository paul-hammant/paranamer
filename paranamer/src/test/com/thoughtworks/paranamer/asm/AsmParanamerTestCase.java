package com.thoughtworks.paranamer.asm;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

import com.thoughtworks.paranamer.AbstractParanamerTestCase;

/**
 * 
 * @author Guilherme Silveira
 */
public class AsmParanamerTestCase extends AbstractParanamerTestCase {

    protected void setUp() throws Exception {
        paranamer = new AsmParanamer();
    }

    public void testRetrievesParameterNamesFromAMethod() throws SecurityException, NoSuchMethodException {
        AsmParanamer asm = new AsmParanamer();
        Method method = SpecificMethodSearchable.class.getMethod("singleString", new Class[] { String.class });
        String[] names = asm.lookupParameterNames(method);
        assertThatParameterNamesMatch("s", names);
    }

    public void testRetrievesParameterNamesFromAConstructor() throws SecurityException, NoSuchMethodException {
        AsmParanamer asm = new AsmParanamer();
        Constructor ctor = SpecificMethodSearchable.class.getConstructor(new Class[] { String.class });
        String[] names = asm.lookupParameterNames(ctor);
        assertThatParameterNamesMatch("foo", names);
    }


    public void testRetrievesParameterNamesFromAMethodWithoutParameters() throws SecurityException,
            NoSuchMethodException {
        AsmParanamer asm = new AsmParanamer();
        String[] names = asm.lookupParameterNames(SpecificMethodSearchable.class.getMethod("noParameters",
                new Class[0]));
        assertThatParameterNamesMatch("", names);
    }

    public void testRetrievesParameterNamesFromAMethodWithoutParametersWithLocalVariable() throws SecurityException,
            NoSuchMethodException {
        AsmParanamer asm = new AsmParanamer();
        String[] names = asm.lookupParameterNames(SpecificMethodSearchable.class.getMethod(
                "noParametersOneLocalVariable", new Class[0]));
        assertThatParameterNamesMatch("", names);
    }

    public void testRetrievesParameterNamesFromAStaticMethod() throws SecurityException, NoSuchMethodException {
        AsmParanamer asm = new AsmParanamer();
        String[] names = asm.lookupParameterNames(SpecificMethodSearchable.class.getMethod(
                "staticWithParameter", new Class[] { int.class }));
        assertThatParameterNamesMatch("i", names);
    }

    public void testRetrievesParameterNamesFromMethodWithLong() throws SecurityException, NoSuchMethodException {
        AsmParanamer asm = new AsmParanamer();
        String[] names = asm.lookupParameterNames(SpecificMethodSearchable.class.getMethod("hasLong",
                new Class[] { long.class }));
        assertThatParameterNamesMatch("l", names);
    }

    public void testRetrievesParameterNamesFromMethodWithDoubleMixedInTheParameters() throws SecurityException,
            NoSuchMethodException {
        AsmParanamer asm = new AsmParanamer();
        String[] names = asm.lookupParameterNames(SpecificMethodSearchable.class.getMethod("mixedParameters",
                new Class[] { double.class, String.class }));
        assertThatParameterNamesMatch("d,s", names);
    }

    public void testDoesNotRetrieveParameterNamedArg0() throws SecurityException, NoSuchMethodException {
        AsmParanamer asm = new AsmParanamer();
        String[] names = asm.lookupParameterNames(SpecificMethodSearchable.class.getMethod(
                "unsupportedParameterNames", new Class[] { String.class }));
        assertNull(names);
    }

    public static class SpecificMethodSearchable {


        public SpecificMethodSearchable(String foo) {

        }

        public SpecificMethodSearchable() {
        }

        public void singleString(String s) {
            int k = 3;
        }

        public void noParametersOneLocalVariable() {
            int k = 3;
        }

        public static void staticWithParameter(int i) {

        }

        public void noParameters() {
        }

        public void hasLong(long l) {

        }

        public void mixedParameters(double d, String s) {

        }

        public void unsupportedParameterNames(String arg0) {

        }
    }

    public static class SearchableTypeByMethodName {
        public void overloaded(int a) {
        }
        public void overloaded(String b) {
        }
    }

}
