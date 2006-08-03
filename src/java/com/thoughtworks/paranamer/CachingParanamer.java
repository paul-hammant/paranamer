package com.thoughtworks.paranamer;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class CachingParanamer implements Paranamer {

    private final Paranamer delegate;
    private WeakHashMap classLoaders = new WeakHashMap();

    public CachingParanamer(Paranamer paranamer) {
        delegate = paranamer;
    }

    public CachingParanamer() {
        delegate = new DefaultParanamer();
    }

    public Method lookupMethod(ClassLoader classLoader, String className, String methodName, String paramNames) {
        String key = className + " " + methodName + " " + paramNames;
        Map map = (Map) classLoaders.get(classLoader);
        Method method;
        if (map != null) {
            method = (Method) map.get(key);
            if (method != null) {
                return method;
            }
        }
        if (map == null) {
            map = new HashMap();
            classLoaders.put(classLoader, map);
        }
        method = delegate.lookupMethod(classLoader, className, methodName, paramNames);
        map.put(key,method);
        return method;
    }

    public Constructor lookupConstructor(ClassLoader classLoader, String className, String paramNames) {
        String key = className + " " + className.substring(className.lastIndexOf(".")+1) + " " + paramNames;
        Map map = (Map) classLoaders.get(classLoader);
        Constructor constuctor;
        if (map != null) {
            constuctor = (Constructor) map.get(key);
            if (constuctor != null) {
                return constuctor;
            }
        }
        if (map == null) {
            map = new HashMap();
            classLoaders.put(classLoader, map);
        }
        constuctor = delegate.lookupConstructor(classLoader, className, paramNames);
        map.put(key,constuctor);
        return constuctor;

    }

    public String[] lookupParameterNames(ClassLoader classLoader, String className, String methodName) {
        return delegate.lookupParameterNames(classLoader, className, methodName);
    }

}
