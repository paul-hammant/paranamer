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

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Qdox-based implementation of ParanamerGenerator which parses Java source files to processSourcePath
 * parameter names lists.
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 * @author Guilherme Silveira
 */
public class QdoxParanamerGenerator implements ParanamerGenerator {

    private static final String SPACE  = " ";
    private static final String NEWLINE = "\n";
    private static final String COMMA = ",";
    private static final String EMPTY = "";

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

    private void processClasses(JavaClass[] classes, String outputPath) throws IOException {
        for (int i = 0; i < classes.length; i++) {
            JavaClass javaClass = classes[i];
            String content = addMethods(javaClass.getMethods());
            Enhancer enhancer = new Enhancer();
            // TODO problem with inner classes
            File classFile = new File(outputPath, javaClass.getFullyQualifiedName().replace('.',File.separatorChar) + ".class");
            enhancer.enhance(classFile, content);
        }
    }

    private String addMethods(JavaMethod[] methods) {
        Arrays.sort(methods);
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < methods.length; i++) {
            JavaMethod javaMethod = methods[i];
            if (Arrays.asList(javaMethod.getModifiers()).contains("public")) {
                buffer.append(addPublicMethod(javaMethod));
            }
        }
        return buffer.toString();
    }

    private String addPublicMethod(JavaMethod method) {
        JavaParameter[] parameters = method.getParameters();
        return format(method, parameters);
    }

    private String format(JavaMethod method, JavaParameter[] parameters) {
        StringBuffer sb = new StringBuffer();
        String methodName = method.getName();
        if (method.isConstructor()) {
            methodName = "<init>";
        }
        String parameterTypes = getParameterTypes(parameters);
        sb.append(formatLine(methodName, parameterTypes, getParameterNames(parameters)));
        return sb.toString();
    }

    private String formatLine(String methodName, String paramTypes, String paramNames){
        StringBuffer sb = new StringBuffer();
        // processClasses line structure:  methodName paramTypes paramNames
        sb.append(methodName).append(SPACE);
        if ( paramTypes.length() > 0 ) {
            sb.append(paramTypes.trim()).append(SPACE);
            sb.append(paramNames.trim()).append(SPACE);
        }
        sb.append(NEWLINE);
        return sb.toString();
    }

    private String getParameterNames(JavaParameter[] parameters) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < parameters.length; i++) {
            sb.append(parameters[i].getName());
            sb.append(comma(i, parameters.length));
        }
        return sb.toString();
    }

    private String getParameterTypes(JavaParameter[] parameters) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < parameters.length; i++) {
            sb.append(parameters[i].getType());
            sb.append(comma(i, parameters.length));
        }
        return sb.toString();
    }

    private String comma(int index, int size) {
        return (index + 1 < size) ? COMMA : EMPTY;
    }

}
