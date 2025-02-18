package com.thoughtworks.paranamer;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

public class JavaNineOrAboveParanamer implements Paranamer {
    @Override
    public String[] lookupParameterNames(Executable methodOrConstructor) {
        return lookupParameterNames(methodOrConstructor, false);
    }

    @Override
    public String[] lookupParameterNames(Executable methodOrConstructor, boolean throwExceptionIfMissing) {
        String[] names = new String[methodOrConstructor.getParameters().length];
        Parameter[] x = methodOrConstructor.getParameters();
        for (int i = 0; i < x.length; i++) {
            names[i] = x[i].getName();
            if (throwExceptionIfMissing && names[i].matches("^arg\\d+$")) {
                throw new ParameterNamesNotFoundException("Java9+ you need to compile with '-parameters' switch");
            }
        }
        return names;
    }
}
