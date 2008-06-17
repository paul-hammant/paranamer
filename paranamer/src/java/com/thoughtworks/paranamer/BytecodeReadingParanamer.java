/***
 *
 * Portions Copyright (c) 2007 Paul Hammant
 * Portions copyright (c) 2000-2007 INRIA, France Telecom
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

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;


/**
 * An ASM-based implementation of Paranamer. It relies on debug information compiled
 * with the "-g" javac option to retrieve parameter names.
 *
 * Portions of this source file are a fork of ASM.
 *
 * @author Guilherme Silveira
 * @author Paul Hammant
 */
public class BytecodeReadingParanamer implements Paranamer {

    public String[] lookupParameterNames(AccessibleObject methodOrCtor) {

        Class[] types = null;
        Class declaringClass = null;
        String name = null;
        if (methodOrCtor instanceof Method) {
            Method method = (Method) methodOrCtor;
            types = method.getParameterTypes();
            name = method.getName();
            declaringClass = method.getDeclaringClass();
        } else {
            Constructor constructor = (Constructor) methodOrCtor;
            types = constructor.getParameterTypes();
            declaringClass = constructor.getDeclaringClass();
            name = "<init>";
        }

        InputStream content = getClassAsStream(declaringClass);
        try {
            ClassReader reader = new ClassReader(content);
            TypeCollector visitor = new TypeCollector(name, types);
            reader.accept(visitor);
            return visitor.getParameterNamesForMethod();
        } catch (IOException e) {
            return null;
        }
    }

    public int areParameterNamesAvailable(Class clazz, String constructorOrMethodName) {
        InputStream content = getClassAsStream(clazz.getClassLoader(), clazz.getName());
        if (content == null) {
            return NO_PARAMETER_NAMES_FOR_CLASS;
        }
        try {
            ClassReader reader = new ClassReader(content);
            //TODO - also for constructors
            List methods = getMatchingMethods(clazz.getClassLoader(), clazz.getName(), constructorOrMethodName);
            if (methods.size() == 0) {
                return Paranamer.NO_PARAMETER_NAMES_FOR_CLASS_AND_MEMBER;
            }
            TypeCollector visitor = new TypeCollector(constructorOrMethodName, ((Method) methods.get(0)).getParameterTypes());
            reader.accept(visitor);
            if (visitor.isClassFound()) {
                if (!visitor.isMethodFound()) {
                    return Paranamer.NO_PARAMETER_NAMES_FOR_CLASS_AND_MEMBER;
                }
            } else {
                return Paranamer.NO_PARAMETER_NAMES_FOR_CLASS;
            }
            return Paranamer.PARAMETER_NAMES_FOUND;
        } catch (IOException e) {
            throw new ParameterNamesNotFoundException("IoException while reading class bytes", e);
        } catch (ClassNotFoundException e) {
            throw new ParameterNamesNotFoundException("ClassNotFoundException while reading class bytes", e);
        }
    }

