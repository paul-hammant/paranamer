package com.thoughtworks.paranamer;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

/**
 * More recent Java versions have a means to access parameter names via reflection
 * Only if -parameters is passed to javac though. This class now utilizes that.
 * The former DefaultParanamer is now renamed to LegacyParanamer (uses QDox to attach more static field info to .class files)
 * Really though in 2025, teams should not be using Paranamer at all.
 */

public class DefaultParanamer implements Paranamer {
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
            if (names[i].matches("^arg\\d+$")) {
                if (throwExceptionIfMissing) {
                    throw new ParameterNamesNotFoundException("Java9+ you need to compile with '-parameters' switch");
                } else {
                    return EMPTY_NAMES;
                }
            }
        }
        return names;
    }
}
