package com.thoughtworks.paranamer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DefaultParanamer implements Paranamer {
    private static final String EMPTY = "";
    private static final String COMMA = ",";
    private static final String NEWLINE = "\n";
    private static final String SPACE = " ";

    private static final String DEFAULT_PARANAMER_RESOURCE = "META-INF/ParameterNames.txt";
    private String paranamerResource;
    

    public DefaultParanamer() {
        this(DEFAULT_PARANAMER_RESOURCE);
    }

    public DefaultParanamer(String paranamerResource) {
        this.paranamerResource = paranamerResource;
    }

    /**
     * {@inheritDoc}
     * 
     * @previousParamNames classLoader,c,m,p
     */
    public Method lookupMethod(ClassLoader classLoader, String className, String methodName, String paramNames) {
        String mappings = getMappingsFromResource(getParanamerResource(classLoader));
        StringBuffer classAndMethodAndParamNamesSB = new StringBuffer(NEWLINE).append(className).append(SPACE).append(methodName);
        if (!paramNames.equals(EMPTY)) {
            classAndMethodAndParamNamesSB.append(SPACE).append(paramNames);
        }
        String classAndMethodAndParamNames = classAndMethodAndParamNamesSB.toString();

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

    public String[] lookupParameterNames(ClassLoader classLoader, String className, String methodName) {
        String mappings = getMappingsFromResource(getParanamerResource(classLoader));
        if (mappings == null) {
            return new String[0];
        }
        String classAndMethodName = NEWLINE + className + SPACE + methodName + SPACE;
        int ix = mappings.indexOf(classAndMethodName);
        List matches = new ArrayList();
        while (ix > 0) {
            int start = ix + classAndMethodName.length();
            int end = mappings.indexOf(SPACE, ix + classAndMethodName.length() + 1);
            String expected = mappings.substring(start, end);
            if (didNotReadOffEndOfLine(expected)) {
                matches.add(expected.trim());
            }
            ix = mappings.indexOf(classAndMethodName, ix + 1);
        }
        return (String[]) matches.toArray(new String[matches.size()]);
    }

    private boolean didNotReadOffEndOfLine(String expected) {
        return !expected.contains(NEWLINE);
    }

    public Constructor lookupConstructor(ClassLoader classLoader, String className, String paramNames) {
        String mappings = getMappingsFromResource(classLoader.getResourceAsStream(DEFAULT_PARANAMER_RESOURCE));
        String classAndConstructorAndParamNames = NEWLINE + className + SPACE + className.substring(className.lastIndexOf(".") + 1) + SPACE + paramNames + SPACE;
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
        int end = mappings.indexOf(NEWLINE, start + 1);
        String methodParamTypes = mappings.substring(start, end).trim();
        return methodParamTypes;
    }

    private String turnClassArrayIntoRepresentativeString(Class[] parameters) {
        String paramTypes = EMPTY;
        for (int k = 0; k < parameters.length; k++) {
            paramTypes = paramTypes + parameters[k].getName();
            paramTypes = paramTypes + ((k + 1 < parameters.length) ? COMMA : EMPTY);
        }
        return paramTypes;
    }
    

    private String getMappingsFromResource(InputStream resourceAsStream) {
        StringBuffer paramMappingsBuffer = new StringBuffer();
        try {
            if (resourceAsStream == null) {
                return EMPTY;
            }
            InputStreamReader inputStreamReader = new InputStreamReader(resourceAsStream);
            LineNumberReader lineReader = new LineNumberReader(inputStreamReader);
            String line = readLine(lineReader);
            while (line != null) {
                paramMappingsBuffer.append(line).append(NEWLINE);
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

    private InputStream getParanamerResource(ClassLoader classLoader) {
        return classLoader.getResourceAsStream(paranamerResource);
    }

}
