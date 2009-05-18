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
import java.util.WeakHashMap;

/**
 * Implementation of Paranamer which delegate to another Paranamer implementation, adding caching functionality.
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public class CachingParanamer implements Paranamer {

    public static final String __PARANAMER_DATA = "v1.0 \n"
        + "com.thoughtworks.paranamer.CachingParanamer CachingParanamer \n"
        + "com.thoughtworks.paranamer.CachingParanamer CachingParanamer com.thoughtworks.paranamer.Paranamer delegate \n"
        + "com.thoughtworks.paranamer.CachingParanamer toString \n"
        + "com.thoughtworks.paranamer.CachingParanamer lookupParameterNames java.lang.AccessibleObject methodOrCtor \n";

    private Paranamer delegate;
    private final WeakHashMap methodCache = new WeakHashMap();

    public CachingParanamer() {
        this(new DefaultParanamer());
    }

    public CachingParanamer(Paranamer delegate) {
        this.delegate = delegate;
    }

    /**
     * @Deperecated Use 'new CachingParanamer(new AdaptiveParanamer())' instead.
     */
    public void switchtoAsm() {
        delegate = new BytecodeReadingParanamer();
    }

    public String[] lookupParameterNames(AccessibleObject methodOrConstructor) {
        return lookupParameterNames(methodOrConstructor, true);
    }

    public String[] lookupParameterNames(AccessibleObject methodOrCtor, boolean throwExceptionIfMissing) {
        if(methodCache.containsKey(methodOrCtor)) {
            return (String[]) methodCache.get(methodOrCtor);
        }

        String[] names = delegate.lookupParameterNames(methodOrCtor, throwExceptionIfMissing);
        methodCache.put(methodOrCtor, names);

        return names;
    }

    /**
     * @Deperecated Use 'new CachingParanamer(new AdaptiveParanamer())' instead.
     */
    public int areParameterNamesAvailable(Class clazz, String ctorOrMethodName) {
        return delegate.areParameterNamesAvailable(clazz, ctorOrMethodName);
    }

    public String toString() {
         return new StringBuffer("[CachingParanamer delegate=")
         .append(delegate).append("]").toString();
     }

}
