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
import java.util.Arrays;

/**
 * Qdox-based implementation of ParanamerGenerator which
 * parses Java source files.
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public class QdoxParanamerGenerator implements ParanamerGenerator {
    private static final String SPACE  = " ";
    private static final String NEWLINE = "\n";
    private static final String COMMA = ",";
    private static final String EMPTY = "";
    private String paranamerResource;

    public QdoxParanamerGenerator() {
        this(ParanamerConstants.DEFAULT_PARANAMER_RESOURCE);
    }

    public QdoxParanamerGenerator(String paranamerResource) {
        this.paranamerResource = paranamerResource;
    }

    public String generate(String sourcePath) {
        StringBuffer buffer = new StringBuffer();
        JavaDocBuilder builder = new JavaDocBuilder();
        builder.addSourceTree(new File(sourcePath));
        JavaClass[] classes = builder.getClasses();
        Arrays.sort(classes);
        for (int i = 0; i < classes.length; i++) {
            JavaClass javaClass = classes[i];
            if (!javaClass.isInterface()) {
                buffer.append(addMethods(javaClass.getMethods(), javaClass.getPackage() + "." + javaClass.getName()));
            }
        }
        return buffer.toString();
    }

    private String addMethods(JavaMethod[] methods, String className) {
        StringBuffer buffer = new StringBuffer();
        Arrays.sort(methods);
        for (int j = 0; j < methods.length; j++) {
            JavaMethod javaMethod = methods[j];
            if (Arrays.asList(javaMethod.getModifiers()).contains("public")) {
                buffer.append(addPublicMethod(javaMethod, className));
            }
        }
        return buffer.toString();
    }

    private String addPublicMethod(JavaMethod method, String className) {
        StringBuffer buffer = new StringBuffer();
        JavaParameter[] parms = method.getParameters();
        DocletTag[] alsoKnownAs = method.getTagsByName("previousParamNames");
        for (int k = 0; k < alsoKnownAs.length; k++) {
            String value = alsoKnownAs[k].getValue();
            buffer.append(className);
            buffer.append(SPACE);
            buffer.append((method.getName() + SPACE + value + SPACE + getTypes(parms)).trim());
            buffer.append(NEWLINE);
        }
        String paramNames = getParamNames(parms);
        String types = getTypes(parms);
        buffer.append(className);
        buffer.append(SPACE);
        buffer.append(method.getName());
        if (!paramNames.equals(EMPTY)) {
            buffer.append(SPACE);
        }
        buffer.append(paramNames);
        if (!types.equals(EMPTY)) {
            buffer.append(SPACE);
        }
        buffer.append(types);
        buffer.append(SPACE);
        buffer.append(NEWLINE);
        return buffer.toString();
    }

    private String getParamNames(JavaParameter[] parms) {
        StringBuffer buffer = new StringBuffer();
        for (int k = 0; k < parms.length; k++) {
            buffer.append(parms[k].getName());
            buffer.append(comma(k, parms.length));
        }
        return buffer.toString();
    }

    private String getTypes(JavaParameter[] parms) {
        StringBuffer buffer = new StringBuffer();
        for (int k = 0; k < parms.length; k++) {
            buffer.append(parms[k].getType());
            buffer.append(comma(k, parms.length));
        }
        return buffer.toString();
    }

    public void write(String outputPath, String content) throws IOException {
        String path = outputPath + File.separator + paranamerResource;
        ensureParentDirectoriesExist(path);
        FileWriter fileWriter = new FileWriter(path);
        PrintWriter pw = new PrintWriter(fileWriter);
        pw.println(ParanamerConstants.HEADER);
        pw.println(content);
        pw.close();
    }

    private void ensureParentDirectoriesExist(String path) {
        File file = new File(path);
        if ( file.getParentFile() != null ){
            file.getParentFile().mkdirs();
        }
    }

    private String comma(int k, int size) {
        return (k + 1 < size) ? COMMA : EMPTY;
    }

}
