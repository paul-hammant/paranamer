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

public class DefaultParanamer implements Paranamer {
    private static final String[] EMPTY_NAMES = new String[]{};
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

    public String[] lookupParameterNames(Method method) {
        if (method.getParameterTypes().length == 0) { // no arguments ... return empty string
            return EMPTY_NAMES;
        }
        Class declaringClass = method.getDeclaringClass();
        String parameterTypes = toNamesCSV(method.getParameterTypes());
        String prefix = declaringClass.getName() + SPACE + method.getName();
        return getNames(declaringClass, parameterTypes, prefix);
    }

    public String[] lookupParameterNames(Constructor constructor) {
        if (constructor.getParameterTypes().length == 0) { // no arguments ... return empty string
            return EMPTY_NAMES;
        }
        Class declaringClass = constructor.getDeclaringClass();
        String parameterTypes = toNamesCSV(constructor.getParameterTypes());
        String prefix = declaringClass.getName() + SPACE + constructor.getName().substring(constructor.getName().lastIndexOf(".")+1);
        return getNames(declaringClass, parameterTypes, prefix);
    }

    private String[] getNames(Class declaringClass, String parameterTypes, String prefix) {
        List results = filterMappingByPrefix(prefix, getParameterListResource(declaringClass.getClassLoader()));
        for (int i = 0; i < results.size(); i++) {
            String definition = (String) results.get(i);
            if (definition.endsWith(parameterTypes)) {
                String csvNames = definition.substring(prefix.length() + 1, definition.lastIndexOf(parameterTypes)).trim();
                return csvNames.split(COMMA);
            }
        }
        return null;
    }

    public int areParameterNamesAvailable(ClassLoader classLoader, String className, String constructorOrMethodName) {
        Reader reader = getParameterListResource(classLoader);
        
        if (reader == null) {
            return NO_PARAMETER_NAMES_LIST;
        }
        String clazzName = className + SPACE;
        List list = filterMappingByPrefix(clazzName, reader);
        if (list.size() == 0) {
            return NO_PARAMETER_NAMES_FOR_CLASS;
        }
        reader = getParameterListResource(classLoader);

        String classAndConstructorOrMethodNames = className + SPACE + constructorOrMethodName + SPACE;
        list = filterMappingByPrefix(classAndConstructorOrMethodNames, reader);
        if (list.size() == 0) {
            return NO_PARAMETER_NAMES_FOR_CLASS_AND_MEMBER;
        }

        return PARAMETER_NAMES_FOUND;
    }


    private String toNamesCSV(Class[] parameterTypes) {
        StringBuffer sb = new StringBuffer(EMPTY);
        for (int i = 0; i < parameterTypes.length; i++) {
            sb.append(parameterTypes[i].getName());
            if (i < parameterTypes.length - 1) {
                sb.append(COMMA);
            }
        }
        return sb.toString();
    }

    private Reader getParameterListResource(ClassLoader classLoader) {
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

}
