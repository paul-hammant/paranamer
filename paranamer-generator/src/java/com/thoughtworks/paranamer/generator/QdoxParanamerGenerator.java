package com.thoughtworks.paranamer.generator;

import com.thoughtworks.paranamer.ParanamerConstants;
import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
            if (!javaClass.isInterface() && javaClass.getFieldByName("__PARANAMER_DATA") != null) {
                String content = addMethods(javaClass.getMethods());
                Enhancer enhancer = new Enhancer();
                // TODO problem with inner classes
                File classFile = new File(outputPath, javaClass.getFullyQualifiedName().replace('.',File.separatorChar) + ".class");
                enhancer.enhance(classFile, content);

            }
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
        DocletTag[] alsoKnownAs = method.getTagsByName("previousParamNames");
        return format(method, parameters, alsoKnownAs);
    }

    private String format(JavaMethod method, JavaParameter[] parameters, DocletTag[] alsoKnownAs) {
        StringBuffer sb = new StringBuffer();
        String methodName = method.getName();
        if (method.isConstructor()) {
            methodName = "<init>";
        }
        String parameterTypes = getParameterTypes(parameters);
        sb.append(formatLine(methodName, parameterTypes, getParameterNames(parameters)));
        for (int i = 0; i < alsoKnownAs.length; i++) {
            sb.append(formatLine(methodName, parameterTypes, alsoKnownAs[i].getValue()));
        }
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
