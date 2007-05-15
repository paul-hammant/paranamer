package com.thoughtworks.paranamer.generator;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

public class QDoxParanamerTestCase extends TestCase {

    private String root;

    protected void setUp() throws Exception {
        String filePath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File path = new File(filePath);
        while(!path.getAbsolutePath().endsWith("trunk")) {
            path = path.getParentFile();
        }

        ParanamerGenerator generator = new QdoxParanamerGenerator();
        root = new File(path, "paranamer-generator").getAbsolutePath();
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
        public Class defineEnhancerClass(byte[] b, int len) {
            return defineClass("com.thoughtworks.paranamer.generator.Elephant", b, 0, b.length);
        }
    }
}
