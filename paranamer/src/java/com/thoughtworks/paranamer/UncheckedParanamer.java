package com.thoughtworks.paranamer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class UncheckedParanamer {
    private Paranamer delegate;

    public UncheckedParanamer(Paranamer delegate) {
        this.delegate = delegate;
    }

    public UncheckedParanamer() {
        this(new DefaultParanamer());
    }

    public Method uncheckedMethodLookup(ClassLoader classLoader, String className, String methodName, String paramNames) {
        Method method = delegate.lookupMethod(classLoader, className, methodName, paramNames);
        if (method == null) {
            throw new ParanamerRuntimeException("Paranamer could not find method signature");
        }
        return method;
    }

    public Constructor uncheckedConstructorLookup(ClassLoader classLoader, String className, String paramNames) {
        Constructor constructor = delegate.lookupConstructor(classLoader, className, paramNames);
        if (constructor == null) {
            throw new ParanamerRuntimeException("Paranamer could not find constructor signature");
        }
        return constructor;
    }

    public String toString() {
        return new StringBuffer("[UncheckedParanamer delegate=")
                .append(delegate)
                .append("]")
                .toString();
    }
}
