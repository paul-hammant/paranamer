package com.thoughtworks.paranamer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class DefaultParanamer implements Paranamer {
    private static final String EMPTY = "";
    private static final String COMMA = ",";
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
        Class loadedClass;
        try {
            loadedClass = classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            return null; // or could throw a/the exception
        }

        // handle no arg method
        try {
            if (EMPTY.equals(paramNames)) {
                return loadedClass.getMethod(methodName, new Class[0]);
            }
        } catch (NoSuchMethodException ignore) {
            return null;
        }

        String classAndMethodAndParameterNames = getClassAndMethodAndParameterNames(className, methodName, paramNames);
        Reader resource = getResource(classLoader);
        if (resource == null) {
            return null;
        }
        List results = filterMappingByPrefix(classAndMethodAndParameterNames, resource);

        if (results.isEmpty()) {
            return null;
        }

        for (Iterator iterator = results.iterator(); iterator.hasNext();) {
            String definition = (String) iterator.next();
            String methodParamTypes = definition.substring(classAndMethodAndParameterNames.length() + 1);
            Method methods[] = loadedClass.getMethods();

            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];

                if (method.getName().equals(methodName)) {
                    Class[] parameters = method.getParameterTypes();
                    String paramTypes = stringifyClassArray(parameters);

                    if (paramTypes.equals(methodParamTypes)) {
                        return method;
                    }
                }
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Constructor lookupConstructor(ClassLoader classLoader, String className, String paramNames) {
        Class loadedClass;
        try {
            loadedClass = classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            return null; // or could throw a/the exception
        }

        // handle no arg ctor
        try {
            if (EMPTY.equals(paramNames)) {
                return loadedClass.getConstructor(new Class[0]);
            }
        } catch (NoSuchMethodException ignore) {
            return null;
        }

        String classAndConstructorAndParamNames = className + SPACE + className.substring(className.lastIndexOf(".") + 1) + SPACE + paramNames + SPACE;
        Reader resource = getResource(classLoader);
        if (resource == null) {
            return null;
        }
        List results = filterMappingByPrefix(classAndConstructorAndParamNames, resource);

        if (results.isEmpty()) {
            return null;
        }

        String definition = (String) results.get(0);
        String methodParamTypes = definition.substring(classAndConstructorAndParamNames.length());
        Constructor constructors[] = loadedClass.getConstructors();

        for (int i = 0; i < constructors.length; i++) {
            Constructor constructor = constructors[i];
            Class[] parameters = constructor.getParameterTypes();
            String paramTypes = stringifyClassArray(parameters);
            if (paramTypes.equals(methodParamTypes)) {
                return constructor;
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String[] lookupParameterNames(ClassLoader classLoader, String className, String methodName) {
        String classAndMethodName = className + SPACE + methodName + SPACE;
        List results = filterMappingByPrefix(classAndMethodName, getResource(classLoader));
        List matches = new ArrayList();

        for (int i = 0; i < results.size(); i++) {
            String definition = (String) results.get(i);

            if (classAndMethodName.length() <= definition.length()) {
                definition = definition.substring(classAndMethodName.length());
                definition = definition.substring(0, definition.indexOf(SPACE));
                matches.add(definition);
            }
        }

        return (String[]) matches.toArray(new String[matches.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public String lookupParameterNamesForMethod(Method method) {
        if (method.getParameterTypes().length == 0) { // no arguments ... return empty string
            return EMPTY;
        }

        Class declaringClass = method.getDeclaringClass();
        String parameterTypes = stringifyClassArray(method.getParameterTypes());
        String prefix = declaringClass.getName() + SPACE + method.getName();

        List results = filterMappingByPrefix(prefix, getResource(declaringClass.getClassLoader()));

        for (int i = 0; i < results.size(); i++) {
            String definition = (String) results.get(i);

            if (definition.endsWith(parameterTypes)) {
                return definition.substring(prefix.length() + 1, definition.lastIndexOf(parameterTypes)).trim();
            }
        }

        return null;
    }

    private String getClassAndMethodAndParameterNames(String className, String methodName, String paramNames) {
        StringBuffer buffer = new StringBuffer(className).append(SPACE).append(methodName);
        if (!paramNames.equals(EMPTY)) {
            buffer.append(SPACE).append(paramNames);
        }
        return buffer.toString();
    }

    private String stringifyClassArray(Class[] parameterTypes) {
        StringBuffer buffer = new StringBuffer(EMPTY);

        for (int i = 0; i < parameterTypes.length; i++) {
            buffer.append(parameterTypes[i].getName());

            if (i < parameterTypes.length - 1) {
                buffer.append(COMMA);
            }
        }

        return buffer.toString();
    }

    private Reader getResource(ClassLoader classLoader) {
        InputStream inputStream = classLoader.getResourceAsStream(paranamerResource);
        if (inputStream == null) {
            return null;
        }
        return new InputStreamReader(inputStream);
    }

    /**
     * Filter the paranamer mappings and only return lines matching the prefix passed in.
     */
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

    private String readLine(LineNumberReader lineReader) {
        try {
            return lineReader.readLine();
        } catch (IOException e) {
            return null; // or throw an exception if you prefer
        }
    }

    public String toString() {
        return new StringBuffer()
                .append("[DefaultParanamer paranamerResource=")
                .append(paranamerResource)
                .append("]")
                .toString();
    }

    public int isParameterNameDataAvailable(ClassLoader classLoader, String className, String ctorOrMethodName) {
        Reader reader = getResource(classLoader);
        if (reader == null) {
            return NO_PARAMETER_NAMES_LIST;
        }
        String clazzName = className + SPACE + className.substring(className.lastIndexOf(".") + 1) + SPACE;
        List list = filterMappingByPrefix(clazzName, reader);
        if (list.size() == 0) {
            return NO_PARAMETER_NAME_DATA_FOR_THAT_CLASS;
        }
        String classAndConstructorOrMethodNames = className + SPACE + className.substring(className.lastIndexOf(".") + 1) + SPACE + ctorOrMethodName + SPACE;
        list = filterMappingByPrefix(classAndConstructorOrMethodNames, reader);
        if (list.size() == 0) {
            return NO_PARAMETER_NAME_DATA_FOR_THAT_CLASS_AND_ARG;
        }

        return PARAMETER_NAME_DATA_FOUND;
    }
}
