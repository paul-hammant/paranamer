package com.thoughtworks.paranamer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class CachingParanamer implements Paranamer {
    private final Paranamer delegate;
    private final WeakHashMap classLoaderCache = new WeakHashMap();
    private final WeakHashMap methodCache = new WeakHashMap();

    public CachingParanamer(Paranamer paranamer) {
        delegate = paranamer;
    }

    public CachingParanamer() {
        delegate = new DefaultParanamer();
    }

    public Method lookupMethod(ClassLoader classLoader, String className, String methodName, String paramNames) {
        String key = className + " " + methodName + " " + paramNames;
        Method method = (Method) checkCache(classLoader, key);

        if(method == null) {
            method = delegate.lookupMethod(classLoader, className, methodName, paramNames);
            cacheIt(classLoader, key, method);
        }

        return method;
    }

    public Constructor lookupConstructor(ClassLoader classLoader, String className, String paramNames) {
        String key = className + " " + className.substring(className.lastIndexOf(".") + 1) + " " + paramNames;
        Constructor constructor = (Constructor) checkCache(classLoader, key);

        if(constructor == null) {
            constructor = delegate.lookupConstructor(classLoader, className, paramNames);
            cacheIt(classLoader, key, constructor);
        }

        return constructor;
    }

    public String[] lookupParameterNames(ClassLoader classLoader, String className, String methodName) {
        String key = className + " " + className + " " + methodName;

        String[] parameterNames = (String[]) checkCache(classLoader, key);

        if(parameterNames == null) {
            parameterNames = delegate.lookupParameterNames(classLoader, className, methodName);
            cacheIt(classLoader, key, parameterNames);
        }

        return parameterNames;
    }

    public String lookupParameterNamesForMethod(Method method) {
        if(methodCache.containsKey(method)) {
            return (String) methodCache.get(method);
        }

        String value = delegate.lookupParameterNamesForMethod(method);
        methodCache.put(method, value);

        return value;
    }

    private Object checkCache(ClassLoader classLoader, String key) {
        Map map = (Map) classLoaderCache.get(classLoader);

        if(map == null) {
            classLoaderCache.put(classLoader, new HashMap());
        } else {
            if(map.containsKey(key)) {
                return map.get(key);
            }
        }

        return null;
    }

    private void cacheIt(ClassLoader classLoader, String key, Object value) {
        Map map = (Map) classLoaderCache.get(classLoader);
        map.put(key, value);
    }

    public String toString() {
        return new StringBuffer("[CachingParanamer delegate=")
                .append(delegate)
                .append(", classLoaders=")
                .append(classLoaderCache).append("]").toString();
    }
}
