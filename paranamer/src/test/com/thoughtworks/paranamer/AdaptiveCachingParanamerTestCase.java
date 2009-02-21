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

import java.lang.reflect.Method;
import java.lang.reflect.AccessibleObject;
import java.util.Arrays;

import junit.framework.TestCase;
import org.jmock.MockObjectTestCase;
import org.jmock.Mock;

public class AdaptiveCachingParanamerTestCase extends MockObjectTestCase {

    private int count = 0;
    Method one = One.class.getMethods()[0];
    Method two = Two.class.getMethods()[0];
    private int fallbackCount = 0;

    public interface One {
        void one();
    }

    public interface Two {
        void two();
    }

    public void testCachedLookupOfParameterNamesWhenPrimaryDoesNotHaveItButSecondaryDoes() {

        Mock primary = mock(Paranamer.class);
        Mock fallback = mock(Paranamer.class);

        Paranamer cachingParanamer = new AdaptiveCachingParanamer((Paranamer) primary.proxy(), (Paranamer) fallback.proxy());
        primary.expects(once()).method("areParameterNamesAvailable").with(same(One.class),eq("one")).will(returnValue(Paranamer.NO_PARAMETER_NAMES_FOR_CLASS));
        fallback.expects(once()).method("lookupParameterNames").with(same(one)).will(returnValue(new String[] {"a","b"}));
        String[] paramNames = cachingParanamer.lookupParameterNames(one);
        assertEquals(Arrays.asList(new String[]{"a", "b"}), Arrays.asList(paramNames));

        paramNames = cachingParanamer.lookupParameterNames(one);

    }

    public void testCachedLookupOfParameterNamesWhenPrimaryDoesHaveIt() {

        Mock primary = mock(Paranamer.class);
        Mock fallback = mock(Paranamer.class);

        Paranamer cachingParanamer = new AdaptiveCachingParanamer((Paranamer) primary.proxy(), (Paranamer) fallback.proxy());
        primary.expects(once()).method("areParameterNamesAvailable").with(same(One.class),eq("one")).will(returnValue(Paranamer.PARAMETER_NAMES_FOUND));
        primary.expects(once()).method("lookupParameterNames").with(same(one)).will(returnValue(new String[] {"a","b"}));
        String[] paramNames = cachingParanamer.lookupParameterNames(one);
        assertEquals(Arrays.asList(new String[]{"a", "b"}), Arrays.asList(paramNames));

        paramNames = cachingParanamer.lookupParameterNames(one);

    }

}