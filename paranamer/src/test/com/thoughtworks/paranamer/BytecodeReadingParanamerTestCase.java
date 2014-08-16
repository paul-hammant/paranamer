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

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 *
 * @author Guilherme Silveira
 */
public class BytecodeReadingParanamerTestCase extends AbstractParanamerTestCase {

    @Before
    public void setUp() throws Exception {
        paranamer = new BytecodeReadingParanamer();
    }

    @Test
    public void testRetrievesParameterNamesFromAMethod() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        Method method = SpecificMethodSearchable.class.getMethod("singleString", new Class[] { String.class });
        String[] names = asm.lookupParameterNames(method);
        assertThatParameterNamesMatch("s", names);
    }


    @Test
    public void testRetrievesParameterNamesFromAConstructor() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        Constructor<?> ctor = SpecificMethodSearchable.class.getConstructor(String.class);
        String[] names = asm.lookupParameterNames(ctor);
        assertThatParameterNamesMatch("foo", names);
    }


    @Test
    public void testRetrievesParameterNamesFromAMethodWithoutParameters() throws SecurityException,
            NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        String[] names = asm.lookupParameterNames(SpecificMethodSearchable.class.getMethod("noParameters",
                new Class[0]));
        assertThatParameterNamesMatch("", names);
    }

    @Test
    public void testRetrievesParameterNamesFromAMethodWithoutParametersWithLocalVariable() throws SecurityException,
            NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        String[] names = asm.lookupParameterNames(SpecificMethodSearchable.class.getMethod(
                "noParametersOneLocalVariable", new Class[0]));
        assertThatParameterNamesMatch("", names);
    }

    @Test
    public void testRetrievesParameterNamesFromAStaticMethod() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        String[] names = asm.lookupParameterNames(SpecificMethodSearchable.class.getMethod(
                "staticWithParameter", new Class[] { int.class }));
        assertThatParameterNamesMatch("i", names);
    }

    @Test
    public void testRetrievesParameterNamesFromMethodWithShort() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        String[] names = asm.lookupParameterNames(SpecificMethodSearchable.class.getMethod("hasShort",
                new Class[] { short.class }));
        assertThatParameterNamesMatch("s", names);
    }

    @Test
    public void testRetrievesParameterNamesFromMethodWithLong() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        String[] names = asm.lookupParameterNames(SpecificMethodSearchable.class.getMethod("hasLong",
                new Class[] { long.class }));
        assertThatParameterNamesMatch("l", names);
    }

    @Test
    public void testRetrievesParameterNamesFromMethodWithLongs() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        String[] names = asm.lookupParameterNames(SpecificMethodSearchable.class.getMethod("hasLongs",
                new Class[] { long[].class }));
        assertThatParameterNamesMatch("l", names);
    }

    @Test
    public void testRetrievesParameterNamesFromMethodWithShorts() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        String[] names = asm.lookupParameterNames(SpecificMethodSearchable.class.getMethod("hasShorts",
                new Class[] { short[].class }));
        assertThatParameterNamesMatch("s", names);
    }

    @Test
    public void testRetrievesParameterNamesFromMethodWithDoubleMixedInTheParameters() throws SecurityException,
            NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        String[] names = asm.lookupParameterNames(SpecificMethodSearchable.class.getMethod("mixedParameters",
                new Class[] { double.class, String.class }));
        assertThatParameterNamesMatch("d,s", names);
    }

    @Test
    public void testDoesNotRetrieveParameterNamedArg0() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        try {
        	asm.lookupParameterNames(SpecificMethodSearchable.class.getMethod(
                "unsupportedParameterNames", new Class[] { String.class }));
        	fail("Should find (arg0) and think this is a debug-free compiled class.");
        } catch(ParameterNamesNotFoundException ex) {
        	// ok
        }
    }

    @Test
    public void testRetrievesParameterNamesFromMethodWithArray() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        Method method = SpecificMethodSearchable.class.getMethod("stringArray", new Class[]{String[].class});
        assertThatParameterNamesMatch("strings", asm.lookupParameterNames(method));
    }

    @Test
    public void testRetrievesParameterNamesFromMethodWithTwoDimensionalArray() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        Method method = SpecificMethodSearchable.class.getMethod("twoDimensionalArray", new Class[]{String[][].class});
        assertThatParameterNamesMatch("strings2D", asm.lookupParameterNames(method));
    }

    @Test
    public void testRetrievesParameterNamesFromMethodWithTwoDimensionalPrimitiveArray() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        Method method = SpecificMethodSearchable.class.getMethod("twoDimensionalArray", new Class[]{long[][].class});
        assertThatParameterNamesMatch("long2D", asm.lookupParameterNames(method));
    }

    @Test
    public void testRetrievesParameterNamesFromIntArrayMethod() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        Method method = SpecificMethodSearchable.class.getMethod("intArray", new Class[] { int[].class });
        assertThatParameterNamesMatch("ints", asm.lookupParameterNames(method));
    }

    @Test
    public void testRetrievesParameterNamesFromDoubleArrayMethod() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        Method method = SpecificMethodSearchable.class.getMethod("doubleArray", new Class[] { double[].class });
        assertThatParameterNamesMatch("doubles", asm.lookupParameterNames(method));
    }

    @Test
    public void testRetrievesParameterNamesFromByteArrayMethod() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        Method method = SpecificMethodSearchable.class.getMethod("byteArray", new Class[] { byte[].class });
        assertThatParameterNamesMatch("bytes", asm.lookupParameterNames(method));
    }

    @Test
    public void testRetrievesParameterNamesFromBooleanArrayMethod() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        Method method = SpecificMethodSearchable.class.getMethod("booleanArray", new Class[] { boolean[].class });
        assertThatParameterNamesMatch("flags", asm.lookupParameterNames(method));
    }

    @Test
    public void testRetrievesParameterNamesFromCharArrayMethod() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        Method method = SpecificMethodSearchable.class.getMethod("charArray", new Class[] { char[].class });
        assertThatParameterNamesMatch("chars", asm.lookupParameterNames(method));
    }

    @Test
    public void testRetrievesParameterNamesFromFloatArrayMethod() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        Method method = SpecificMethodSearchable.class.getMethod("floatArray", new Class[] { float[].class });
        assertThatParameterNamesMatch("floats", asm.lookupParameterNames(method));
    }



    @Test
    public void testRetrievesParameterNamesFromOtherArrayMethod() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        Method method = SpecificMethodSearchable.class.getMethod("otherArray", new Class[] { Other[].class });
        assertThatParameterNamesMatch("others", asm.lookupParameterNames(method));
    }

    @Test
    public void testRetrievesParameterNamesFromAConstructorInJar()
            throws ClassNotFoundException, NoSuchMethodException {
        URL url = getClass().getResource("/test.jar");
        URLClassLoader classLoader = new URLClassLoader(new URL[]{url});
        Class<?> clazz = Class.forName("com.thoughtworks.paranamer.SpecificMethodSearchable", true, classLoader);

        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        Constructor<?> ctor = clazz.getConstructor(String.class);
        assertThatParameterNamesMatch("foo", asm.lookupParameterNames(ctor));
    }

    @Test
    public void testRetrievesParameterNamesFromBootstrapClassLoader() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        Constructor<?> ctor = Integer.class.getConstructor(int.class);
        try {
            asm.lookupParameterNames(ctor);
            fail("Should not find names for classes loaded by the bootstrap class loader.");
        } catch(ParameterNamesNotFoundException ex) {
            // ok
        }
    }

    @Test
    public void testLookupParameterNamesForInterfaceMethod() {
        try {
            super.testLookupParameterNamesForInterfaceMethod();
            fail("should have barfed");
        } catch (Exception e) {
            assertTrue(e instanceof ParameterNamesNotFoundException);
        }
    }

    @Test
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

        public void hasShort(short s) {

        }

        public void hasLongs(long[] l) {

        }

        public void hasShorts(short[] s) {

        }

        public void mixedParameters(double d, String s) {

        }

        public void unsupportedParameterNames(String arg0) {
        }

        public void stringArray(String[] strings) {
        }

        public void twoDimensionalArray(String[][] strings2D) {
        }

        public void twoDimensionalArray(long[][] long2D) {
        }

        public void intArray(int[] ints) {
        }

        public void booleanArray(boolean[] flags) {
        }

        public void charArray(char[] chars) {
        }

        public void floatArray(float[] floats) {
        }

        public void doubleArray(double[] doubles) {
        }

        public void byteArray(byte[] bytes) {
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