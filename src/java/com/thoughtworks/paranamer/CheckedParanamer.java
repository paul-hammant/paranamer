package com.thoughtworks.paranamer;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

public class CheckedParanamer {

    private Paranamer delegate;

    public CheckedParanamer(Paranamer delegate) {
        this.delegate = delegate;
    }

    public CheckedParanamer() {
        this.delegate = new ParanamerImpl();
    }

    public Method checkedMethodLookup(ClassLoader classLoader, String className, String methodName, String paramNames) throws ParanamerException {
        Method method = delegate.lookupMethod(classLoader, className, methodName, paramNames);
        if (method == null) {
            throw new ParanamerException("Paranamer could not find method signature");
        }
        return method;
    }

    public Constructor checkedConstructorLookup(ClassLoader classLoader, String className, String paramNames) throws ParanamerException {
        Constructor ctor = delegate.lookupConstructor(classLoader, className, paramNames);
        if (ctor == null) {
            throw new ParanamerException("Paranamer could not find constructor signature");
        }
        return ctor;
    }
}
