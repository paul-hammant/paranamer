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

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaConstructor;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

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

    public QdoxParanamerGenerator() {
    }

    public void processSourcePath(String sourcePath, String outputPath) throws IOException {
    	List<JavaClass> classes = getClassesSortedByName(sourcePath);
        processClasses(classes, outputPath);
    }

    private List<JavaClass> getClassesSortedByName(String sourcePath) {
    	JavaProjectBuilder builder = new JavaProjectBuilder();
        builder.addSourceTree(new File(sourcePath));
        List<JavaClass> classes = new ArrayList<JavaClass>(builder.getClasses());
        Collections.sort(classes, new Comparator<JavaClass>() {
        	public int compare(JavaClass o1, JavaClass o2) 
        	{
        		return o1.getName().compareTo(o2.getName());
        	}
		} );
        return classes;
    }

    public void processClasses(Collection<JavaClass> classes, String outputPath) throws IOException {
        for (JavaClass javaClass : classes) {
        	StringBuilder content = new StringBuilder();
        	content.append(addConstructors(javaClass.getConstructors()));
        	content.append(addMethods(javaClass.getMethods()));
            // TODO problem with inner classes
            makeEnhancer().enhance(new File(outputPath, javaClass.getFullyQualifiedName().replace('.', File.separatorChar) + ".class"), content);

        }
    }

    public Enhancer makeEnhancer() {
        return new Enhancer();
    }

    private String addConstructors(List<JavaConstructor> methods) {
        StringBuilder buffer = new StringBuilder();
        for (JavaConstructor javaConstructor : methods) {
            if (!javaConstructor.isPrivate() && javaConstructor.getParameters().size() > 0) {
                buffer.append(addConstructor(javaConstructor));
            }
        }
        return buffer.toString();
    }
    
    private String addMethods(List<JavaMethod> methods) {
    	Collections.sort(methods, new Comparator<JavaMethod>() {
    		public int compare(JavaMethod o1, JavaMethod o2) {
    			return o1.getName().compareTo(o2.getName());
    		}
		});
    	StringBuilder buffer = new StringBuilder();
        for (JavaMethod javaMethod : methods) {
            if (!javaMethod.isPrivate() && javaMethod.getParameters().size() > 0) {
                buffer.append(addMethod(javaMethod));
            }
        }
        return buffer.toString();
    }

    private CharSequence addConstructor(JavaConstructor constructor) {
        List<JavaParameter> parameters = constructor.getParameters();
        return format(constructor.getName(), parameters, true);
    }

    private CharSequence addMethod(JavaMethod method) {
        List<JavaParameter> parameters = method.getParameters();
        return format(method.getName(), parameters, false);
    }

    private CharSequence format(String name, List<JavaParameter> parameters, boolean isConstructor) {
    	StringBuilder sb = new StringBuilder();
        String methodName = name;
        if (isConstructor) {
            methodName = "<init>";
        }
        String parameterTypes = getParameterTypes(parameters);
        sb.append(formatLine(methodName, parameterTypes, getParameterNames(parameters)));
        return sb;
    }

    private CharSequence formatLine(String methodName, String paramTypes, String paramNames){
    	StringBuilder sb = new StringBuilder();
        // processClasses line structure:  methodName paramTypes paramNames
        sb.append(methodName).append(SPACE);
        if ( paramTypes.length() > 0 ) {
            sb.append(paramTypes.trim()).append(SPACE);
            sb.append(paramNames.trim()).append(SPACE);
        }
        sb.append(NEWLINE);
        return sb;
    }

    private String getParameterNames(List<JavaParameter> parameters) {
    	StringBuilder sb = new StringBuilder();
        Iterator<JavaParameter> paramIter = parameters.iterator();
        while(paramIter.hasNext()) {
        	JavaParameter param = paramIter.next();
        	sb.append(param.getName());
        	if(paramIter.hasNext())
        	{
        		sb.append(COMMA);
        	}
        }
        return sb.toString();
    }

    private String getParameterTypes(List<JavaParameter> parameters) {
        StringBuilder sb = new StringBuilder();
        Iterator<JavaParameter> paramIter = parameters.iterator();
        while(paramIter.hasNext()) {
        	JavaParameter param = paramIter.next();
        	sb.append(param.getType().getCanonicalName());
        	if(paramIter.hasNext())
        	{
        		sb.append(COMMA);
        	}
        }
        return sb.toString();
    }
}
