package com.thoughtworks.paranamer;

/*
 * Copyright 2007 Paul Hammant
 * Copyright 2013 Samuel Halliday
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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.netlib.blas.Dasum;
import org.netlib.blas.Dgbmv;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.lang.reflect.AccessibleObject;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * @author Samuel Halliday
 */
public class JavadocParanamerTest extends AbstractParanamerTestCase {

    private static final String JAVADOCS_3 = "http://docs.oracle.com/javase/1.3/docs/api/";
    private static final String JAVADOCS_4 = "http://docs.oracle.com/javase/1.4.2/docs/api/";
    private static final String JAVADOCS_5 = "http://docs.oracle.com/javase/1.5.0/docs/api/";
    private static final String JAVADOCS_6 = "http://docs.oracle.com/javase/6/docs/api/";
    private static final String JAVADOCS_7 = "http://docs.oracle.com/javase/7/docs/api/";

    private static final String JAVADOCS_F2J = "http://icl.cs.utk.edu/projectsfiles/f2j/javadoc/";
    private static final String JAVADOCS_F2J_FILE = "paranamer/target/test-data/arpack_combined_all-0.1-javadoc.jar";

    private static final String JAVADOCS_4_PARTIAL_DIR = "paranamer/src/resources/javadocs/jdk1.4/docs";
    private static final String JAVADOCS_5_PARTIAL_DIR = "paranamer/src/resources/javadocs/jdk5/docs";
    private static final String JAVADOCS_6_PARTIAL_DIR = "paranamer/src/resources/javadocs/jdk6/docs/api";
    private static final String JAVADOCS_7_PARTIAL_DIR = "paranamer/src/resources/javadocs/jdk7";

    private static final String JAVADOCS_4_PARTIAL_ZIP = "paranamer/src/resources/javadocs/jdk1.4.zip";
    private static final String JAVADOCS_5_PARTIAL_ZIP = "paranamer/src/resources/javadocs/jdk5.zip";
    private static final String JAVADOCS_6_PARTIAL_ZIP = "paranamer/src/resources/javadocs/jdk6.zip";
    private static final String JAVADOCS_7_PARTIAL_ZIP = "paranamer/src/resources/javadocs/jdk7.zip";

    private static final String JAVADOCS_PARANAMER_FILE = "paranamer/target/test-data/paranamer-2.5.5-javadoc.jar";

    @Before
    public void setUp() throws Exception {
        paranamer = new JavadocParanamer(new File(JAVADOCS_PARANAMER_FILE));
    }

    @Test
    @Ignore("java.lang.Object.toString is not documented in the paranamer javadocs")
    @Override
    public void testLookupParameterNamesForMethodWhenNoArg() throws Exception {
    }

    @Test
    @Ignore("private methods are not documented so parameter names are not available")
    @Override
    public void testLookupParameterNamesForPrivateMethod() throws Exception {
    }

    @Test(expected = FileNotFoundException.class)
    public void failsIfBadInput() throws Exception {
        new JavadocParanamer(new URL(JAVADOCS_7 + "/DOES_NOT_EXIST"));
    }

    @Test(expected = FileNotFoundException.class)
    public void failsIfNotAFile() throws Exception {
        new JavadocParanamer(new File("DOES_NOT_EXIST"));
    }

    @Test(expected = FileNotFoundException.class)
    public void failsIfNotAJavadocDirectory() throws Exception {
        new JavadocParanamer(new File("./"));
    }

    @Test
    public void dirParanamer() throws Exception {
        testJavaIoFile(JAVADOCS_4_PARTIAL_DIR);
        testJavaIoFile(JAVADOCS_5_PARTIAL_DIR);
        testJavaIoFile(JAVADOCS_6_PARTIAL_DIR);
        testJavaIoFile(JAVADOCS_7_PARTIAL_DIR);
    }

    @Test
    public void fileParanamer() throws Exception {
        testJavaIoFile(JAVADOCS_4_PARTIAL_ZIP);
        testJavaIoFile(JAVADOCS_5_PARTIAL_ZIP);
        testJavaIoFile(JAVADOCS_6_PARTIAL_ZIP);
        testJavaIoFile(JAVADOCS_7_PARTIAL_ZIP);
    }

    @Test
    public void javadocs3() throws Exception {
        testJavaUtilUrl(JAVADOCS_3);
    }

    @Test
    public void javadocs4() throws Exception {
        testJavaUtilUrl(JAVADOCS_4);
    }

    @Test
    public void javadocs5() throws Exception {
        testJavaUtilUrl(JAVADOCS_5);
    }

    @Test
    public void javadocs6() throws Exception {
        testJavaUtilUrl(JAVADOCS_6);
    }

    @Test
    public void javadocs7() throws Exception {
        testJavaUtilUrl(JAVADOCS_7);
    }

    @Test
    public void f2JUrl() throws Exception {
        f2J(new JavadocParanamer(new URL(JAVADOCS_F2J)));
    }

    @Test
    public void f2JFile() throws Exception {
        f2J(new JavadocParanamer(new File(JAVADOCS_F2J_FILE)));
    }

    public void f2J(Paranamer p) throws Exception {
        testAccessible(p, Dasum.class.getMethod("dasum",
                Integer.TYPE, double[].class, Integer.TYPE, Integer.TYPE),
                "n", "dx", "_dx_offset", "incx");

        testAccessible(p, Dgbmv.class.getMethod("dgbmv",
                String.class, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE,
                Double.TYPE, double[].class, Integer.TYPE, Integer.TYPE, double[].class,
                Integer.TYPE, Integer.TYPE, Double.TYPE, double[].class, Integer.TYPE,
                Integer.TYPE),
                "trans", "m", "n", "kl", "ku", "alpha", "a", "_a_offset", "lda",
                "x", "_x_offset", "incx", "beta", "y", "_y_offset", "incy");
    }

    private void testJavaIoFile(String fileOrDirectory) throws Exception {
        Paranamer p = new JavadocParanamer(new File(fileOrDirectory));
        testAccessible(p, File.class.getMethod("listFiles", FileFilter.class), "filter");
        testAccessible(p, File.class.getConstructor(File.class, String.class), "parent", "child");
    }

    private void testJavaUtilUrl(String url) throws Exception {
        Paranamer p = new JavadocParanamer(new URL(url));

        // normal methods, collision in name
        testAccessible(p, Random.class.getMethod("nextInt", Integer.TYPE), "n");
        testAccessible(p, Random.class.getMethod("nextInt"));

        // static
        testAccessible(p, System.class.getMethod("getProperty", String.class, String.class), "key", "def");

        // constructor
        testAccessible(p, String.class.getConstructor(char[].class), "value");

        // generics (Java 5+)
        testAccessible(p, Collection.class.getMethod("containsAll", Collection.class), "c");
    }

    private void testAccessible(Paranamer p, AccessibleObject accessible, String... expected) {
        String[] names = p.lookupParameterNames(accessible);
        assertTrue(accessible + " " + Arrays.toString(names), Arrays.equals(expected, names));
    }

}
