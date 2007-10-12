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

    public void testDoesNotRetrieveParameterNamedArg0() throws SecurityException, NoSuchMethodException {
        BytecodeReadingParanamer asm = new BytecodeReadingParanamer();
        String[] names = asm.lookupParameterNames(SpecificMethodSearchable.class.getMethod(
                "unsupportedParameterNames", new Class[] { String.class }));
        assertNull(names);
    }

    public static class SpecificMethodSearchable {

        String foo;
        int bar = 11;

 //       @GratuitousAnnotation
        public SpecificMethodSearchable(String foo) {
            System.out.println("");

        }
   //     @GratuitousAnnotation

        public SpecificMethodSearchable() {
            System.out.println("");
        }
     //   @GratuitousAnnotation

    //    public void singleString(@GratuitousAnnotation String s) {
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
    }

    public static class SearchableTypeByMethodName {
        public void overloaded(int a) {
        }
        public void overloaded(String b) {
        }
    }

}