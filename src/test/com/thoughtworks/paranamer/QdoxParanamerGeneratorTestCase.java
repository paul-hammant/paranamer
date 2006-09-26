package com.thoughtworks.paranamer;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class QdoxParanamerGeneratorTestCase extends TestCase {

    String allParameters =
            "com.thoughtworks.paranamer.CachingParanamer CachingParanamer \n" +
                    "com.thoughtworks.paranamer.CachingParanamer CachingParanamer paranamer com.thoughtworks.paranamer.Paranamer \n" +
                    "com.thoughtworks.paranamer.CachingParanamer lookupParameterNamesForMethod method java.lang.reflect.Method \n" +
                    "com.thoughtworks.paranamer.CachingParanamer toString \n" +
                    "com.thoughtworks.paranamer.CachingParanamer lookupParameterNames classLoader,className,methodName java.lang.ClassLoader,java.lang.String,java.lang.String \n" +
                    "com.thoughtworks.paranamer.CachingParanamer lookupConstructor classLoader,className,paramNames java.lang.ClassLoader,java.lang.String,java.lang.String \n" +
                    "com.thoughtworks.paranamer.CachingParanamer lookupMethod classLoader,className,methodName,paramNames java.lang.ClassLoader,java.lang.String,java.lang.String,java.lang.String \n" +
                    "com.thoughtworks.paranamer.CheckedParanamer CheckedParanamer \n" +
                    "com.thoughtworks.paranamer.CheckedParanamer CheckedParanamer delegate com.thoughtworks.paranamer.Paranamer \n" +
                    "com.thoughtworks.paranamer.CheckedParanamer toString \n" +
                    "com.thoughtworks.paranamer.CheckedParanamer checkedConstructorLookup classLoader,className,paramNames java.lang.ClassLoader,java.lang.String,java.lang.String \n" +
                    "com.thoughtworks.paranamer.CheckedParanamer checkedMethodLookup classLoader,className,methodName,paramNames java.lang.ClassLoader,java.lang.String,java.lang.String,java.lang.String \n" +
                    "com.thoughtworks.paranamer.DefaultParanamer DefaultParanamer \n" +
                    "com.thoughtworks.paranamer.DefaultParanamer DefaultParanamer paranamerResource java.lang.String \n" +
                    "com.thoughtworks.paranamer.DefaultParanamer lookupParameterNamesForMethod method java.lang.reflect.Method \n" +
                    "com.thoughtworks.paranamer.DefaultParanamer toString \n" +
                    "com.thoughtworks.paranamer.DefaultParanamer lookupParameterNames classLoader,className,methodName java.lang.ClassLoader,java.lang.String,java.lang.String \n" +
                    "com.thoughtworks.paranamer.DefaultParanamer lookupConstructor classLoader,className,paramNames java.lang.ClassLoader,java.lang.String,java.lang.String \n" +
                    "com.thoughtworks.paranamer.DefaultParanamer lookupMethod classLoader,c,m,p java.lang.ClassLoader,java.lang.String,java.lang.String,java.lang.String\n" +
                    "com.thoughtworks.paranamer.DefaultParanamer lookupMethod classLoader,className,methodName,paramNames java.lang.ClassLoader,java.lang.String,java.lang.String,java.lang.String \n" +
                    "com.thoughtworks.paranamer.ParanamerException ParanamerException message java.lang.String \n" +
                    "com.thoughtworks.paranamer.ParanamerGeneratorMojo execute \n" +
                    "com.thoughtworks.paranamer.ParanamerGeneratorTask execute \n" +
                    "com.thoughtworks.paranamer.ParanamerGeneratorTask setOutputDirectory outputDirectory java.lang.String \n" +
                    "com.thoughtworks.paranamer.ParanamerGeneratorTask setSourceDirectory sourceDirectory java.lang.String \n" +
                    "com.thoughtworks.paranamer.ParanamerRuntimeException ParanamerRuntimeException message java.lang.String \n" +
                    "com.thoughtworks.paranamer.QdoxParanamerGenerator QdoxParanamerGenerator \n" +
                    "com.thoughtworks.paranamer.QdoxParanamerGenerator QdoxParanamerGenerator paranamerResource java.lang.String \n" +
                    "com.thoughtworks.paranamer.QdoxParanamerGenerator generate sourcePath java.lang.String \n" +
                    "com.thoughtworks.paranamer.QdoxParanamerGenerator write outputPath,content java.lang.String,java.lang.String \n" +
                    "com.thoughtworks.paranamer.UncheckedParanamer UncheckedParanamer \n" +
                    "com.thoughtworks.paranamer.UncheckedParanamer UncheckedParanamer delegate com.thoughtworks.paranamer.Paranamer \n" +
                    "com.thoughtworks.paranamer.UncheckedParanamer toString \n" +
                    "com.thoughtworks.paranamer.UncheckedParanamer uncheckedConstructorLookup classLoader,className,paramNames java.lang.ClassLoader,java.lang.String,java.lang.String \n" +
                    "com.thoughtworks.paranamer.UncheckedParanamer uncheckedMethodLookup classLoader,className,methodName,paramNames java.lang.ClassLoader,java.lang.String,java.lang.String,java.lang.String \n";

    private ParanamerGenerator generator;
    
    protected void setUp() throws Exception {
        generator = new QdoxParanamerGenerator();
    }

    public void testCanGenerateParameterNamesFromSource() throws Exception {
        assertEquals(allParameters, generator.generate(getSourcePath()));
    }

    private String getSourcePath() {
        return new File(".").getAbsolutePath() + "/src/java";
    }

    public void testCanWriteParameterNames() throws IOException {
        File dir = createOutputDirectory();
        generator.write(dir.getAbsolutePath(), allParameters);
        String file = new File(dir.getPath()+File.separator+
                ParanamerConstants.DEFAULT_PARANAMER_RESOURCE).getAbsolutePath();
        assertTrue(new File(file).exists());
        assertEquals(ParanamerConstants.HEADER,
                new LineNumberReader(new FileReader(file)).readLine());
    }

    private File createOutputDirectory() {
        File dir = new File("target");
        dir.mkdirs();
        return dir;
    }
 
}
