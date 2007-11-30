/*
 * Copyright 2007 Paul Hammant
 * Copyright 2007 ThinkTank Mathematics Limited
 * 
 * ThinkTank Mathematics Limited grants a non-revocable, perpetual licence
 * to Paul Hammant for unlimited use, relicensing and redistribution. No
 * explicit permission is required from ThinkTank Mathematics Limited for
 * any future decisions made with regard to this file.
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
 */
package com.thoughtworks.paranamer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashSet;
import java.util.Random;

import junit.framework.TestCase;

/**
 * @author Samuel Halliday, ThinkTank Mathematics Limited
 */
public class JavadocParanamerTest extends TestCase {

	private static final String SUN_API_BASE = "docs/api";
	private final static String SUN_ARCHIVE_FILENAME;
	// you may set your own base directory here for the directory base of the
	// extracted documentation
	private static final String SUN_DIRECTORY_BASE = "";
	private final static String SUN_DOWNLOAD_URL;
	private static final String SUN_JAVADOC_URL;

	// we set up the URL for the SUN Javadocs and zip file names depending on the runtime
	static {
		int version =
				Integer.parseInt(System.getProperty("java.version").substring(
					2, 3));
		switch (version) {
		case 5:
			SUN_ARCHIVE_FILENAME = "jdk-1_5_0-doc.zip";
			SUN_DOWNLOAD_URL =
					"http://java.sun.com/javase/downloads/index_jdk5.jsp";
			SUN_JAVADOC_URL = "http://java.sun.com/j2se/1.5.0/docs/api";
			break;
		case 4:
			SUN_ARCHIVE_FILENAME = "j2sdk-1_4_2-doc.zip";
			SUN_DOWNLOAD_URL = "http://java.sun.com/j2se/1.4.2/download.html";
			SUN_JAVADOC_URL = "http://java.sun.com/j2se/1.4.2/docs/api";
			break;
		case 3:
			SUN_ARCHIVE_FILENAME = "j2sdk-1_3_1-doc.zip";
			SUN_DOWNLOAD_URL = "http://java.sun.com/j2se/1.3/download.html";
			SUN_JAVADOC_URL = "http://java.sun.com/j2se/1.3/docs/api";
			break;
		default:
			SUN_ARCHIVE_FILENAME = "jdk-6-doc.zip";
			SUN_DOWNLOAD_URL = "http://java.sun.com/javase/downloads/";
			SUN_JAVADOC_URL = "http://java.sun.com/javase/6/docs/api/";
		}
	}

	public void testCanFindForAppropriateMethodDir() throws IOException {
		testCanFindForAppropriateMethod(getDirectoryParanamerSun());
	}

	public void testCanFindForAppropriateMethodFile() throws IOException {
		testCanFindForAppropriateMethod(getArchiveParanamerSun());
	}

	public void testCanFindForAppropriateMethodURL() throws IOException {
		testCanFindForAppropriateMethod(getURLParanamerSun());
	}

	public void testCannotFindForInappropriateMethodsEtcDir()
			throws IOException {
		testCannotFindForInappropriateMethodsEtc(getDirectoryParanamerSun());
	}

	public void testCannotFindForInappropriateMethodsEtcFile()
			throws IOException {
		testCannotFindForInappropriateMethodsEtc(getArchiveParanamerSun());
	}

	public void testCannotFindForInappropriateMethodsEtcURL()
			throws IOException {
		testCannotFindForInappropriateMethodsEtc(getURLParanamerSun());
	}

	public void testFailsIfABadUrl() {
		try {
			new JavadocParanamer(
				new URL(
					"http://codehaus.org/justForTestIngSorryIfThisMessesUpTheLogsBobSeeParanamerSource.zip"));
			fail("should have barfed");
		} catch (Exception e) { // TODO - which exception?
			// expected
		}
	}

	public void testFailsIfNotAFile() throws IOException {
		try {
			new JavadocParanamer(new File("non-existant-file"));
			fail("should have barfed");
		} catch (FileNotFoundException e) {
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

	public void testGenericsDontInterfereWithExtractionDir() throws IOException {
		testGenericsDontInterfereWithExtraction(getDirectoryParanamerSun());
	}

	public void testGenericsDontInterfereWithExtractionFile()
			throws IOException {
		testGenericsDontInterfereWithExtraction(getArchiveParanamerSun());
	}

	public void testGenericsDontInterfereWithExtractionURL() throws IOException {
		testGenericsDontInterfereWithExtraction(getURLParanamerSun());
	}

	public void testNamesInIterativeMannerDir() throws IOException {
		testNamesInIterativeManner(getDirectoryParanamerSun());
	}

	public void testNamesInIterativeMannerFile() throws IOException {
		testNamesInIterativeManner(getArchiveParanamerSun());
	}

	public void testNamesInIterativeMannerURL() throws IOException {
		testNamesInIterativeManner(getURLParanamerSun());
	}

	private JavadocParanamer getArchiveParanamerSun() throws IOException {
		File archive = new File(SUN_DIRECTORY_BASE + SUN_ARCHIVE_FILENAME);
		if (!archive.exists())
			assertTrue("Please download " + SUN_ARCHIVE_FILENAME + " from "
					+ SUN_DOWNLOAD_URL + " and place it in "
					+ SUN_DIRECTORY_BASE + " to run this test.", false);

		return new JavadocParanamer(archive);
	}

	private JavadocParanamer getDirectoryParanamerSun() throws IOException {
		File directory = new File(SUN_DIRECTORY_BASE + SUN_API_BASE);
		if (!directory.exists())
			assertTrue("Please download " + SUN_ARCHIVE_FILENAME + " from "
					+ SUN_DOWNLOAD_URL + " and extract it in "
					+ SUN_DIRECTORY_BASE + " to run this test.", false);

		return new JavadocParanamer(directory);
	}

	private JavadocParanamer getURLParanamerSun() throws IOException {
		return new JavadocParanamer(new URL(SUN_JAVADOC_URL));
	}

	private void testCanFindForAppropriateMethod(Paranamer paranamer) {
		assertEquals(Paranamer.PARAMETER_NAMES_FOUND,
			paranamer.areParameterNamesAvailable(File.class, "canRead"));
	}

	private void testCannotFindForInappropriateMethodsEtc(Paranamer paranamer) {
		assertEquals(Paranamer.NO_PARAMETER_NAMES_FOR_CLASS,
			paranamer.areParameterNamesAvailable(getClass(), ""));
		assertEquals(Paranamer.PARAMETER_NAMES_FOUND,
			paranamer.areParameterNamesAvailable(Random.class, "<init>"));
		// make sure we're not just grepping on the javadocs
		assertEquals(Paranamer.NO_PARAMETER_NAMES_FOR_CLASS_AND_MEMBER,
			paranamer.areParameterNamesAvailable(File.class,
				"operating system"));
	}

	private void testGenericsDontInterfereWithExtraction(Paranamer paranamer) {
		// this test of HashSet demonstrates that generics do not interfere
		// and that methods defined in super classes also work
		for (int i = 0; i < HashSet.class.getMethods().length; i++) {
			Method method = HashSet.class.getMethods()[i];
			try {
				paranamer.lookupParameterNames(method);
			} catch (ParameterNamesNotFoundException e) {
				System.out.println("Unable to find names for " + e.getMessage());
			}
		}
	}

	private void testNamesInIterativeManner(Paranamer paranamer) {
		Method[] methods = HashSet.class.getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			try {
				paranamer.lookupParameterNames(method);
			} catch (ParameterNamesNotFoundException e) {
				System.out.println("Unable to find names for " + e.getMessage());
			}
		}
	}

}
