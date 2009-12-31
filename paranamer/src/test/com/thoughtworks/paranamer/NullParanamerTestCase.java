package com.thoughtworks.paranamer;

import java.lang.reflect.Method;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class NullParanamerTestCase {

    @Test
    public void testNoNamesForMethod() throws Exception {
        NullParanamer paranamer = new NullParanamer();
        Method method = DefaultParanamer.class.getDeclaredMethod("getParameterTypeName", new Class[] {Class.class});
        String[] names = paranamer.lookupParameterNames(method);
        Assert.assertEquals(0, names.length);
    }

    @Test
    public void testNoNamesForMethodCanThrowReequiredException() throws Exception {
        NullParanamer paranamer = new NullParanamer();
        Method method = DefaultParanamer.class.getDeclaredMethod("getParameterTypeName", new Class[] {Class.class});
        try {
            paranamer.lookupParameterNames(method, true);
            Assert.fail("should have barfed");
        } catch (ParameterNamesNotFoundException e) {
            // expected
        }
    }

}
