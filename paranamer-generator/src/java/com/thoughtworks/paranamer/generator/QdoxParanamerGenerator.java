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
 * Qdox-based implementation of ParanamerGenerator which parses Java source files to generate
 * parameter names lists.
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 * @author Guilherme Silveira
 */
public class QdoxParanamerGenerator implements ParanamerGenerator {
    
    private static final String DOT = ".";
    private static final String SPACE  = " ";
    private static final String NEWLINE = "\n";
    private static final String COMMA = ",";
    private static final String EMPTY = "";
    private final String paranamerResource;
    private final Map types = new HashMap();

    public QdoxParanamerGenerator() {
        this(ParanamerConstants.DEFAULT_PARANAMER_RESOURCE);
    }

    public QdoxParanamerGenerator(String paranamerResource) {
        this.paranamerResource = paranamerResource;
    }

    public String generate(String sourcePath) {
        JavaClass[] classes = getClassesSortedByName(sourcePath);
        return format(classes);
    }

    private JavaClass[] getClassesSortedByName(String sourcePath) {
        JavaDocBuilder builder = new JavaDocBuilder();
        builder.addSourceTree(new File(sourcePath));
        JavaClass[] classes = builder.getClasses();
        Arrays.sort(classes);
        return classes;
    }

    private String format(JavaClass[] classes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < classes.length; i++) {
            JavaClass javaClass = classes[i];
            if (!javaClass.isInterface()) {
                String className = javaClass.getPackage() + DOT + javaClass.getName();
                String content = addMethods(javaClass.getMethods(), className);
                this.types.put(javaClass.getFullyQualifiedName(), content);
                sb.append(content);
            }
        }
        return sb.toString();
    }

    private String addMethods(JavaMethod[] methods, String className) {
        Arrays.sort(methods);
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < methods.length; i++) {
            JavaMethod javaMethod = methods[i];
            if (Arrays.asList(javaMethod.getModifiers()).contains("public")) {
                buffer.append(addPublicMethod(javaMethod, className));
            }
        }
        return buffer.toString();
    }

    private String addPublicMethod(JavaMethod method, String className) {
        JavaParameter[] parameters = method.getParameters();
        DocletTag[] alsoKnownAs = method.getTagsByName("previousParamNames");
        return format(method, className, parameters, alsoKnownAs);
    }

    private String format(JavaMethod method, String className, JavaParameter[] parameters, DocletTag[] alsoKnownAs) {
        StringBuffer sb = new StringBuffer();
        String methodName = method.getName();
        String parameterTypes = getParameterTypes(parameters);
        sb.append(formatLine(className, methodName, parameterTypes, getParameterNames(parameters)));
        for (int i = 0; i < alsoKnownAs.length; i++) {
            sb.append(formatLine(className, methodName, parameterTypes, alsoKnownAs[i].getValue()));
        }
        return sb.toString();
    }
    
    private String formatLine(String className, String methodName, String paramTypes, String paramNames){
        StringBuffer sb = new StringBuffer();
        // format line structure:  className methodName paramTypes paramNames        
        sb.append(className).append(SPACE);
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

    public void write(String outputPath, String content) throws IOException {
        //System.err.println("--> " + content + " <--");
        String path = outputPath + File.separator + paranamerResource;
        ensureParentDirectoriesExist(path);
        PrintWriter pw = new PrintWriter(new FileWriter(path));
        pw.println(ParanamerConstants.HEADER);
        pw.println(content);
        pw.close();
        Enhancer enhancer = new Enhancer();
        for(Iterator it = types.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String fullyQualifiedName = (String) entry.getKey();
            // TODO problem with inner classes
            File file = new File(outputPath, fullyQualifiedName.replace('.',File.separatorChar));
            enhancer.enhance(file, (String) entry.getValue());
        }
    }

    private void ensureParentDirectoriesExist(String path) {
        File file = new File(path);
        if ( file.getParentFile() != null ){
            file.getParentFile().mkdirs();
        }
    }

    private String comma(int index, int size) {
        return (index + 1 < size) ? COMMA : EMPTY;
    }

}
