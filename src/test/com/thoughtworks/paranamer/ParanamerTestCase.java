package com.thoughtworks.paranamer;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.lang.reflect.Method;

public class ParanamerTestCase extends TestCase {

    String allParameters =
            "com.thoughtworks.paranamer.CachingParanamer CachingParanamer \n" +
                    "com.thoughtworks.paranamer.CachingParanamer CachingParanamer paranamer com.thoughtworks.paranamer.Paranamer \n" +
                    "com.thoughtworks.paranamer.CachingParanamer lookupParameterNames classLoader,className,methodName java.lang.ClassLoader,java.lang.String,java.lang.String \n" +
                    "com.thoughtworks.paranamer.CachingParanamer lookupConstructor classLoader,className,paramNames java.lang.ClassLoader,java.lang.String,java.lang.String \n" +
                    "com.thoughtworks.paranamer.CachingParanamer lookupMethod classLoader,className,methodName,paramNames java.lang.ClassLoader,java.lang.String,java.lang.String,java.lang.String \n" +
                    "com.thoughtworks.paranamer.CheckedParanamer CheckedParanamer \n" +
                    "com.thoughtworks.paranamer.CheckedParanamer CheckedParanamer delegate com.thoughtworks.paranamer.Paranamer \n" +
                    "com.thoughtworks.paranamer.CheckedParanamer checkedConstructorLookup classLoader,className,paramNames java.lang.ClassLoader,java.lang.String,java.lang.String \n" +
                    "com.thoughtworks.paranamer.CheckedParanamer checkedMethodLookup classLoader,className,methodName,paramNames java.lang.ClassLoader,java.lang.String,java.lang.String,java.lang.String \n" +
                    "com.thoughtworks.paranamer.ParanamerException ParanamerException message java.lang.String \n" +
                    "com.thoughtworks.paranamer.ParanamerImpl lookupParameterNames classLoader,className,methodName java.lang.ClassLoader,java.lang.String,java.lang.String \n" +
                    "com.thoughtworks.paranamer.ParanamerImpl lookupConstructor classLoader,className,paramNames java.lang.ClassLoader,java.lang.String,java.lang.String \n" +
                    "com.thoughtworks.paranamer.ParanamerImpl lookupMethod classLoader,c,m,p java.lang.ClassLoader,java.lang.String,java.lang.String,java.lang.String\n" +
                    "com.thoughtworks.paranamer.ParanamerImpl lookupMethod classLoader,className,methodName,paramNames java.lang.ClassLoader,java.lang.String,java.lang.String,java.lang.String \n" +
                    "com.thoughtworks.paranamer.ParanamerMojo execute \n" +
                    "com.thoughtworks.paranamer.ParanamerRuntimeException ParanamerRuntimeException message java.lang.String \n" +
                    "com.thoughtworks.paranamer.ParanamerTask execute \n" +
                    "com.thoughtworks.paranamer.ParanamerTask setOutputDirectory outputDirectory java.lang.String \n" +
                    "com.thoughtworks.paranamer.ParanamerTask setSourceDirectory sourceDirectory java.lang.String \n" +
                    "com.thoughtworks.paranamer.QdoxParanamerGenerator generate sourcePath java.lang.String \n" +
                    "com.thoughtworks.paranamer.QdoxParanamerGenerator write outputPath,parameterText java.lang.String,java.lang.String \n" +
                    "com.thoughtworks.paranamer.UncheckedParanamer UncheckedParanamer \n" +
                    "com.thoughtworks.paranamer.UncheckedParanamer UncheckedParanamer delegate com.thoughtworks.paranamer.Paranamer \n" +
                    "com.thoughtworks.paranamer.UncheckedParanamer uncheckedConstructorLookup classLoader,className,paramNames java.lang.ClassLoader,java.lang.String,java.lang.String \n" +
                    "com.thoughtworks.paranamer.UncheckedParanamer uncheckedMethodLookup classLoader,className,methodName,paramNames java.lang.ClassLoader,java.lang.String,java.lang.String,java.lang.String \n";
    String expected1 = "com.thoughtworks.paranamer.Paranamer lookupMethod clazz,classMethodAndParamNames java.lang.Class,java.lang.String\n";

    private String parameterSignatures;

    protected void setUp() throws Exception {
        parameterSignatures = new QdoxParanamerGenerator().generate(new File(".").getAbsolutePath() + "/src/java");
    }

    public void testGenerationOfParamNameDataDoesSo() {
        assertEquals(allParameters, parameterSignatures);
    }

    public void testWritingOfParamNameDataWorks() throws IOException {
        File dir = new File("target/classes/");
        dir.mkdirs();
        new QdoxParanamerGenerator().write(dir.getAbsolutePath(), allParameters);
        String file = new File("target/classes/META-INF/ParameterNames.txt").getAbsolutePath();
        assertTrue(new File(file).exists());
        assertEquals("format version 1.0",
                new LineNumberReader(new FileReader(file)).readLine());
    }

    public void testMethodCantBeRetrievedIfItAintThere() throws IOException {
        Object method = new ParanamerImpl().lookupMethod(Paranamer.class.getClassLoader(), "com.thoughtworks.paranamer.QdoxParanamerGenerator", "generate", "hello,goodbye");
        assertNull(method);
    }

    public void testBogusClassEndsLookup() throws IOException {
        Object method = new ParanamerImpl().lookupMethod(Paranamer.class.getClassLoader(), "foo.Bar", "generate", "hello,goodbye");
        assertNull(method);
    }

    public void testMissingMetaInfEndsLookup() throws IOException {
        File file = new File("target/classes/META-INF/ParameterNames.txt");
        file.delete();
        assertFalse(file.exists());
        Object method = new ParanamerImpl().lookupMethod(Paranamer.class.getClassLoader(), "com.thoughtworks.paranamer.QdoxParanamerGenerator", "generate", "sourcePath,rootPackage");
        assertNull(method);
    }

    public void testMethodCantBeRetrievedForClassThatAintThere() throws IOException {
        Object method = new ParanamerImpl().lookupMethod(Paranamer.class.getClassLoader(), "paranamer.Footle", "generate", "sourcePath,rootPackage");
        assertNull(method);
    }

    public void testMethodRetrievalFailureIfNoParametersTextFile() throws IOException {
        new File("/Users/paul/scm/oss/Paranamer/classes/META-INF/ParameterNames.txt").delete();
        Object method = new ParanamerImpl().lookupMethod(Paranamer.class.getClassLoader(), "com.thoughtworks.paranamer.QdoxParanamerGenerator", "generate", "hello,goodbye");
        assertNull(method);
    }

    public void testMethodWithNoArgsCanBeRetrievedByParameterNames() throws IOException, NoSuchMethodException {
        File dir = new File("target/classes/");
        new QdoxParanamerGenerator().write(dir.getAbsolutePath(), allParameters);
        Method method = new ParanamerImpl().lookupMethod(Paranamer.class.getClassLoader(), "com.thoughtworks.paranamer.ParanamerMojo", "execute", "");
        assertEquals(ParanamerMojo.class.getMethod("execute", new Class[0]), method);
    }




}