    private InputStream getClassAsStream(Class clazz) {
        ClassLoader classLoader = clazz.getClassLoader();
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

    private List getMatchingMethods(ClassLoader classLoader, String className, String name) throws ClassNotFoundException {
        List list = new ArrayList();
        Method[] methods = classLoader.loadClass(className).getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equals(name)) {
                list.add(method);
            }
        }
        return list;
    }

    /**
     * The type collector waits for an specific method in order to start a method
     * collector.
     *
     * @author Guilherme Silveira
     */
    private static class TypeCollector {

        private static final String COMMA = ",";

        private final String methodName;

        private final Class[] parameterTypes;

        private MethodCollector collector;
        private boolean methodFound = false;
        private boolean classFound = false;

        private TypeCollector(String methodName, Class[] parameterTypes) {
            this.methodName = methodName;
            this.parameterTypes = parameterTypes;
            this.collector = null;
        }

        public MethodCollector visitMethod(int access, String name, String desc) {
            // already found the method, skip any processing
            classFound = true;
            if (collector != null) {
                return null;
            }
            // not the same name
            if (!name.equals(methodName)) {
                return null;
            }
            methodFound = true;
            Type[] argumentTypes = Type.getArgumentTypes(desc);
            int longOrDoubleQuantity = 0;
            for (int i1 = 0; i1 < argumentTypes.length; i1++) {
                Type t = argumentTypes[i1];
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
                s = "[L" + s.substring(0, s.length() - 2) + ";";
            }
            return s;
        }

        private String[] getParameterNamesForMethod() {
            if (collector == null) {
                return null;
            }
            if (!collector.isDebugInfoPresent()) {
                throw new ParameterNamesNotFoundException("Parameter names not found for " + methodName);
            }
            return collector.getResult().split(COMMA);
        }

        private boolean isMethodFound() {
            return methodFound;
        }

        private boolean isClassFound() {
            return classFound;
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
            this.debugInfoPresent = paramCount == 0 ? true : false;
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

/***
 * Portions Copyright (c) 2007 Paul Hammant
 * Portions copyright (c) 2000-2007 INRIA, France Telecom
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


    /**
     * A Java class parser to make a Class Visitor visit an existing class.
     * This class parses a byte array conforming to the Java class file format and
     * calls the appropriate visit methods of a given class visitor for each field,
     * method and bytecode instruction encountered.
     *
     * @author Eric Bruneton
     * @author Eugene Kuleshov
     */
    private static class ClassReader {

        /**
         * The class to be parsed. <i>The content of this array must not be
         * modified. This field is intended for Attribute sub classes, and
         * is normally not needed by class generators or adapters.</i>
         */
        public final byte[] b;

        /**
         * The start index of each constant pool item in {@link #b b}, plus one.
         * The one byte offset skips the constant pool item tag that indicates its
         * type.
         */
        private final int[] items;

        /**
         * The String objects corresponding to the CONSTANT_Utf8 items. This cache
         * avoids multiple parsing of a given CONSTANT_Utf8 constant pool item,
         * which GREATLY improves performances (by a factor 2 to 3). This caching
         * strategy could be extended to all constant pool items, but its benefit
         * would not be so great for these items (because they are much less
         * expensive to parse than CONSTANT_Utf8 items).
         */
        private final String[] strings;

        /**
         * Maximum length of the strings contained in the constant pool of the
         * class.
         */
        private final int maxStringLength;

        /**
         * Start index of the class header information (access, name...) in
         * {@link #b b}.
         */
        public final int header;


        /**
         * The type of CONSTANT_Fieldref constant pool items.
         */
        final static int FIELD = 9;

        /**
         * The type of CONSTANT_Methodref constant pool items.
         */
        final static int METH = 10;

        /**
         * The type of CONSTANT_InterfaceMethodref constant pool items.
         */
        final static int IMETH = 11;

        /**
         * The type of CONSTANT_Integer constant pool items.
         */
        final static int INT = 3;

        /**
         * The type of CONSTANT_Float constant pool items.
         */
        final static int FLOAT = 4;

        /**
         * The type of CONSTANT_Long constant pool items.
         */
        final static int LONG = 5;

        /**
         * The type of CONSTANT_Double constant pool items.
         */
        final static int DOUBLE = 6;

        /**
         * The type of CONSTANT_NameAndType constant pool items.
         */
        final static int NAME_TYPE = 12;

        /**
         * The type of CONSTANT_Utf8 constant pool items.
         */
        final static int UTF8 = 1;

        // ------------------------------------------------------------------------
        // Constructors
        // ------------------------------------------------------------------------

        /**
         * Constructs a new {@link ClassReader} object.
         *
         * @param b the bytecode of the class to be read.
         */
        private ClassReader(final byte[] b) {
            this(b, 0);
        }

        /**
         * Constructs a new {@link ClassReader} object.
         *
         * @param b   the bytecode of the class to be read.
         * @param off the start offset of the class data.
         */
        private ClassReader(final byte[] b, final int off) {
            this.b = b;
            // parses the constant pool
            items = new int[readUnsignedShort(off + 8)];
            int n = items.length;
            strings = new String[n];
            int max = 0;
            int index = off + 10;
            for (int i = 1; i < n; ++i) {
                items[i] = index + 1;
                int size;
                switch (b[index]) {
                    case FIELD:
                    case METH:
                    case IMETH:
                    case INT:
                    case FLOAT:
                    case NAME_TYPE:
                        size = 5;
                        break;
                    case LONG:
                    case DOUBLE:
                        size = 9;
                        ++i;
                        break;
                    case UTF8:
                        size = 3 + readUnsignedShort(index + 1);
                        if (size > max) {
                            max = size;
                        }
                        break;
                        // case HamConstants.CLASS:
                        // case HamConstants.STR:
                    default:
                        size = 3;
                        break;
                }
                index += size;
            }
            maxStringLength = max;
            // the class header information starts just after the constant pool
            header = index;
        }


        /**
         * Constructs a new {@link ClassReader} object.
         *
         * @param is an input stream from which to read the class.
         * @throws IOException if a problem occurs during reading.
         */
        private ClassReader(final InputStream is) throws IOException {
            this(readClass(is));
        }

        /**
         * Reads the bytecode of a class.
         *
         * @param is an input stream from which to read the class.
         * @return the bytecode read from the given input stream.
         * @throws IOException if a problem occurs during reading.
         */
        private static byte[] readClass(final InputStream is) throws IOException {
            if (is == null) {
                throw new IOException("Class not found");
            }
            byte[] b = new byte[is.available()];
            int len = 0;
            while (true) {
                int n = is.read(b, len, b.length - len);
                if (n == -1) {
                    if (len < b.length) {
                        byte[] c = new byte[len];
                        System.arraycopy(b, 0, c, 0, len);
                        b = c;
                    }
                    return b;
                }
                len += n;
                if (len == b.length) {
                    byte[] c = new byte[b.length + 1000];
                    System.arraycopy(b, 0, c, 0, len);
                    b = c;
                }
            }
        }

        // ------------------------------------------------------------------------
        // Public methods
        // ------------------------------------------------------------------------

        /**
         * Makes the given visitor visit the Java class of this {@link ClassReader}.
         * This class is the one specified in the constructor (see
         * {@link #ClassReader(byte[]) ClassReader}).
         *
         * @param classVisitor the visitor that must visit this class.
         */
        private void accept(final TypeCollector classVisitor) {
            char[] c = new char[maxStringLength]; // buffer used to read strings
            int i, j, k; // loop variables
            int u, v, w; // indexes in b

            String attrName;
            int anns = 0;
            int ianns = 0;

            // visits the header
            u = header;
            v = items[readUnsignedShort(u + 4)];
            int len = readUnsignedShort(u + 6);
            w = 0;
            u += 8;
            for (i = 0; i < len; ++i) {
                u += 2;
            }
            v = u;
            i = readUnsignedShort(v);
            v += 2;
            for (; i > 0; --i) {
                j = readUnsignedShort(v + 6);
                v += 8;
                for (; j > 0; --j) {
                    v += 6 + readInt(v + 2);
                }
            }
            i = readUnsignedShort(v);
            v += 2;
            for (; i > 0; --i) {
                j = readUnsignedShort(v + 6);
                v += 8;
                for (; j > 0; --j) {
                    v += 6 + readInt(v + 2);
                }
            }

            i = readUnsignedShort(v);
            v += 2;
            for (; i > 0; --i) {
                v += 6 + readInt(v + 2);
            }

            // visits the class annotations
            for (i = 1; i >= 0; --i) {
                v = i == 0 ? ianns : anns;
                if (v != 0) {
                    v += 2;
                }
            }

            // visits the fields
            i = readUnsignedShort(u);
            u += 2;
            for (; i > 0; --i) {
                j = readUnsignedShort(u + 6);
                u += 8;
                for (; j > 0; --j) {
                    u += 6 + readInt(u + 2);
                }
            }

            // visits the methods
            i = readUnsignedShort(u);
            u += 2;
            for (; i > 0; --i) {
                u = readMethod(classVisitor, c, u);
            }

        }

        private int readMethod(TypeCollector classVisitor, char[] c, int u) {
            int v;
            int w;
            int j;
            String attrName;
            int k;
            int access = readUnsignedShort(u);
            String name = readUTF8(u + 2, c);
            String desc = readUTF8(u + 4, c);
            v = 0;
            w = 0;

            // looks for Code and Exceptions attributes
            j = readUnsignedShort(u + 6);
            u += 8;
            for (; j > 0; --j) {
                attrName = readUTF8(u, c);
                int attrSize = readInt(u + 2);
                u += 6;
                // tests are sorted in decreasing frequency order
                // (based on frequencies observed on typical classes)
                if (attrName.equals("Code")) {
                    v = u;
                }
                u += attrSize;
            }
            // reads declared exceptions
            if (w == 0) {
            } else {
                w += 2;
                for (j = 0; j < readUnsignedShort(w); ++j) {
                    w += 2;
                }
            }

            // visits the method's code, if any
            MethodCollector mv = classVisitor.visitMethod(access, name, desc);

            if (mv != null && v != 0) {
                int codeLength = readInt(v + 4);
                v += 8;

                int codeStart = v;
                int codeEnd = v + codeLength;
                v = codeEnd;

                j = readUnsignedShort(v);
                v += 2;
                for (; j > 0; --j) {
                    v += 8;
                }
                // parses the local variable, line number tables, and code
                // attributes
                int varTable = 0;
                int varTypeTable = 0;
                j = readUnsignedShort(v);
                v += 2;
                for (; j > 0; --j) {
                    attrName = readUTF8(v, c);
                    if (attrName.equals("LocalVariableTable")) {
                        varTable = v + 6;
                    } else if (attrName.equals("LocalVariableTypeTable")) {
                        varTypeTable = v + 6;
                    }
                    v += 6 + readInt(v + 2);
                }

                v = codeStart;
                // visits the local variable tables
                if (varTable != 0) {
                    if (varTypeTable != 0) {
                        k = readUnsignedShort(varTypeTable) * 3;
                        w = varTypeTable + 2;
                        int[] typeTable = new int[k];
                        while (k > 0) {
                            typeTable[--k] = w + 6; // signature
                            typeTable[--k] = readUnsignedShort(w + 8); // index
                            typeTable[--k] = readUnsignedShort(w); // start
                            w += 10;
                        }
                    }
                    k = readUnsignedShort(varTable);
                    w = varTable + 2;
                    for (; k > 0; --k) {
                        int index = readUnsignedShort(w + 8);
                        mv.visitLocalVariable(readUTF8(w + 4, c), index);
                        w += 10;
                    }
                }
            }
            return u;
        }

        /**
         * Reads an unsigned short value in {@link #b b}. <i>This method is
         * intended for Attribute sub classes, and is normally not needed by
         * class generators or adapters.</i>
         *
         * @param index the start index of the value to be read in {@link #b b}.
         * @return the read value.
         */
        private int readUnsignedShort(final int index) {
            byte[] b = this.b;
            return ((b[index] & 0xFF) << 8) | (b[index + 1] & 0xFF);
        }

        /**
         * Reads a signed int value in {@link #b b}. <i>This method is intended for
         * Attribute sub classes, and is normally not needed by class
         * generators or adapters.</i>
         *
         * @param index the start index of the value to be read in {@link #b b}.
         * @return the read value.
         */
        private int readInt(final int index) {
            byte[] b = this.b;
            return ((b[index] & 0xFF) << 24) | ((b[index + 1] & 0xFF) << 16)
                    | ((b[index + 2] & 0xFF) << 8) | (b[index + 3] & 0xFF);
        }

        /**
         * Reads an UTF8 string constant pool item in {@link #b b}. <i>This method
         * is intended for Attribute sub classes, and is normally not needed
         * by class generators or adapters.</i>
         *
         * @param index the start index of an unsigned short value in {@link #b b},
         *              whose value is the index of an UTF8 constant pool item.
         * @param buf   buffer to be used to read the item. This buffer must be
         *              sufficiently large. It is not automatically resized.
         * @return the String corresponding to the specified UTF8 item.
         */
        private String readUTF8(int index, final char[] buf) {
            int item = readUnsignedShort(index);
            String s = strings[item];
            if (s != null) {
                return s;
            }
            index = items[item];
            return strings[item] = readUTF(index + 2, readUnsignedShort(index), buf);
        }

        /**
         * Reads UTF8 string in {@link #b b}.
         *
         * @param index  start offset of the UTF8 string to be read.
         * @param utfLen length of the UTF8 string to be read.
         * @param buf    buffer to be used to read the string. This buffer must be
         *               sufficiently large. It is not automatically resized.
         * @return the String corresponding to the specified UTF8 string.
         */
        private String readUTF(int index, final int utfLen, final char[] buf) {
            int endIndex = index + utfLen;
            byte[] b = this.b;
            int strLen = 0;
            int c, d, e;
            while (index < endIndex) {
                c = b[index++] & 0xFF;
                switch (c >> 4) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        // 0xxxxxxx
                        buf[strLen++] = (char) c;
                        break;
                    case 12:
                    case 13:
                        // 110x xxxx 10xx xxxx
                        d = b[index++];
                        buf[strLen++] = (char) (((c & 0x1F) << 6) | (d & 0x3F));
                        break;
                    default:
                        // 1110 xxxx 10xx xxxx 10xx xxxx
                        d = b[index++];
                        e = b[index++];
                        buf[strLen++] = (char) (((c & 0x0F) << 12)
                                | ((d & 0x3F) << 6) | (e & 0x3F));
                        break;
                }
            }
            return new String(buf, 0, strLen);
        }

    }

/***
 * Portions Copyright (c) 2007 Paul Hammant
 * Portions copyright (c) 2000-2007 INRIA, France Telecom
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

    /**
     * A Java type. This class can be used to make it easier to manipulate type and
     * method descriptors.
     *
     * @author Eric Bruneton
     * @author Chris Nokleberg
     */
    private static class Type {

        /**
         * The sort of the <tt>void</tt> type.
         */
        private final static int VOID = 0;

        /**
         * The sort of the <tt>boolean</tt> type.
         */
        private final static int BOOLEAN = 1;

        /**
         * The sort of the <tt>char</tt> type.
         */
        private final static int CHAR = 2;

        /**
         * The sort of the <tt>byte</tt> type.
         */
        private final static int BYTE = 3;

        /**
         * The sort of the <tt>short</tt> type.
         */
        private final static int SHORT = 4;

        /**
         * The sort of the <tt>int</tt> type.
         */
        private final static int INT = 5;

        /**
         * The sort of the <tt>float</tt> type.
         */
        private final static int FLOAT = 6;

        /**
         * The sort of the <tt>long</tt> type.
         */
        private final static int LONG = 7;

        /**
         * The sort of the <tt>double</tt> type.
         */
        private final static int DOUBLE = 8;

        /**
         * The sort of array reference types.
         */
        private final static int ARRAY = 9;

        /**
         * The sort of object reference type.
         */
        private final static int OBJECT = 10;

        /**
         * The <tt>void</tt> type.
         */
        private final static Type VOID_TYPE = new Type(VOID);

        /**
         * The <tt>boolean</tt> type.
         */
        private final static Type BOOLEAN_TYPE = new Type(BOOLEAN);

        /**
         * The <tt>char</tt> type.
         */
        private final static Type CHAR_TYPE = new Type(CHAR);

        /**
         * The <tt>byte</tt> type.
         */
        private final static Type BYTE_TYPE = new Type(BYTE);

        /**
         * The <tt>short</tt> type.
         */
        private final static Type SHORT_TYPE = new Type(SHORT);

        /**
         * The <tt>int</tt> type.
         */
        private final static Type INT_TYPE = new Type(INT);

        /**
         * The <tt>float</tt> type.
         */
        private final static Type FLOAT_TYPE = new Type(FLOAT);

        /**
         * The <tt>long</tt> type.
         */
        private final static Type LONG_TYPE = new Type(LONG);

        /**
         * The <tt>double</tt> type.
         */
        private final static Type DOUBLE_TYPE = new Type(DOUBLE);

        // ------------------------------------------------------------------------
        // Fields
        // ------------------------------------------------------------------------

        /**
         * The sort of this Java type.
         */
        private final int sort;

        /**
         * A buffer containing the internal name of this Java type. This field is
         * only used for reference types.
         */
        private char[] buf;

        /**
         * The offset of the internal name of this Java type in {@link #buf buf}.
         * This field is only used for reference types.
         */
        private int off;

        /**
         * The length of the internal name of this Java type. This field is only
         * used for reference types.
         */
        private int len;

        // ------------------------------------------------------------------------
        // Constructors
        // ------------------------------------------------------------------------

        /**
         * Constructs a primitive type.
         *
         * @param sort the sort of the primitive type to be constructed.
         */
        private Type(final int sort) {
            this.sort = sort;
            this.len = 1;
        }

        /**
         * Constructs a reference type.
         *
         * @param sort the sort of the reference type to be constructed.
         * @param buf  a buffer containing the descriptor of the previous type.
         * @param off  the offset of this descriptor in the previous buffer.
         * @param len  the length of this descriptor.
         */
        private Type(final int sort, final char[] buf, final int off, final int len) {
            this.sort = sort;
            this.buf = buf;
            this.off = off;
            this.len = len;
        }


        /**
         * Returns the Java types corresponding to the argument types of the given
         * method descriptor.
         *
         * @param methodDescriptor a method descriptor.
         * @return the Java types corresponding to the argument types of the given
         *         method descriptor.
         */
        private static Type[] getArgumentTypes(final String methodDescriptor) {
            char[] buf = methodDescriptor.toCharArray();
            int off = 1;
            int size = 0;
            while (true) {
                char car = buf[off++];
                if (car == ')') {
                    break;
                } else if (car == 'L') {
                    while (buf[off++] != ';') {
                    }
                    ++size;
                } else if (car != '[') {
                    ++size;
                }
            }

            Type[] args = new Type[size];
            off = 1;
            size = 0;
            while (buf[off] != ')') {
                args[size] = getType(buf, off);
                off += args[size].len + (args[size].sort == OBJECT ? 2 : 0);
                size += 1;
            }
            return args;
        }


        /**
         * Returns the Java type corresponding to the given type descriptor.
         *
         * @param buf a buffer containing a type descriptor.
         * @param off the offset of this descriptor in the previous buffer.
         * @return the Java type corresponding to the given type descriptor.
         */
        private static Type getType(final char[] buf, final int off) {
            int len;
            switch (buf[off]) {
                case 'V':
                    return VOID_TYPE;
                case 'Z':
                    return BOOLEAN_TYPE;
                case 'C':
                    return CHAR_TYPE;
                case 'B':
                    return BYTE_TYPE;
                case 'S':
                    return SHORT_TYPE;
                case 'I':
                    return INT_TYPE;
                case 'F':
                    return FLOAT_TYPE;
                case 'J':
                    return LONG_TYPE;
                case 'D':
                    return DOUBLE_TYPE;
                case '[':
                    len = 1;
                    while (buf[off + len] == '[') {
                        ++len;
                    }
                    if (buf[off + len] == 'L') {
                        ++len;
                        while (buf[off + len] != ';') {
                            ++len;
                        }
                    }
                    return new Type(ARRAY, buf, off, len + 1);
                    // case 'L':
                default:
                    len = 1;
                    while (buf[off + len] != ';') {
                        ++len;
                    }
                    return new Type(OBJECT, buf, off + 1, len - 1);
            }
        }

        // ------------------------------------------------------------------------
        // Accessors
        // ------------------------------------------------------------------------

        /**
         * Returns the number of dimensions of this array type. This method should
         * only be used for an array type.
         *
         * @return the number of dimensions of this array type.
         */
        private int getDimensions() {
            int i = 1;
            while (buf[off + i] == '[') {
                ++i;
            }
            return i;
        }

        /**
         * Returns the type of the elements of this array type. This method should
         * only be used for an array type.
         *
         * @return Returns the type of the elements of this array type.
         */
        private Type getElementType() {
            return getType(buf, off + getDimensions());
        }

        /**
         * Returns the name of the class corresponding to this type.
         *
         * @return the fully qualified name of the class corresponding to this type.
         */
        private String getClassName() {
            switch (sort) {
                case VOID:
                    return "void";
                case BOOLEAN:
                    return "boolean";
                case CHAR:
                    return "char";
                case BYTE:
                    return "byte";
                case SHORT:
                    return "short";
                case INT:
                    return "int";
                case FLOAT:
                    return "float";
                case LONG:
                    return "long";
                case DOUBLE:
                    return "double";
                case ARRAY:
                    StringBuffer b = new StringBuffer(getElementType().getClassName());
                    for (int i = getDimensions(); i > 0; --i) {
                        b.append("[]");
                    }
                    return b.toString();
                    // case OBJECT:
                default:
                    return new String(buf, off, len).replace('/', '.');
            }
        }
    }


}
