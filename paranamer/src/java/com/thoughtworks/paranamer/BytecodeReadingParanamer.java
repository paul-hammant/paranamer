/***
 *
 * Portions Copyright (c) 2007 Paul Hammant
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

package com.thoughtworks.paranamer;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;


/**
 * An ASM-based implementation of Paranamer. It relies on debug information compiled
 * with the "-g" javac option to retrieve parameter names.
 * <p/>
 *
 * @author Guilherme Silveira
 * @author Paul Hammant
 */
public class BytecodeReadingParanamer implements Paranamer {

    private static final Map<String, String> primitives = new HashMap<String, String>() {
        {
            put("int","I");
            put("boolean","Z");
            put("char","C");
            put("short","B");
            put("float","F");
            put("long","J");
            put("double","D");
        }
    };

    public String[] lookupParameterNames(AccessibleObject methodOrConstructor) {
        return lookupParameterNames(methodOrConstructor, true);
    }

    public String[] lookupParameterNames(AccessibleObject methodOrCtor, boolean throwExceptionIfMissing) {

        Class<?>[] types = null;
        Class<?> declaringClass = null;
        String name = null;
        if (methodOrCtor instanceof Method) {
            Method method = (Method) methodOrCtor;
            types = method.getParameterTypes();
            name = method.getName();
            declaringClass = method.getDeclaringClass();
        } else {
            Constructor<?> constructor = (Constructor<?>) methodOrCtor;
            types = constructor.getParameterTypes();
            declaringClass = constructor.getDeclaringClass();
            name = "<init>";
        }

        if (types.length == 0) {
            return EMPTY_NAMES;
        }
        InputStream byteCodeStream = getClassAsStream(declaringClass);
        if (byteCodeStream == null) {
            if (throwExceptionIfMissing) {
                throw new ParameterNamesNotFoundException("Unable to get class bytes");
            } else {
                return Paranamer.EMPTY_NAMES;
            }
        }
        try {
            ClassReader reader = new ClassReader(byteCodeStream);
            TypeCollector visitor = new TypeCollector(name, types, throwExceptionIfMissing);
            reader.accept(visitor, ClassReader.EXPAND_FRAMES);
            String[] parameterNamesForMethod = visitor.getParameterNamesForMethod();
            return parameterNamesForMethod;
        } catch (IOException e) {
            if (throwExceptionIfMissing) {
                throw new ParameterNamesNotFoundException("IoException while reading class bytes", e);
            } else {
                return Paranamer.EMPTY_NAMES;
            }
        } finally {
            try {
                byteCodeStream.close();
            } catch (IOException e) {
            }
        }
    }

    private InputStream getClassAsStream(Class<?> clazz) {
        ClassLoader classLoader = clazz.getClassLoader();
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        return getClassAsStream(classLoader, clazz.getName());
    }

    private InputStream getClassAsStream(ClassLoader classLoader, String className) {
        String name = className.replace('.', '/') + ".class";
        // better pre-cache all methods otherwise this content will be loaded
        // multiple times
        InputStream asStream = classLoader.getResourceAsStream(name);
        if (asStream == null) {
            asStream = BytecodeReadingParanamer.class.getResourceAsStream(name);
        }
        return asStream;
    }

    /**
     * The type collector waits for an specific method in order to start a method
     * collector.
     *
     * @author Guilherme Silveira
     */
    private static class TypeCollector extends ClassAdapter {

        private static final String COMMA = ",";

        private final String methodName;

        private final Class<?>[] parameterTypes;
        private final boolean throwExceptionIfMissing;

        private MethodCollector collector;

        private TypeCollector(String methodName, Class<?>[] parameterTypes, boolean throwExceptionIfMissing) {
            super(new ClassVisitor() {
                public void visit(int i, int i1, String s, String s1, String s2, String[] strings) {

                }

                public void visitSource(String s, String s1) {

                }

                public void visitOuterClass(String s, String s1, String s2) {

                }

                public AnnotationVisitor visitAnnotation(String s, boolean b) {
                    return null;
                }

                public void visitAttribute(Attribute attribute) {

                }

                public void visitInnerClass(String s, String s1, String s2, int i) {

                }

                public FieldVisitor visitField(int i, String s, String s1, String s2, Object o) {
                    return null;
                }

                public MethodVisitor visitMethod(int i, String s, String s1, String s2, String[] strings) {
                    return null;
                }

                public void visitEnd() {

                }
            });
            this.methodName = methodName;
            this.parameterTypes = parameterTypes;
            this.throwExceptionIfMissing = throwExceptionIfMissing;
            this.collector = null;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String s2, String[] strings) {
            // already found the method, skip any processing
            if (collector != null) {
                return null;
            }
            // not the same name
            if (!name.equals(methodName)) {
                return null;
            }
            Type[] argumentTypes = Type.getArgumentTypes(desc);
            int longOrDoubleQuantity = 0;
            for (Type t : argumentTypes) {
                if (t.getClassName().equals("long")
                        || t.getClassName().equals("double")) {
                    longOrDoubleQuantity++;
                }
            }
            int paramCount = argumentTypes.length;
            // not the same quantity of parameters
            if (paramCount != this.parameterTypes.length) {
                return null;
            }
            for (int i = 0; i < argumentTypes.length; i++) {
                if (!correctTypeName(argumentTypes, i).equals(
                        this.parameterTypes[i].getName())) {
                    return null;
                }
            }
            this.collector = new MethodCollector((Modifier.isStatic(access) ? 0 : 1),
                    argumentTypes.length + longOrDoubleQuantity);
            return collector;
        }

