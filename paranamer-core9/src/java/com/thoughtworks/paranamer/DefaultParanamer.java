package com.thoughtworks.paranamer;

import java.lang.reflect.AccessibleObject;
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
    public String[] lookupParameterNames(AccessibleObject methodOrConstructor) {
        return lookupParameterNames(methodOrConstructor, false);
    }

    @Override
    public String[] lookupParameterNames(AccessibleObject methodOrConstructor, boolean throwExceptionIfMissing) {
        Executable methodOrConstructorExecutable = null;
        try {
            methodOrConstructorExecutable = (Executable) methodOrConstructor;
        } catch (ClassCastException e) {
            return EMPTY_NAMES; // should never happen unless someone passed a Field in.
        }
        String[] names = new String[methodOrConstructorExecutable.getParameters().length];
        Parameter[] x = methodOrConstructorExecutable.getParameters();
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
