/*
 * Copyright 2007 Paul Hammant
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
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
 *
 * ---
 *
 * Original Source donated to Paranamer project by Sam Halliday, ThinkTank Mathematics Limited
 * A grant of copyright to this source was given to Paul Hammant by Sam, November 2007, who
 * also retains a right to his original version.
 *
 */
package com.thoughtworks.paranamer;

import com.thoughtworks.qdox.model.IndentBuffer;
import junit.framework.TestCase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * @author Samuel Halliday, ThinkTank Mathematics Limited
 */
public class JavadocParanamerTest extends TestCase {

	private final static String JAVADOC_FILENAME = "qdox-1.6.3-javadoc.jar";

	public void testFailsIfNotAFile() throws IOException {
		try {
			new JavadocParanamer(new File("non-existant-file"));
            fail("should have barfed");
        } catch (FileNotFoundException e) {
			// expected
		}
    }

    public void testFailsIfABadUrl() {
        try {
            new JavadocParanamer(new URL("http://codehaus.org/justForTestIngSorryIfThisMessesUpTheLogsBobSeeParanamerSource.zip"));
            fail("should have barfed");
        } catch (Exception e) {  //TODO - which exception?
            // expected
        }
    }

    public void testFailsIfNotAJavadocDirectory() throws IOException {
		try {
			new JavadocParanamer(new File("./"));
            fail("should have barfed");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("package-list"));
		}
	}

	public void testCannotFindForInappropriateMethodsEtcInFile() throws IOException {
        Paranamer paranamer = new JavadocParanamer(getQDoxJavaDocFile());
        assertEquals(Paranamer.NO_PARAMETER_NAMES_FOR_CLASS, paranamer.areParameterNamesAvailable(getClass(), ""));
		assertEquals(Paranamer.PARAMETER_NAMES_FOUND, paranamer.areParameterNamesAvailable(IndentBuffer.class,"<init>"));
		// make sure we're not just grepping on the javadocs
		assertEquals(Paranamer.NO_PARAMETER_NAMES_FOR_CLASS_AND_MEMBER, paranamer.areParameterNamesAvailable(IndentBuffer.class, "resulting object model navigated"));
    }

    public void testCanFindForAppropriateMethod() throws IOException {
        Paranamer paranamer = new JavadocParanamer(getQDoxJavaDocFile());
		assertEquals(Paranamer.PARAMETER_NAMES_FOUND, paranamer.areParameterNamesAvailable(IndentBuffer.class, "write"));
	}

    public File getQDoxJavaDocFile() {
        String path = new File(".").getAbsolutePath();
        File file = new File(path.substring(0,path.lastIndexOf("/paranamer/")));
        if (!file.exists()) {
            throw new RuntimeException("weird path " + file.getAbsolutePath());
        }
        return new File(file.getAbsolutePath(), JAVADOC_FILENAME);
    }

	public void testNamesInIterativeManner() throws IOException {
        Paranamer paranamer = new JavadocParanamer(getQDoxJavaDocFile());
        Method[] methods = IndentBuffer.class.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            try {
                String[] names = paranamer.lookupParameterNames(method);
            } catch (ParameterNamesNotFoundException e) {
                // NOTE: File.compareTo(File) is bad coding from SUN
                // it should have been File.compareTo(Object)
                System.out.println("Unable to find names for " + e.getMessage());
            }
        }

	}
    public void testGenericsDontInterfereWithExtraction() {
//        // this test of HashSet demonstrates that generics do not interfere
//        // and that methods defined in superclasses also work
//        for (int i = 0; i < HashSet.class.getMethods().length; i++) {
//            Method method = HashSet.class.getMethods()[i];
//            try {
//                String[] names = paranamer.lookupParameterNames(method);
//// if (names.length > 0)
//// System.out.println(method + " " + Arrays.toString(names));
//            } catch (ParameterNamesNotFoundException e) {
//                System.out.println("Unable to find names for " + e.getMessage());
//            }
//        }

    }

}
