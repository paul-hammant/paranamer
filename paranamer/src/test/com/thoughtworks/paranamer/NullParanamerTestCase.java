package com.thoughtworks.paranamer;

import java.lang.reflect.Method;
import junit.framework.TestCase;

public class NullParanamerTestCase extends TestCase {

    public void testNoNamesForMethod() throws Exception {
        NullParanamer paranamer = new NullParanamer();
        Method method = DefaultParanamer.class.getDeclaredMethod("getParameterTypeName", new Class[] {Class.class});
        String[] names = paranamer.lookupParameterNames(method);
        assertEquals(0, names.length);
    }

    public void testNoNamesForMethodCanThrowReequiredException() throws Exception {
        NullParanamer paranamer = new NullParanamer();
        Method method = DefaultParanamer.class.getDeclaredMethod("getParameterTypeName", new Class[] {Class.class});
        try {
            paranamer.lookupParameterNames(method, true);
            fail("should have barfed");
        } catch (ParameterNamesNotFoundException e) {
            // expected
        }
    }

}
