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
package com.thoughtworks.paranamer.generator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;

public class OldQDoxParanamerTestCase {

    private String root;

    @Before
    public void setUp() throws Exception {
        ParanamerGenerator generator = new QdoxParanamerGenerator();
        root = new File(".").getAbsolutePath();
        // Kludge for running inside Intellij when it's universally setting
        // the current directory to the root of the checkout :-(
        if (!root.contains("/paranamer-generator")) {
            root = root.replace("/.", "/paranamer-generator");
        }
        generator.processSourcePath(root + "/src/test", root + "/target/test-classes/");
    }

    private String paranamerDataOf(String className) throws IOException, NoSuchFieldException, IllegalAccessException {
        return paranamerDataOf(className, className);
    }

    private String paranamerDataOf(String className, String fileName) throws IOException, NoSuchFieldException, IllegalAccessException {
        FileInputStream fis = new FileInputStream(root + "/target/test-classes/com/thoughtworks/paranamer/generator/" + fileName + ".class");
        byte[] bytes = new byte[40000];
        int read = fis.read(bytes);
        byte[] bytes2 = new byte[read];
        System.arraycopy(bytes, 0, bytes2, 0, read);

        MyClassLoader cl = new MyClassLoader();

        Class<?> enhancedClazz = cl.defineEnhancerClass(className, bytes2, read);
        Field f = enhancedClazz.getField("__PARANAMER_DATA");
        f.setAccessible(true);
        return (String) f.get(null);
    }

    @Test
    public void testSimpleClassGeneration() throws IOException, NoSuchFieldException, IllegalAccessException {
        String s1 = paranamerDataOf("Elephant");
        String s2 = "<init> java.util.Map map \n"
                + "longArray long longs \n"
                + "setMap java.util.Map map \n";
        Assert.assertEquals(s2, s1);
    }

    @Test
    public void testGenericClassGeneration() throws IOException, NoSuchFieldException, IllegalAccessException {
        String s1 = paranamerDataOf("Horse");
        String s2 = "<init> E dumbo \n" +
                "elephantArrays E,java.lang.String theArrayParameter,otherParameter \n" +
                "setElephant E dumboss \n";
        Assert.assertEquals(s2, s1);
    }

    @Test
    public void testGenerationWithInnerClass1() throws IOException, NoSuchFieldException, IllegalAccessException {
        String s1 = paranamerDataOf("Monkey");
        String s2 = "<init> com.thoughtworks.paranamer.generator.Monkey.Banana monkeyEatsBanana \n"
                + "aMethod com.thoughtworks.paranamer.generator.Monkey.Avocado theInnerAvocado \n";
        Assert.assertEquals(s2, s1);
    }

    @Test
    public void testGenerationWithInnerClass2() throws IOException, NoSuchFieldException, IllegalAccessException {
        String s1 = paranamerDataOf("Monkey$Banana");
        String s2 = "bananaMethod int number \n";
        Assert.assertEquals(s2, s1);
    }

    @Test
    public void testGenerationWithMultipleClassesAtSameFile1() throws IOException, NoSuchFieldException, IllegalAccessException {
        String s1 = paranamerDataOf("Chicken");
        String s2 = "aMethod java.lang.String aParamater \n"
                + "strangeMethod com.thoughtworks.paranamer.generator.Fox aFox \n";
        Assert.assertEquals(s2, s1);
    }

    @Test
    public void testGenerationWithMultipleClassesAtSameFile2() throws IOException, NoSuchFieldException, IllegalAccessException {
        String s1 = paranamerDataOf("Dog");
        String s2 = "crazyMethod com.thoughtworks.paranamer.generator.Chicken aChicken \n"
                + "woof java.lang.String woofWoof \n";
        Assert.assertEquals(s2, s1);
    }

    @Test
    public void testGenerationWithClassMismatchingFilename() throws IOException, NoSuchFieldException, IllegalAccessException {
        String s1 = paranamerDataOf("Chameleon");
        String s2 = "thisIsCrazy com.thoughtworks.paranamer.generator.Monkey veryCrazy \n";
        Assert.assertEquals(s2, s1);
    }

    private static class MyClassLoader extends ClassLoader {
        public Class<?> defineEnhancerClass(String name, byte[] bytes, int length) {
            return defineClass("com.thoughtworks.paranamer.generator." + name, bytes, 0, bytes.length);
        }
    }
}