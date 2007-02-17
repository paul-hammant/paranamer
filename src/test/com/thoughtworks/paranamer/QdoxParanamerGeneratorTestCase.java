package com.thoughtworks.paranamer;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class QdoxParanamerGeneratorTestCase extends TestCase {

    String allParameters =
            "com.thoughtworks.paranamer.AsmParanamer lookupParameterNamesForMethod method java.lang.reflect.Method \n" +
                    "com.thoughtworks.paranamer.AsmParanamer lookupParameterNames classLoader,className,methodName java.lang.ClassLoader,java.lang.String,java.lang.String \n" +
                    "com.thoughtworks.paranamer.AsmParanamer lookupConstructor classLoader,className,paramNames java.lang.ClassLoader,java.lang.String,java.lang.String \n" +
                    "com.thoughtworks.paranamer.AsmParanamer lookupMethod classLoader,className,methodName,paramNames java.lang.ClassLoader,java.lang.String,java.lang.String,java.lang.String \n" +
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
                    "com.thoughtworks.paranamer.MethodCollector MethodCollector previous,ignoreCount,paramCount org.objectweb.asm.MethodVisitor,int,int \n" +
                    "com.thoughtworks.paranamer.MethodCollector isDebugInfoPresent \n" +
                    "com.thoughtworks.paranamer.MethodCollector getResult \n" +
                    "com.thoughtworks.paranamer.MethodCollector visitAnnotation arg0,arg1 java.lang.String,boolean \n" +
                    "com.thoughtworks.paranamer.MethodCollector visitAnnotationDefault \n" +
                    "com.thoughtworks.paranamer.MethodCollector visitParameterAnnotation arg0,arg1,arg2 int,java.lang.String,boolean \n" +
                    "com.thoughtworks.paranamer.MethodCollector visitAttribute arg0 org.objectweb.asm.Attribute \n" +
                    "com.thoughtworks.paranamer.MethodCollector visitCode \n" +
                    "com.thoughtworks.paranamer.MethodCollector visitEnd \n" +
                    "com.thoughtworks.paranamer.MethodCollector visitFieldInsn arg0,arg1,arg2,arg3 int,java.lang.String,java.lang.String,java.lang.String \n" +
                    "com.thoughtworks.paranamer.MethodCollector visitFrame arg0,arg1,arg2,arg3,arg4 int,int,java.lang.Object[],int,java.lang.Object[] \n" +
                    "com.thoughtworks.paranamer.MethodCollector visitIincInsn arg0,arg1 int,int \n" +
                    "com.thoughtworks.paranamer.MethodCollector visitInsn arg0 int \n" +
                    "com.thoughtworks.paranamer.MethodCollector visitIntInsn arg0,arg1 int,int \n" +
                    "com.thoughtworks.paranamer.MethodCollector visitJumpInsn arg0,arg1 int,org.objectweb.asm.Label \n" +
                    "com.thoughtworks.paranamer.MethodCollector visitLabel arg0 org.objectweb.asm.Label \n" +
                    "com.thoughtworks.paranamer.MethodCollector visitLdcInsn arg0 java.lang.Object \n" +
                    "com.thoughtworks.paranamer.MethodCollector visitLineNumber arg0,arg1 int,org.objectweb.asm.Label \n" +
                    "com.thoughtworks.paranamer.MethodCollector visitLocalVariable name,desc,signature,start,end,index java.lang.String,java.lang.String,java.lang.String,org.objectweb.asm.Label,org.objectweb.asm.Label,int \n" +
                    "com.thoughtworks.paranamer.MethodCollector visitLookupSwitchInsn arg0,arg1,arg2 org.objectweb.asm.Label,int[],org.objectweb.asm.Label[] \n" +
                    "com.thoughtworks.paranamer.MethodCollector visitMaxs arg0,arg1 int,int \n" +
                    "com.thoughtworks.paranamer.MethodCollector visitMethodInsn arg0,arg1,arg2,arg3 int,java.lang.String,java.lang.String,java.lang.String \n" +
                    "com.thoughtworks.paranamer.MethodCollector visitMultiANewArrayInsn arg0,arg1 java.lang.String,int \n" +
                    "com.thoughtworks.paranamer.MethodCollector visitTableSwitchInsn arg0,arg1,arg2,arg3 int,int,org.objectweb.asm.Label,org.objectweb.asm.Label[] \n" +
                    "com.thoughtworks.paranamer.MethodCollector visitTryCatchBlock arg0,arg1,arg2,arg3 org.objectweb.asm.Label,org.objectweb.asm.Label,org.objectweb.asm.Label,java.lang.String \n" +
                    "com.thoughtworks.paranamer.MethodCollector visitTypeInsn arg0,arg1 int,java.lang.String \n" +
                    "com.thoughtworks.paranamer.MethodCollector visitVarInsn arg0,arg1 int,int \n" +
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
                    "com.thoughtworks.paranamer.TypeCollector TypeCollector methodName,parameterTypes java.lang.String,java.lang.Class[] \n" +
                    "com.thoughtworks.paranamer.TypeCollector getParameterNamesForMethod \n" +
                    "com.thoughtworks.paranamer.TypeCollector visitAnnotation arg0,arg1 java.lang.String,boolean \n" +
                    "com.thoughtworks.paranamer.TypeCollector visitField arg0,arg1,arg2,arg3,arg4 int,java.lang.String,java.lang.String,java.lang.String,java.lang.Object \n" +
                    "com.thoughtworks.paranamer.TypeCollector visitMethod access,name,desc,signature,exceptions int,java.lang.String,java.lang.String,java.lang.String,java.lang.String[] \n" +
                    "com.thoughtworks.paranamer.TypeCollector visit arg0,arg1,arg2,arg3,arg4,arg5 int,int,java.lang.String,java.lang.String,java.lang.String,java.lang.String[] \n" +
                    "com.thoughtworks.paranamer.TypeCollector visitAttribute arg0 org.objectweb.asm.Attribute \n" +
                    "com.thoughtworks.paranamer.TypeCollector visitEnd \n" +
                    "com.thoughtworks.paranamer.TypeCollector visitInnerClass arg0,arg1,arg2,arg3 java.lang.String,java.lang.String,java.lang.String,int \n" +
                    "com.thoughtworks.paranamer.TypeCollector visitOuterClass arg0,arg1,arg2 java.lang.String,java.lang.String,java.lang.String \n" +
                    "com.thoughtworks.paranamer.TypeCollector visitSource arg0,arg1 java.lang.String,java.lang.String \n" +
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
