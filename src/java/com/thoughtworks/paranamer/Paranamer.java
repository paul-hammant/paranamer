package com.thoughtworks.paranamer;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

public interface Paranamer {

    /**
     *
     * Lookup a method, and return null if its not there
     *
     */
    public Method lookupMethod(ClassLoader classLoader, String className, String methodName, String paramNames);

    /**
     *
     * Lookup a constructor, and return null if its not there
     *
     */
    public Constructor lookupConstructor(ClassLoader classLoader, String className, String paramNames);


    /**
     *
     * Lookup parameter names choices, for class/method only
     *
     */
    public String[] lookupParameterNames(ClassLoader classLoader, String className, String methodName);




}
