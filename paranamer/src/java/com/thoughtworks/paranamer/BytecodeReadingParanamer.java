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

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
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
 * Portions of this source file are a fork of ASM.
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
        InputStream content = getClassAsStream(declaringClass);
        if (content == null) {
            if (throwExceptionIfMissing) {
                throw new ParameterNamesNotFoundException("Unable to get class bytes");
            } else {
                return Paranamer.EMPTY_NAMES;
            }
        }
        try {
            ClassReader reader = new ClassReader(content);
            TypeCollector visitor = new TypeCollector(name, types, throwExceptionIfMissing);
            reader.accept(visitor, ClassReader.EXPAND_FRAMES);
            return visitor.getParameterNamesForMethod();
        } catch (IOException e) {
            if (throwExceptionIfMissing) {
                throw new ParameterNamesNotFoundException("IoException while reading class bytes", e);
            } else {
                return Paranamer.EMPTY_NAMES;
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
            super(null);
            this.methodName = methodName;
            this.parameterTypes = parameterTypes;
            this.throwExceptionIfMissing = throwExceptionIfMissing;
            this.collector = null;
        }

        public MethodCollector visitMethod(int access, String name, String desc) {
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
            if (s.endsWith("[]")) {
                String prefix = s.substring(0, s.length() - 2);
                if (primitives.containsKey(prefix)) {
                    s = "[" + primitives.get(prefix);
                } else {
                s = "[L" + prefix + ";";
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
    private static class MethodCollector {

        private final int paramCount;

        private final int ignoreCount;

        private int currentParameter;

        private final StringBuffer result;

        private boolean debugInfoPresent;

        private MethodCollector(int ignoreCount, int paramCount) {
            this.ignoreCount = ignoreCount;
            this.paramCount = paramCount;
            this.result = new StringBuffer();
            this.currentParameter = 0;
            // if there are 0 parameters, there is no need for debug info
            this.debugInfoPresent = paramCount == 0;
        }

        public void visitLocalVariable(String name, int index) {
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
