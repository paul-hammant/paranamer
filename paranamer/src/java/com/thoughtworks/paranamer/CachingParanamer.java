package com.thoughtworks.paranamer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Implementation of Paranamer which delegate to another Paranamer implementation, adding caching functionality.
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public class CachingParanamer implements Paranamer {
    private static final String PERIOD = ".";
    private static final String SPACE = " ";
    private final Paranamer delegate;
    private final WeakHashMap classLoaderCache = new WeakHashMap();
    private final WeakHashMap methodCache = new WeakHashMap();

    public CachingParanamer() {
        this(new DefaultParanamer());
    }

    public CachingParanamer(Paranamer delegate) {
        this.delegate = delegate;
    }

    public Constructor lookupConstructor(ClassLoader classLoader, String className, String paramNames) {
        String key = className + SPACE + className.substring(className.lastIndexOf(PERIOD) + 1) + SPACE + paramNames;
        Constructor constructor = (Constructor) checkCache(classLoader, key);

        if(constructor == null) {
            constructor = delegate.lookupConstructor(classLoader, className, paramNames);
            cacheIt(classLoader, key, constructor);
        }

        return constructor;
    }

    public Method lookupMethod(ClassLoader classLoader, String className, String methodName, String paramNames) {
        String key = className + SPACE + methodName + SPACE + paramNames;
        Method method = (Method) checkCache(classLoader, key);

        if(method == null) {
            method = delegate.lookupMethod(classLoader, className, methodName, paramNames);
            cacheIt(classLoader, key, method);
        }

        return method;
    }

    public String[] lookupParameterNames(Method method) {
        if(methodCache.containsKey(method)) {
            return (String[]) methodCache.get(method);
        }

        String[] names = delegate.lookupParameterNames(method);
        methodCache.put(method, names);

        return names;
    }

    public String[] lookupParameterNames(Constructor constructor) {
        if(methodCache.containsKey(constructor)) {
            return (String[]) methodCache.get(constructor);
        }

        String[] names = delegate.lookupParameterNames(constructor);
        methodCache.put(constructor, names);

        return names;
    }

    public int areParameterNamesAvailable(ClassLoader classLoader, String className, String ctorOrMethodName) {
        return delegate.areParameterNamesAvailable(classLoader, className, ctorOrMethodName);
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
                 .append(classLoaderCache)
                 .append("]").toString();
     }

}
