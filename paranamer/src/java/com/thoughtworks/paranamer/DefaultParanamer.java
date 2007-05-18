package com.thoughtworks.paranamer;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

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
        String parameterTypeNames = parameterNamesCSV(method.getParameterTypes());
        String[] names = getNames(declaringClass, parameterTypeNames, method.getName() + SPACE);
        if ( names == null ){
            throw new ParameterNamesNotFoundException("No parameter names found for class '"+declaringClass+"', method "+ method.getName()
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
        String parameterTypeNames = parameterNamesCSV(constructor.getParameterTypes());
        String[] names = getNames(declaringClass, parameterTypeNames, constructorName(constructor) + SPACE);
        if ( names == null ){
            throw new ParameterNamesNotFoundException("No parameter names found for class '"+declaringClass+"', constructor "+ constructorName(constructor)
                    +" and parameter types "+parameterTypeNames);
        }
        return names;
    }

    private String constructorName(Constructor constructor) {
        return constructor.getName().substring(constructor.getName().lastIndexOf(DOT)+1);
    }

    private String[] getNames(Class declaringClass, String parameterTypes, String prefix) {
        String data = getParameterListResource(declaringClass);
        String line = findFirstMatchingLine(data, prefix + parameterTypes);
        String[] parts = line.split(SPACE);
        // assumes line structure: constructorName parameterTypes parameterNames
        if (parts.length == 3 && parts[1].equals(parameterTypes)) {
            String parameterNames = parts[2];
            return parameterNames.split(COMMA);
        }
        return null;
    }

    public int areParameterNamesAvailable(ClassLoader classLoader, Class clazz, String constructorOrMethodName) {
        String data = getParameterListResource(clazz);
        
        if (data == null) {
            return NO_PARAMETER_NAMES_LIST;
        }

        String line = findFirstMatchingLine(data, constructorOrMethodName + SPACE);
        if (line.length() == 0) {
            return NO_PARAMETER_NAMES_FOR_CLASS_AND_MEMBER;
        }

        return PARAMETER_NAMES_FOUND;
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

    private String getParameterListResource(Class declaringClass) {
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
            return (String) field.get(null);
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
    private String findFirstMatchingLine(String data, String prefix) {
        if (data == null) {
            return "";
        }
        int ix = data.indexOf(prefix);
        if (ix > 0) {
            int iy = data.indexOf("\n", ix);
            if(iy >0) {
                return data.substring(ix,iy);
            }
        }
        return "";
    }

    public String toString() {
        return super.toString(); 
    }
}
