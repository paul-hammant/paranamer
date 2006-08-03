package com.thoughtworks.paranamer;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

public class UncheckedParanamer {

    private Paranamer delegate;

    public UncheckedParanamer(Paranamer delegate) {
        this.delegate = delegate;
    }

    public UncheckedParanamer() {
        this.delegate = new DefaultParanamer();
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
}
