package com.thoughtworks.paranamer;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

/**
 * Paranamer allows lookups of methods and constructors by parameter names.
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public interface Paranamer {

    /**
     * Parameter names are available for that class and constructor/method
     */
    int PARAMETER_NAMES_FOUND = 0;
    /**
     * Parameter names are generally not available
     */
    int NO_PARAMETER_NAMES_LIST = 1;
    /**
     * Parameter names are available, but not for that class
     */
    int NO_PARAMETER_NAMES_FOR_CLASS = 2;
    /**
     * Parameter names are available for that class, but not for that constructor or method
     */
    int NO_PARAMETER_NAMES_FOR_CLASS_AND_MEMBER = 3;

    /**
     * Lookup the parameter names of a given constructor
     *
     * @param constructor the Constructor for which the parameter names are looked up
     * @return A list of the parameter names
     * @throws ParameterNamesNotFoundException if no parameter names are found
     */
    public String[] lookupParameterNames(Constructor constructor);

    /**
     * Lookup the parameter names of a given method
     *
     * @param method the Method for which the parameter names are looked up
     * @return A list of the parameter names
     * @throws ParameterNamesNotFoundException if no parameter names are found
     */
    public String[] lookupParameterNames(Method method);

    /**
     * Determine if the parameter names are available
     *
     * @param classLoader the ClassLoader used for the lookup
     * @param className the name of the class to which the method or constructor belongs
     * @param constructorOrMethodName the name of the method or constructor
     * @return An int encoding the parameter names availability 
     */
    public int areParameterNamesAvailable(ClassLoader classLoader, String className, String constructorOrMethodName);

}
