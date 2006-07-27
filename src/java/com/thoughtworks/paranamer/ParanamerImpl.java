package com.thoughtworks.paranamer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ParanamerImpl implements Paranamer {

    /**
     * Lookup a method, and return null if its not there
     * <p/>
     * Copy the body of the method to wherever you want to - it means you won't have to rely on
     * one more jar in your app.
     *
     * @previousParamNames classLoader,c,m,p
     */
    public Method lookupMethod(ClassLoader classLoader, String className, String methodName, String paramNames) {
        String mappings = getMappingsFromResource(classLoader.getResourceAsStream("META-INF/ParameterNames.txt"));
        String classAndMethodAndParamNames = "\n" + className + " " + methodName + " " + paramNames + " ";
        int ix = mappings.indexOf(classAndMethodAndParamNames);
        if (ix != -1) {
            String methodParamTypes = extractParamerTypesFromFoundMethod(ix, classAndMethodAndParamNames, mappings);
            Class loadedClazz;
            try {
                loadedClazz = classLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                return null; // or could throw a/the exception
            }
            Method methods[] = loadedClazz.getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                Class[] parameters = method.getParameterTypes();
                if (method.getName().equals(methodName)) {
                    String paramTypes = turnClassArrayIntoRepresentativeString(parameters);
                    if (paramTypes.equals(methodParamTypes)) {
                        return method;
                    }
                }
            }
        }
        return null;
    }

    private String getMappingsFromResource(InputStream resourceAsStream) {
        StringBuffer paramMappingsBuffer = new StringBuffer();
        try {
            if (resourceAsStream == null) {
                return "";
            }
            InputStreamReader inputStreamReader = new InputStreamReader(resourceAsStream);
            LineNumberReader lineReader = new LineNumberReader(inputStreamReader);
            String line = readLine(lineReader);
            while (line != null) {
                paramMappingsBuffer.append(line).append("\n");
                line = readLine(lineReader);
            }
            return paramMappingsBuffer.toString();
        } finally {
            try {
                if (resourceAsStream != null) {
                    resourceAsStream.close();
                }
            } catch (IOException e) {
            }
        }
    }

    private String readLine(LineNumberReader lineReader) {
        try {
            return lineReader.readLine();
        } catch (IOException e) {
            return null; // or throw an exception if you prefer
        }
    }

    public String[] lookupParameterNames(ClassLoader classLoader, String className, String methodName) {
        String mappings = getMappingsFromResource(classLoader.getResourceAsStream("META-INF/ParameterNames.txt"));
        if (mappings == null) {
            return new String[0];
        }
        String classAndMethodName = "\n" + className + " " + methodName + " ";
        int ix = mappings.indexOf(classAndMethodName);
        List matches = new ArrayList();
        while (ix > 0) {
            matches.add(mappings.substring(ix + classAndMethodName.length(), mappings.indexOf(" ", ix + classAndMethodName.length() + 1)).trim());
            ix = mappings.indexOf(classAndMethodName, ix + 1);
        }
        return (String[]) matches.toArray(new String[matches.size()]);
    }

    public Constructor lookupConstructor(ClassLoader classLoader, String className, String paramNames) {
        String mappings = getMappingsFromResource(classLoader.getResourceAsStream("META-INF/ParameterNames.txt"));
        String classAndConstructorAndParamNames = "\n" + className + " " + className.substring(className.lastIndexOf(".") + 1) + " " + paramNames + " ";
        int ix = mappings.indexOf(classAndConstructorAndParamNames);
        if (ix != -1) {
            String methodParamTypes = extractParamerTypesFromFoundMethod(ix, classAndConstructorAndParamNames, mappings);
            Class loadedClazz;
            try {
                loadedClazz = classLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                return null; // or could throw a/the exception
            }
            Constructor constructors[] = loadedClazz.getConstructors();
            for (int i = 0; i < constructors.length; i++) {
                Constructor constructor = constructors[i];
                Class[] parameters = constructor.getParameterTypes();
                String paramTypes = turnClassArrayIntoRepresentativeString(parameters);
                if (paramTypes.equals(methodParamTypes)) {
                    return constructor;
                }
            }
        }
        return null;
    }

    private String extractParamerTypesFromFoundMethod(int ix, String classAndConstructorAndParamNames, String mappings) {
        int start = ix + classAndConstructorAndParamNames.length();
        int end = mappings.indexOf("\n", start + 1);
        String methodParamTypes = mappings.substring(start, end).trim();
        return methodParamTypes;
    }

    private String turnClassArrayIntoRepresentativeString(Class[] parameters) {
        String paramTypes = "";
        for (int k = 0; k < parameters.length; k++) {
            paramTypes = paramTypes + parameters[k].getName();
            paramTypes = paramTypes + ((k + 1 < parameters.length) ? "," : "");
        }
        return paramTypes;
    }
}
