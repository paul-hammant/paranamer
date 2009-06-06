/***
 *
 * Copyright (c) 2007 Paul Hammant
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.thoughtworks.paranamer;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;


/**
 *
 * @author Guilherme Silveira
 */
public class BytecodeReadingParanamerTestCase extends AbstractParanamerTestCase {

    protected void setUp() throws Exception {
        paranamer = new BytecodeReadingParanamer();
    }

    public void testRetrievesParameterNamesFromAMethod() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        Method method = SpecificMethodSearchable.class.getMethod("singleString", new Class[] { String.class });
        String[] names = asm.lookupParameterNames(method);
        assertThatParameterNamesMatch("s", names);
    }

    public void testAreParameterNamesAvailableWorks() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        int retval = asm.areParameterNamesAvailable(SpecificMethodSearchable.class, "<init>");
        assertEquals(Paranamer.PARAMETER_NAMES_FOUND, retval);
    }


    public void testRetrievesParameterNamesFromAConstructor() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        Constructor ctor = SpecificMethodSearchable.class.getConstructor(new Class[] { String.class });
        String[] names = asm.lookupParameterNames(ctor);
        assertThatParameterNamesMatch("foo", names);
    }


    public void testRetrievesParameterNamesFromAMethodWithoutParameters() throws SecurityException,
            NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        String[] names = asm.lookupParameterNames(SpecificMethodSearchable.class.getMethod("noParameters",
                new Class[0]));
        assertThatParameterNamesMatch("", names);
    }

    public void testRetrievesParameterNamesFromAMethodWithoutParametersWithLocalVariable() throws SecurityException,
            NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        String[] names = asm.lookupParameterNames(SpecificMethodSearchable.class.getMethod(
                "noParametersOneLocalVariable", new Class[0]));
        assertThatParameterNamesMatch("", names);
    }

    public void testRetrievesParameterNamesFromAStaticMethod() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        String[] names = asm.lookupParameterNames(SpecificMethodSearchable.class.getMethod(
                "staticWithParameter", new Class[] { int.class }));
        assertThatParameterNamesMatch("i", names);
    }

    public void testRetrievesParameterNamesFromMethodWithLong() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        String[] names = asm.lookupParameterNames(SpecificMethodSearchable.class.getMethod("hasLong",
                new Class[] { long.class }));
        assertThatParameterNamesMatch("l", names);
    }

    public void testRetrievesParameterNamesFromMethodWithDoubleMixedInTheParameters() throws SecurityException,
            NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        String[] names = asm.lookupParameterNames(SpecificMethodSearchable.class.getMethod("mixedParameters",
                new Class[] { double.class, String.class }));
        assertThatParameterNamesMatch("d,s", names);
    }

    public void testDoesNotRetrieveParagmeterNamedArg0() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        try {
        	asm.lookupParameterNames(SpecificMethodSearchable.class.getMethod(
                "unsupportedParameterNames", new Class[] { String.class }));
        	fail("Should find (arg0) and think this is a debug-free compiled class.");
        } catch(ParameterNamesNotFoundException ex) {
        	// ok
        }
    }

    public void testRetrievesParameterNamesFromMethodWithArray() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        Method method = SpecificMethodSearchable.class.getMethod("stringArray", new Class[]{String[].class});
        assertThatParameterNamesMatch("strings", asm.lookupParameterNames(method));
    }

    public void testRetrievesParameterNamesFromIntArrayMethod() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        Method method = SpecificMethodSearchable.class.getMethod("intArray", new Class[] { int[].class });
        assertThatParameterNamesMatch("ints", asm.lookupParameterNames(method));
    }

    public void testRetrievesParameterNamesFromDoubleArrayMethod() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        Method method = SpecificMethodSearchable.class.getMethod("doubleArray", new Class[] { double[].class });
        assertThatParameterNamesMatch("doubles", asm.lookupParameterNames(method));
    }

    public void testRetrievesParameterNamesFromOtherArrayMethod() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        Method method = SpecificMethodSearchable.class.getMethod("otherArray", new Class[] { Other[].class });
        assertThatParameterNamesMatch("others", asm.lookupParameterNames(method));
    }

    public void testRetrievesParameterNamesFromAConstructorInJar()
            throws ClassNotFoundException, NoSuchMethodException {
        URL url = getClass().getResource("/test.jar");
        URLClassLoader classLoader = new URLClassLoader(new URL[]{url});
        Class clazz = Class.forName("com.thoughtworks.paranamer.SpecificMethodSearchable", true, classLoader);

        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        Constructor ctor = clazz.getConstructor(new Class[]{String.class});
        assertThatParameterNamesMatch("foo", asm.lookupParameterNames(ctor));
    }

    public void testRetrievesParameterNamesFromBootstrapClassLoader() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        Constructor ctor = Integer.class.getConstructor(new Class[] { int.class });
        try {
            asm.lookupParameterNames(ctor);
            fail("Should not find names for classes loaded by the bootstrap class loader.");
        } catch(ParameterNamesNotFoundException ex) {
            // ok
        }
    }

    public void testLookupParameterNamesForInterfaceMethod() {
        try {
            super.testLookupParameterNamesForInterfaceMethod();
            fail("should have barfed");
        } catch (Exception e) {
            assertTrue(e instanceof ParameterNamesNotFoundException);
        }
    }

    // from http://jira.codehaus.org/browse/PARANAMER-10
    public void testEmptyParameterShouldReturnAnEmptyArray() throws Exception {
		BytecodeReadingParanamer paranamer = new BytecodeReadingParanamer();

        Method method = NoArgs.class.getMethod("foo", new Class[]{});
		String[] methNames = paranamer.lookupParameterNames(method);
		assertEquals(0, methNames.length); //Failure here!
	}

    class NoArgs {
	    public void foo() {}
    }

    public static class SpecificMethodSearchable {

        String foo;
        int bar = 11;

        public SpecificMethodSearchable(String foo) {
            System.out.println("");

        }
        public SpecificMethodSearchable() {
            System.out.println("");
        }

        public void singleString(String s) {
            bar = 22;
        }

        public void noParametersOneLocalVariable() {
            foo = "foo";
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

        public void stringArray(String[] strings) {
        }

        public void intArray(int[] ints) {
        }

        public void doubleArray(double[] doubles) {
        }

        public void otherArray(Other[] others) {
        }
    }
    public static class Other{}


    public static class SearchableTypeByMethodName {
        public void overloaded(int a) {
        }
        public void overloaded(String b) {
        }
    }

}