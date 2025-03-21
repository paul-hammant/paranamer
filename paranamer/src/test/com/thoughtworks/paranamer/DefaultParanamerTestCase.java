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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class DefaultParanamerTestCase extends AbstractParanamerTestCase {

    @Before
    public void setUp() throws Exception {
        paranamer = new LegacyParanamer();
    }

    public static interface HelloService {
        public static final String __PARANAMER_DATA = "v1.0 \n"
              + "hello java.lang.String name \n";
        void hello(String name);
    }

    public static class HelloServiceImpl implements BytecodeReadingParanamerTestCase.HelloService {
        public static final String __PARANAMER_DATA = "v1.0 \n"
              + "hello java.lang.String name \n";
        public void hello(String name) {
        }
    }

    @Test
    public void testGetNameFromInterfaceMethod() throws NoSuchMethodException {
        assertArrayEquals(new String[]{"name"}, paranamer.lookupParameterNames(
                HelloServiceImpl.class.getDeclaredMethod("hello", new Class[]{String.class})));
        assertArrayEquals(new String[]{"name"}, paranamer.lookupParameterNames(
                HelloService.class.getDeclaredMethod("hello", new Class[]{String.class})));

    }

}
