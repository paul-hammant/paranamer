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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import junit.framework.TestCase;

public class QDoxParanamerTestCase extends TestCase {

    private String root;

    protected void setUp() throws Exception {
        ParanamerGenerator generator = new QdoxParanamerGenerator();
        root = new File(".").getAbsolutePath();
        generator.processSourcePath(root + "/src/test", root + "/target/test-classes/");
    }

    public void doNot_testFoo() throws IOException, NoSuchFieldException {

        FileInputStream fis = new FileInputStream(root + "/target/test-classes/com/thoughtworks/paranamer/generator/Elephant.class");
        byte[] bytes = new byte[4000];
        int read = fis.read(bytes);

        MyClassLoader cl = new MyClassLoader();

        Class enhancedClazz = cl.defineEnhancerClass(bytes, read);
        Field f = enhancedClazz.getField("__PARANAMER_DATA");
        assertNotNull(f);
    }


    public void testNothing() {
        
    }

    private static class MyClassLoader extends ClassLoader {
        public Class defineEnhancerClass(byte[] bytes, int length) {
            return defineClass("com.thoughtworks.paranamer.generator.Elephant", bytes, 0, bytes.length);
        }
    }
}
