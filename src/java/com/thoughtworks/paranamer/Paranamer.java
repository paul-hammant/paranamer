package com.thoughtworks.paranamer;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

/**
 * Paranamer allows lookups of methods and constructors by parameter names.
 * It also provides lookup of all the possible parameter names for a given method. 
 * 
 * @author Paul Hammant
 */
public interface Paranamer {

    /**
     * Lookup a method 
     * 
     * @param classLoader the ClassLoader used for the lookup
     * @param className the name of the class to which the method belongs
     * @param methodName the method names
     * @param paramNames the CSV of the parameters of the method 
     * @return A Method or <code>null</null> if method not found 
     */
    public Method lookupMethod(ClassLoader classLoader, String className, String methodName, String paramNames);


    /**
     * Lookup a constructor
     * 
     * @param classLoader the ClassLoader used for the lookup
     * @param className the name of the class to which the constructor belongs
     * @param paramNames the CSV of the parameters of the constructor
     * @return A Constructor or <code>null</null> if constructor not found 
     */
    public Constructor lookupConstructor(ClassLoader classLoader, String className, String paramNames);


    /**
     * Lookup the possible parameter names of a given method name
     * 
     * @param classLoader the ClassLoader used for the lookup
     * @param className the name of the class to which the method belongs
     * @param methodName the method name
     * @return An array of String, each encoding a CSV of the parameter names
     */
    public String[] lookupParameterNames(ClassLoader classLoader, String className, String methodName);

    /**
     * Lookup the parameter names of a given method
     *
     * @param method the method to be searched for
     * @return A CSV list of the parameter names
     */
    public String lookupParameterNamesForMethod(Method method);

}
