package com.thoughtworks.paranamer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Implementation of Paranamer which delegate to another Paranamer implementation, adding unchecked exception
 * handling.
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public class UncheckedParanamer {
    private Paranamer delegate;

    public UncheckedParanamer() {
        this(new DefaultParanamer());
    }
    public UncheckedParanamer(Paranamer delegate) {
        this.delegate = delegate;
    }

    public Constructor uncheckedConstructorLookup(ClassLoader classLoader, String className, String paramNames) {
        Constructor constructor = delegate.lookupConstructor(classLoader, className, paramNames);
        if (constructor == null) {
            throw new ParanamerRuntimeException("Paranamer could not find constructor signature");
        }
        return constructor;
    }

    public Method uncheckedMethodLookup(ClassLoader classLoader, String className, String methodName, String paramNames) {
        Method method = delegate.lookupMethod(classLoader, className, methodName, paramNames);
        if (method == null) {
            throw new ParanamerRuntimeException("Paranamer could not find method signature");
        }
        return method;
    }

    public String toString() {
        return new StringBuffer("[UncheckedParanamer delegate=")
                .append(delegate)
                .append("]")
                .toString();
    }
}
