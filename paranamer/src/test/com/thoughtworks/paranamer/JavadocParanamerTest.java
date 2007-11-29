/*
 * Copyright 2007 Paul Hammant
 * Copyright 2007 ThinkTank Mathematics Limited
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

	public void testArchive() throws IOException {
		JavadocParanamer paranamer = getArchiveParanamerSun();
		availabilityTest(paranamer);
		namesTest(paranamer);
	}

	public void testConstructor() throws IOException {
		boolean caught = false;
		try {
			new JavadocParanamer(new File("non-existant-file"));
		} catch (FileNotFoundException e) {
			caught = true;
		}
		assertTrue(caught);
		caught = false;
		try {
			new JavadocParanamer(new File("./"));
		} catch (IllegalArgumentException e) {
			caught = true;
			// should have failed due to no package-list found
			assertTrue(e.getMessage().indexOf("package-list") >= 0);
		}
		assertTrue(caught);
		getArchiveParanamerSun();
		getDirectoryParanamerSun();
		getURLParanamerSun();
	}

// public void testDirectory() throws IOException {
// JavadocParanamer paranamer = getDirectoryParanamerSun();
// availabilityTest(paranamer);
// namesTest(paranamer);
// }

// public void testURL() throws IOException {
// JavadocParanamer paranamer = getURLParanamerSun();
// availabilityTest(paranamer);
// namesTest(paranamer);
// }

	private void availabilityTest(JavadocParanamer paranamer) {
		assertTrue(paranamer.areParameterNamesAvailable(getClass(), "") == Paranamer.NO_PARAMETER_NAMES_FOR_CLASS);
		assertTrue(paranamer.areParameterNamesAvailable(RuntimeException.class,
			"<init>") == Paranamer.PARAMETER_NAMES_FOUND);
		// make sure we're not just grepping on the javadocs
		assertTrue(paranamer.areParameterNamesAvailable(File.class,
			"operating systems") == Paranamer.NO_PARAMETER_NAMES_FOR_CLASS_AND_MEMBER);
		assertTrue(paranamer.areParameterNamesAvailable(File.class, "toURL") == Paranamer.PARAMETER_NAMES_FOUND);
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

	private void namesTest(JavadocParanamer paranamer) {
		{
			Method[] methods = File.class.getMethods();

			for (int i = 0; i < methods.length; i++) {
				Method method = methods[i];
				try {
					paranamer.lookupParameterNames(method);
				} catch (ParameterNamesNotFoundException e) {
					// lots of errors probably as the Javadocs are for
					// Java 6 and we're probably running Java 1.2
					System.out.println("Unable to find names for "
							+ e.getMessage());
				}
			}
		}
		{
			// this test of HashSet demonstrates that generics do not always interfere
			// and that methods defined in superclasses also work
			Method[] methods = HashSet.class.getMethods();

			for (int i = 0; i < methods.length; i++) {
				Method method = methods[i];
				try {
					paranamer.lookupParameterNames(method);
				} catch (ParameterNamesNotFoundException e) {
					// lots of errors probably as the Javadocs are for
					// Java 6 and we're probably running Java 1.2
					System.out.println("Unable to find names for "
							+ e.getMessage());
				}
			}
		}
	}

}
