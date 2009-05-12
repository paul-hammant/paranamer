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
import java.util.Arrays;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;

public class AdaptiveParanamerTestCase extends MockObjectTestCase {

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

    public void testLookupOfParameterNamesWhenPrimaryDoesNotHaveItButSecondaryDoes() {

        Mock primary = mock(Paranamer.class);
        Mock fallback = mock(Paranamer.class);

        Paranamer paranamer = new AdaptiveParanamer((Paranamer) primary.proxy(), (Paranamer) fallback.proxy());
        primary.expects(once()).method("lookupParameterNames").with(same(one), eq(false)).will(returnValue(null));
        fallback.expects(once()).method("lookupParameterNames").with(same(one), eq(true)).will(returnValue(new String[] {"a","b"}));
        String[] paramNames = paranamer.lookupParameterNames(one, true);
        assertEquals(Arrays.asList(new String[]{"a", "b"}), Arrays.asList(paramNames));

    }

    public void testLookupOfParameterNamesWhenPrimaryDoesHaveIt() {

        Mock primary = mock(Paranamer.class);
        Mock fallback = mock(Paranamer.class);

        Paranamer paranamer = new AdaptiveParanamer((Paranamer) primary.proxy(), (Paranamer) fallback.proxy());
        primary.expects(once()).method("lookupParameterNames").with(same(one), eq(false)).will(returnValue(new String[] {"a","b"}));
        String[] paramNames = paranamer.lookupParameterNames(one, true);
        assertEquals(Arrays.asList(new String[]{"a", "b"}), Arrays.asList(paramNames));

    }

    public void testMissingAndWrongPermutationsAreThrown() {
        try {
            new AdaptiveParanamer(null, new DefaultParanamer());
        } catch (RuntimeException e) {
            assertEquals("must supply delegate and fallback (which must be different)", e.getMessage());
        }
        try {
            new AdaptiveParanamer(new DefaultParanamer(), null);
        } catch (RuntimeException e) {
            assertEquals("must supply delegate and fallback (which must be different)", e.getMessage());
        }
        try {
            DefaultParanamer pn = new DefaultParanamer();
            new AdaptiveParanamer(pn, pn);
        } catch (RuntimeException e) {
            assertEquals("must supply delegate and fallback (which must be different)", e.getMessage());
        }
    }

}