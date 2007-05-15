package com.thoughtworks.paranamer;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * Default implementation of Paranamer
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 * @author Guilherme Silveira
 */
public class DefaultParanamer implements Paranamer {
    
    private static final String[] EMPTY_NAMES = new String[]{};
    private static final String COMMA = ",";
    private static final String DOT = ".";
    private static final String SPACE = " ";

    private static final String __PARANAMER_DATA = "v1.0 \n"
        + "DefaultParanamer \n"
        + "DefaultParanamer java.lang.String paranamerResource \n"
        + "toString \n"
        + "lookupParameterNames java.lang.Constructor constructor \n"
        + "lookupParameterNames java.lang.Method method \n";

    public DefaultParanamer() {
    }

    public String[] lookupParameterNames(Method method) {
        if (method.getParameterTypes().length == 0) { 
            // no arguments ... return empty names
            return EMPTY_NAMES;
        }
        Class declaringClass = method.getDeclaringClass();
        String methodName = method.getName();
        String parameterTypeNames = parameterNamesCSV(method.getParameterTypes());
        String prefix = join(new String[]{methodName}, SPACE, false);
        String[] names = getNames(declaringClass, parameterTypeNames, prefix);
        if ( names == null ){
            throw new ParameterNamesNotFoundException("No parameter names found for class '"+declaringClass+"', method "+methodName
                                        +" and parameter types "+parameterTypeNames);
        }
        return names;
    }
    
    public String[] lookupParameterNames(Constructor constructor) {
        if (constructor.getParameterTypes().length == 0) { 
            // no arguments ... return empty names
            return EMPTY_NAMES;
        }
        Class declaringClass = constructor.getDeclaringClass();
        String methodName = methodName(constructor);
        String parameterTypeNames = parameterNamesCSV(constructor.getParameterTypes());
        String prefix = join(new String[]{methodName}, SPACE, false);
        String[] names = getNames(declaringClass, parameterTypeNames, prefix);
        if ( names == null ){
            throw new ParameterNamesNotFoundException("No parameter names found for class '"+declaringClass+"', constructor "+methodName
                    +" and parameter types "+parameterTypeNames);
        }
        return names;
    }

    private String methodName(Constructor constructor) {
        return constructor.getName().substring(constructor.getName().lastIndexOf(DOT)+1);
    }

    private String[] getNames(Class declaringClass, String parameterTypes, String prefix) {
        ClassLoader loader = declaringClass.getClassLoader();
        String data = getParameterListResource(declaringClass, loader);
        List lines = filterLinesByPrefix(data, prefix);
        for (int i = 0; i < lines.size(); i++) {
            String line = (String) lines.get(i);
            String[] parts = line.split(SPACE);
            // assumes line structure: methodName parameterTypes parameterNames
            if ( parts.length == 3 && parts[1].equals(parameterTypes) ){
              String parameterNames = parts[2];
              return parameterNames.split(COMMA);                
            }
        }
        return null;
    }

    public int areParameterNamesAvailable(ClassLoader classLoader, Class clazz, String constructorOrMethodName) {
        String data = getParameterListResource(clazz, classLoader);
        
        if (data == null) {
            return NO_PARAMETER_NAMES_LIST;
        }

        String prefix = join(new String[]{constructorOrMethodName}, SPACE, true);
        List lines = filterLinesByPrefix(data, prefix);
        if (lines.size() == 0) {
            return NO_PARAMETER_NAMES_FOR_CLASS_AND_MEMBER;
        }

        return PARAMETER_NAMES_FOUND;
    }

    private String join(String[] parts, String separator, boolean trailingSeparator){
        StringBuffer sb = new StringBuffer();
        for ( int i = 0; i < parts.length; i++ ){
            sb.append(parts[i]);
            if ( i < parts.length - 1 || trailingSeparator ){
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    private String parameterNamesCSV(Class[] parameterTypes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < parameterTypes.length; i++) {
            sb.append(parameterTypes[i].getName());
            if (i < parameterTypes.length - 1) {
                sb.append(COMMA);
            }
        }
        return sb.toString();
    }

    private String getParameterListResource(Class declaringClass, ClassLoader classLoader) {
        try {
            Field field = declaringClass.getDeclaredField("__PARANAMER_DATA");
            // TODO create acc test which finds field?
            // TODO create acc test which does not find field?
            // TODO create acc test what to do with private? access anyway?
            // TODO create acc test with non static field?
            // TODO create acc test with another type of field?
            if(!Modifier.isStatic(field.getModifiers()) || !field.getType().equals(String.class)) {
                return null;
            }
            String s = (String) field.get(null);
            return s;
        } catch (NoSuchFieldException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Filter the mappings and only return lines matching the prefix passed in.
     * @param data the data encoding the mappings
     * @param prefix the String prefix
     * @return A list of lines that match the prefix
     */
    private List filterLinesByPrefix(String data, String prefix) {
        List lines = new ArrayList();
        if (data == null) {
            return Collections.EMPTY_LIST;
        }
        StringReader sr = new StringReader(data);

        try {
            LineNumberReader lineReader = new LineNumberReader(sr);
            String line = readLine(lineReader);

            while (line != null) {
                if (line.startsWith(prefix)) {
                    lines.add(line.trim());
                }
                line = readLine(lineReader);
            }
            return lines;
        } finally {
            if (sr != null) {
                sr.close();
            }
        }
    }

    private String readLine(LineNumberReader lineReader) {
        try {
            return lineReader.readLine();
        } catch (IOException e) {
            return null; 
        }
    }

    public String toString() {
        return new StringBuffer()
        .append("[DefaultParanamer paranamerResource=")
        .append("]").toString();
    }

}
