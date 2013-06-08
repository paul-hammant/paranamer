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

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AdaptiveParanamerTestCase {


    Method one = One.class.getMethods()[0];

    public interface One {
        void one();
    }

    @Test
    public void testLookupOfParameterNamesWhenPrimaryDoesNotHaveItButSecondaryDoes() {

        final Paranamer primary = mock(Paranamer.class);
        final Paranamer fallback = mock(Paranamer.class);

        AdaptiveParanamer paranamer = new AdaptiveParanamer(primary, fallback);

        when(primary.lookupParameterNames(one, false)).thenReturn(Paranamer.EMPTY_NAMES);
        when(fallback.lookupParameterNames(one, true)).thenReturn(new String[]{"a", "b"});

        String[] paramNames = paranamer.lookupParameterNames(one, true);
        Assert.assertEquals(Arrays.asList("a", "b"), Arrays.asList(paramNames));
    }

    @Test
    public void testLookupOfParameterNamesWhenPrimaryDoesHaveIt() {
        final Paranamer primary = mock(Paranamer.class);
        final Paranamer fallback = mock(Paranamer.class);

        AdaptiveParanamer paranamer = new AdaptiveParanamer(primary, fallback);

        when(primary.lookupParameterNames(one, false)).thenReturn(new String[]{"a", "b"});

        String[] paramNames = paranamer.lookupParameterNames(one, true);
        Assert.assertEquals(Arrays.asList("a", "b"), Arrays.asList(paramNames));

    }

}