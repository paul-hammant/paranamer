/***
 *
 * Copyright (c) 2007 Paul Hammant
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.thoughtworks.paranamer;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.util.WeakHashMap;

/**
 * Implementation of Paranamer which delegate to another Paranamer implementation, adding caching functionality.
 *
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public class AdaptiveCachingParanamer implements Paranamer {

    public static final String __PARANAMER_DATA = "v1.0 \n"
        + "com.thoughtworks.paranamer.AdaptiveCachingParanamer CachingParanamer \n"
        + "com.thoughtworks.paranamer.AdaptiveCachingParanamer CachingParanamer com.thoughtworks.paranamer.Paranamer delegate \n"
        + "com.thoughtworks.paranamer.AdaptiveCachingParanamer CachingParanamer com.thoughtworks.paranamer.Paranamer,com.thoughtworks.paranamer.Paranamer delegate,fallback\n"
        + "com.thoughtworks.paranamer.AdaptiveCachingParanamer toString \n"
        + "com.thoughtworks.paranamer.AdaptiveCachingParanamer lookupParameterNames java.lang.AccessibleObject methodOrCtor \n";

    private Paranamer delegate;
    private Paranamer fallback;
    private final WeakHashMap methodCache = new WeakHashMap();

    /**
     * Cache a DefaultParanamer's lookups.
     */
    public AdaptiveCachingParanamer() {
        this(new DefaultParanamer(), new BytecodeReadingParanamer());
    }


    /**
     * Cache a primary and secondary Paranamer instance (the second is a fallback to the first)
     * @param delegate first
     * @param fallback second
     */
    public AdaptiveCachingParanamer(Paranamer delegate, Paranamer fallback) {
        this.delegate = delegate;
        this.fallback = fallback;
        if (delegate == null || fallback == null || delegate == fallback) {
            throw new RuntimeException("must supply delegate and fallback (which must be different)");
        }
    }

    public String[] lookupParameterNames(AccessibleObject methodOrCtor) {
        if(methodCache.containsKey(methodOrCtor)) {
            return (String[]) methodCache.get(methodOrCtor);
        }

        String[] names = null;
        Class declaringClass = null;
        String name = null;
        if (methodOrCtor instanceof Method) {
            Method method = (Method) methodOrCtor;
            declaringClass = method.getDeclaringClass();
            name = method.getName();
        } else {
            Constructor constructor = (Constructor) methodOrCtor;
            declaringClass = constructor.getDeclaringClass();
            name = constructor.getName();
        }

        if (delegate.areParameterNamesAvailable(declaringClass, name) == Paranamer.PARAMETER_NAMES_FOUND) {
            names = delegate.lookupParameterNames(methodOrCtor);
        } else {
            names = fallback.lookupParameterNames(methodOrCtor);
        }
        methodCache.put(methodOrCtor, names);

        return names;
    }

    public int areParameterNamesAvailable(Class clazz, String ctorOrMethodName) {
        int i = delegate.areParameterNamesAvailable(clazz, ctorOrMethodName);
        if (i != Paranamer.PARAMETER_NAMES_FOUND) {
            i = fallback.areParameterNamesAvailable(clazz, ctorOrMethodName);
        }
        return i;
    }

    public String toString() {
         return new StringBuffer("[AdaptiveCachingParanamer delegate=")
         .append(delegate).append(", fallback=").append(fallback).append("]").toString();
     }

}