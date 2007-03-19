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
     * Parameter names are generally not available
     */
    int NO_PARAMETER_NAMES_LIST = 1;
    /**
     * Parameter names are available, but not for that class
     */
    int NO_PARAMETER_NAME_DATA_FOR_THAT_CLASS = 2;
    /**
     * Parameter names are available for that class, but not for that constructor or method
     */
    int NO_PARAMETER_NAME_DATA_FOR_THAT_CLASS_AND_ARG = 3;
    /**
     * Parameter names are available for that class and constructor/method
     */
    int PARAMETER_NAME_DATA_FOUND = 0;

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

    /**
     * Lookup the parameter names of a given method
     *
     * @param classLoader the ClassLoader used for the lookup
     * @param className the name of the class to which the constructor belongs
     * @param ctorOrMethodName the method or constructor
     * @return a code suggesting what the availability of parameter name info is
     */
    public int isParameterNameDataAvailable(ClassLoader classLoader, String className, String ctorOrMethodName);


}
