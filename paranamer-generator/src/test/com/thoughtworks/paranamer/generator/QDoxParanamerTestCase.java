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