        private String correctTypeName(Type[] argumentTypes, int i) {
            String s = argumentTypes[i].getClassName();
            // array notation needs cleanup.
            String braces = "";
            while (s.endsWith("[]")) {
                braces = braces + "[";
                s = s.substring(0, s.length() - 2);
            }
            if (!braces.equals("")) {
                if (primitives.containsKey(s)) {
                    s = braces + primitives.get(s);
                } else {
                s = braces + "L" + s + ";";
                }
            }
            return s;
        }

        private String[] getParameterNamesForMethod() {
            if (collector == null) {
                return Paranamer.EMPTY_NAMES;
            }
            if (!collector.isDebugInfoPresent()) {
                if (throwExceptionIfMissing) {
                    throw new ParameterNamesNotFoundException("Parameter names not found for " + methodName);
                } else {
                    return Paranamer.EMPTY_NAMES;
                }
            }
            return collector.getResult().split(COMMA);
        }

    }

    /**
     * Objects of this class collects information from a specific method.
     *
     * @author Guilherme Silveira
     */
    private static class MethodCollector extends MethodAdapter {

        private final int paramCount;

        private final int ignoreCount;

        private int currentParameter;

        private final StringBuffer result;

        private boolean debugInfoPresent;

        private MethodCollector(int ignoreCount, int paramCount) {
            super(new MethodVisitor() {
                public AnnotationVisitor visitAnnotationDefault() {
                    return null;
                }

                public AnnotationVisitor visitAnnotation(String s, boolean b) {
                    return null;
                }

                public AnnotationVisitor visitParameterAnnotation(int i, String s, boolean b) {
                    return null;
                }

                public void visitAttribute(Attribute attribute) {

                }

                public void visitCode() {

                }

                public void visitFrame(int i, int i1, Object[] objects, int i2, Object[] objects1) {

                }

                public void visitInsn(int i) {

                }

                public void visitIntInsn(int i, int i1) {

                }

                public void visitVarInsn(int i, int i1) {

                }

                public void visitTypeInsn(int i, String s) {

                }

                public void visitFieldInsn(int i, String s, String s1, String s2) {

                }

                public void visitMethodInsn(int i, String s, String s1, String s2) {

                }

                public void visitJumpInsn(int i, Label label) {

                }

                public void visitLabel(Label label) {

                }

                public void visitLdcInsn(Object o) {

                }

                public void visitIincInsn(int i, int i1) {

                }

                public void visitTableSwitchInsn(int i, int i1, Label label, Label[] labels) {

                }

                public void visitLookupSwitchInsn(Label label, int[] ints, Label[] labels) {

                }

                public void visitMultiANewArrayInsn(String s, int i) {

                }

                public void visitTryCatchBlock(Label label, Label label1, Label label2, String s) {

                }

                public void visitLocalVariable(String s, String s1, String s2, Label label, Label label1, int i) {

                }

                public void visitLineNumber(int i, Label label) {

                }

                public void visitMaxs(int i, int i1) {

                }

                public void visitEnd() {

                }
            });
            this.ignoreCount = ignoreCount;
            this.paramCount = paramCount;
            this.result = new StringBuffer();
            this.currentParameter = 0;
            // if there are 0 parameters, there is no need for debug info
            this.debugInfoPresent = paramCount == 0;
        }

        @Override
        public void visitLocalVariable(String name, String s1, String s2, Label label, Label label1, int index) {
            if (index >= ignoreCount && index < ignoreCount + paramCount) {
                if (!name.equals("arg" + currentParameter)) {
                    debugInfoPresent = true;
                }
                result.append(',');
                result.append(name);
                currentParameter++;
            }
        }

        private String getResult() {
            return result.length() != 0 ? result.substring(1) : "";
        }

        private boolean isDebugInfoPresent() {
            return debugInfoPresent;
        }

    }


}
