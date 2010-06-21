/***
 *
 * Copyright (c) 2009 Paul Hammant
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

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class AnnotationParanamerTestCase {

    @Test
    public void testCanFindNamedAnnotationsForConstructors() {
        Paranamer paranamer = new AnnotationParanamer();
        String[] names = paranamer.lookupParameterNames(Red.class.getConstructors()[0]);
        Assert.assertNotNull(names);
        Assert.assertEquals(1, names.length);
        Assert.assertEquals("FF0000", names[0]);
    }

    @Test
    public void testCanFindNamedAnnotationsForMethods() throws NoSuchMethodException {
        Paranamer paranamer = new AnnotationParanamer();
        String[] names = paranamer.lookupParameterNames(Red.class.getDeclaredMethod("rouge", String.class));
        Assert.assertNotNull(names);
        Assert.assertEquals(1, names.length);
        Assert.assertEquals("FF0000", names[0]);
    }

    @Test
    public void testCantFindNamedAnnotations() {
        Paranamer paranamer = new AnnotationParanamer();
        String[] names = paranamer.lookupParameterNames(Blue.class.getConstructors()[0], false);
        Assert.assertEquals(0, names.length);
    }

    @Test
    public void testCantFindNamedAnnotationsAndThrow() {
        Paranamer paranamer = new AnnotationParanamer();
        try {
            String[] names = paranamer.lookupParameterNames(Blue.class.getConstructors()[0], true);
        } catch (ParameterNamesNotFoundException e) {
            Assert.assertTrue(e.getMessage().indexOf("One or more @Named annotations missing") > -1);
            Assert.assertTrue(e.getMessage().indexOf("TestCase$Blue") > -1);
            Assert.assertTrue(e.getMessage().indexOf("methodOrCtor <init> and parameter types java.lang.String") > -1);
        }
    }


    public static class Red {
        public Red(@Named("FF0000") String color) {
        }
        public void rouge(@Named("FF0000") String color) {
        }
    }

    public static class Blue {
        public Blue(String color) {
        }
    }


    @Test
    public void testCanFindOverridenAnnotations() {
        Paranamer paranamer = new AnnotationParanamer() {
            @Override
            protected String getNamedValue(Annotation ann) {
                return ((SoCalled) ann).value();
            }

            @Override
            protected boolean isNamed(Annotation ann) {
                return ann instanceof SoCalled;
            }
        };
        String[] names = paranamer.lookupParameterNames(Green.class.getConstructors()[0]);
        Assert.assertNotNull(names);
        Assert.assertEquals(1, names.length);
        Assert.assertEquals("green", names[0]);
    }



    @Retention(RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    public static @interface SoCalled {
        String value();
    }

    public static class Green {
        public Green(@SoCalled("green") String color) {
        }
    }

    @Test
    public void testCanFindNamedAnnotationsWithFallback() {
        Paranamer paranamer = new AnnotationParanamer(new BytecodeReadingParanamer());
        String[] names = paranamer.lookupParameterNames(Yellow.class.getConstructors()[0], true);
        Assert.assertEquals(2, names.length);
        Assert.assertEquals("foo", names[0]);
        Assert.assertEquals("bar", names[1]);


    }

    public static class Yellow {
        public Yellow(String foo, @Named("bar") String otherParam) {
        }
    }



}
