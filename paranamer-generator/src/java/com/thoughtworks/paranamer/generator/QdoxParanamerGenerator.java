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

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.Type;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Qdox-based implementation of ParanamerGenerator which parses Java source files to processSourcePath
 * parameter names lists.
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 * @author Guilherme Silveira
 * @author Victor Williams Stafusa da Silva
 */
public class QdoxParanamerGenerator implements ParanamerGenerator {

    private static final String SPACE  = " ";
    private static final String NEWLINE = "\n";
    private static final String COMMA = ",";
    private static final String EMPTY = "";
    private static final String BRACKETS = "[]";

    public QdoxParanamerGenerator() {
    }

    public void processSourcePath(String sourcePath, String outputPath) throws IOException {
        JavaClass[] classes = getClassesSortedByName(sourcePath);
        processClasses(classes, outputPath);
    }

    private JavaClass[] getClassesSortedByName(String sourcePath) {
        JavaDocBuilder builder = new JavaDocBuilder();
        builder.addSourceTree(new File(sourcePath));
        JavaClass[] classes = builder.getClasses();
        Arrays.sort(classes);
        return classes;
    }

    public void processClasses(JavaClass[] classes, String outputPath) throws IOException {
        for (JavaClass javaClass : classes) {
            String content = addMethods(javaClass.getMethods());
            File f = new File(outputPath, javaClass.getFullyQualifiedName().replace('.', File.separatorChar) + ".class");
            makeEnhancer().enhance(f, content);
        }
    }

    public Enhancer makeEnhancer() {
        return new Enhancer();
    }

    private String addMethods(JavaMethod[] methods) {
        Arrays.sort(methods);
        StringBuilder buffer = new StringBuilder();
        for (JavaMethod javaMethod : methods) {
            if (!Arrays.asList(javaMethod.getModifiers()).contains("private") && javaMethod.getParameters().length > 0) {
                addMethod(buffer, javaMethod);
            }
        }
        return buffer.toString();
    }

    private void addMethod(StringBuilder sb, JavaMethod method) {
        JavaParameter[] parameters = method.getParameters();
        formatMethod(sb, method, parameters);
    }

    private void formatMethod(StringBuilder sb, JavaMethod method, JavaParameter[] parameters) {
        String methodName = method.getName();
        if (method.isConstructor()) {
            methodName = "<init>";
        }

        // processClasses line structure:  methodName paramTypes paramNames
        sb.append(methodName).append(SPACE);
        if (parameters.length > 0) {
            formatParameterTypes(sb, parameters);
            sb.append(SPACE);
            formatParameterNames(sb, parameters);
            sb.append(SPACE);
        }
        sb.append(NEWLINE);
    }

    private void formatParameterNames(StringBuilder sb, JavaParameter[] parameters) {
        for (int i = 0; i < parameters.length; i++) {
            sb.append(parameters[i].getName());
            sb.append(comma(i, parameters.length));
        }
    }

    private void formatParameterTypes(StringBuilder sb, JavaParameter[] parameters) {
        for (int i = 0; i < parameters.length; i++) {

            // This code is a bit dodgy to ensure that both inner classes and arrays shows up correctly.
            // It is based in the Type.toString() method, but using getFullyQualifiedName() instead of getValue().
            Type t = parameters[i].getType();
            sb.append(t.getFullyQualifiedName());
            int dimensions = t.getDimensions();
            for (int d = 0; d < dimensions; d++) {
                sb.append(BRACKETS);
            }

            sb.append(comma(i, parameters.length));
        }
    }

    private String comma(int index, int size) {
        return (index + 1 < size) ? COMMA : EMPTY;
    }

}
