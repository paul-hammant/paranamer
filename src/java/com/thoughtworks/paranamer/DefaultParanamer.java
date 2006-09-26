package com.thoughtworks.paranamer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class DefaultParanamer implements Paranamer {
    private static final String EMPTY = "";
    private static final String COMMA = ",";
    private static final String NEWLINE = "\n";
    private static final String SPACE = " ";

    private String paranamerResource;

    public DefaultParanamer() {
        this(ParanamerConstants.DEFAULT_PARANAMER_RESOURCE);
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
        String mappings = readMappings(getResource(classLoader));
        String classAndMethodAndParameterNames = getClassAndMethodAndParameterNames(className, methodName, paramNames);

        int index = mappings.indexOf(classAndMethodAndParameterNames);
        if (index != -1) {
            String methodParamTypes = extractParameterTypesFromFoundMethod(index, classAndMethodAndParameterNames, mappings);
            Class loadedClass;
            try {
                loadedClass = classLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                return null; // or could throw a/the exception
            }
            Method methods[] = loadedClass.getMethods();
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

    private String getClassAndMethodAndParameterNames(String className, String methodName, String paramNames) {
        StringBuffer buffer = new StringBuffer(NEWLINE).append(className).append(SPACE).append(methodName);
        if (!paramNames.equals(EMPTY)) {
            buffer.append(SPACE).append(paramNames);
        }
        return buffer.toString();
    }

    public String[] lookupParameterNames(ClassLoader classLoader, String className, String methodName) {
        String mappings = readMappings(getResource(classLoader));
        String classAndMethodName = NEWLINE + className + SPACE + methodName + SPACE;
        int index = mappings.indexOf(classAndMethodName);
        List matches = new ArrayList();
        while (index > 0) {
            int start = index + classAndMethodName.length();
            int end = mappings.indexOf(SPACE, index + classAndMethodName.length() + 1);
            String expected = mappings.substring(start, end);
            if (didNotReadOffEndOfLine(expected)) {
                matches.add(expected.trim());
            }
            index = mappings.indexOf(classAndMethodName, index + 1);
        }
        return (String[]) matches.toArray(new String[matches.size()]);
    }

    public String lookupParameterNamesForMethod(Method method) {
        if(method.getParameterTypes().length == 0) { // no arguments ... return empty string
            return "";
        }
        
        Class declaringClass = method.getDeclaringClass();
        String parameterTypes = stringifyMethodParameterTypes(method);
        String prefix = declaringClass.getName() + SPACE + method.getName();

        List results = filterMappingByPrefix(prefix, getResource(declaringClass.getClassLoader()));

        for (int i = 0; i < results.size(); i++) {
            String definition = (String) results.get(i);

            if(definition.endsWith(parameterTypes)) {
                return definition.substring(prefix.length() + 1, definition.lastIndexOf(parameterTypes));
            }
        }

        return null;
    }

    private String stringifyMethodParameterTypes(Method method) {
        StringBuffer buffer = new StringBuffer(SPACE);
        Class[] parameterTypes = method.getParameterTypes();

        for (int i = 0; i < parameterTypes.length; i++) {
            buffer.append(parameterTypes[i].getName());

            if (i < parameterTypes.length - 1) {
                buffer.append(COMMA);
            }
        }

        return buffer.toString();
    }

    private boolean didNotReadOffEndOfLine(String expected) {
        return !expected.contains(NEWLINE);
    }

    public Constructor lookupConstructor(ClassLoader classLoader, String className, String paramNames) {
        String mappings = readMappings(getResource(classLoader));
        String classAndConstructorAndParamNames = NEWLINE + className + SPACE + className.substring(className.lastIndexOf(".") + 1) + SPACE + paramNames + SPACE;

        filterMappingByPrefix(classAndConstructorAndParamNames, getResource(classLoader));
        int index = mappings.indexOf(classAndConstructorAndParamNames);
        if (index != -1) {
            String methodParamTypes = extractParameterTypesFromFoundMethod(index, classAndConstructorAndParamNames, mappings);
            Class loadedClass;
            try {
                loadedClass = classLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                return null; // or could throw a/the exception
            }
            Constructor constructors[] = loadedClass.getConstructors();
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

    private String extractParameterTypesFromFoundMethod(int index, String classAndConstructorAndParamNames, String mappings) {
        int start = index + classAndConstructorAndParamNames.length();
        int end = mappings.indexOf(NEWLINE, start + 1);
        return mappings.substring(start, end).trim();
    }

    private String turnClassArrayIntoRepresentativeString(Class[] parameters) {
        String parameterTypes = EMPTY;
        for (int k = 0; k < parameters.length; k++) {
            parameterTypes = parameterTypes + parameters[k].getName();
            parameterTypes = parameterTypes + ((k + 1 < parameters.length) ? COMMA : EMPTY);
        }
        return parameterTypes;
    }

    private Reader getResource(ClassLoader classLoader) {
        InputStream inputStream = classLoader.getResourceAsStream(paranamerResource);
        if (inputStream == null) {
            throw new NoSuchElementException("Failed to find resource " + paranamerResource);
        }
        return new InputStreamReader(inputStream);
    }

    private List filterMappingByPrefix(String prefix, Reader resource) {
        List results = new ArrayList();

        try {
            LineNumberReader lineReader = new LineNumberReader(resource);
            String line = readLine(lineReader);

            while (line != null) {
                if (line.startsWith(prefix)) {
                    results.add(line.trim());
                }
                line = readLine(lineReader);
            }
            return results;
        } finally {
            try {
                if (resource != null) {
                    resource.close();
                }
            } catch (IOException ignore) {
                // ignore
            }
        }
    }

    private String readMappings(Reader resource) {
        StringBuffer buffer = new StringBuffer();
        try {
            LineNumberReader lineReader = new LineNumberReader(resource);
            String line = readLine(lineReader);
            while (line != null) {
                buffer.append(line).append(NEWLINE);
                line = readLine(lineReader);
            }
            return buffer.toString();
        } finally {
            try {
                if (resource != null) {
                    resource.close();
                }
            } catch (IOException ignore) {
                // ignore
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

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[DefaultParanamer paranamerResource=");
        sb.append(paranamerResource);
        sb.append("]");
        return sb.toString();
    }
}
