package com.thoughtworks.paranamer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

import junit.framework.TestCase;

/**
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public abstract class AbstractParanamerTestCase extends TestCase {

    protected Paranamer paranamer;

    public void testLookupParanamerCanIndicateAbleToGetParameterNames()
            throws IOException {
        int x = paranamer.areParameterNamesAvailable(DefaultParanamer.class.getClassLoader(),
                "com.thoughtworks.paranamer.DefaultParanamer", "lookupParameterNames");
        assertEquals(Paranamer.PARAMETER_NAMES_FOUND, x);
    }

    public void testLookupParanamerCanIndicateThatUnableToGetParameterNamesForRealClassButBogusMethod()
            throws IOException {
        int x = paranamer.areParameterNamesAvailable(DefaultParanamer.class.getClassLoader(),
                "com.thoughtworks.paranamer.DefaultParanamer", "fooo");
        assertEquals(Paranamer.NO_PARAMETER_NAMES_FOR_CLASS_AND_MEMBER, x);
    }

    public void testLookupParameterNamesForMethodWhenNoArg() throws Exception {
        Method method = DefaultParanamer.class.getMethod("toString", new Class[0]);
        String[] names = paranamer.lookupParameterNames(method);
        assertThatParameterNamesMatch("", names);
    }

    public void testLookupParameterNamesForConstructorWithStringArg() throws Exception {
        Constructor ctor = DefaultParanamer.class.getConstructor(new Class[] {String.class});
        String[] names = paranamer.lookupParameterNames(ctor);
        assertThatParameterNamesMatch("paranamerResource", names);
    }

    protected void assertThatParameterNamesMatch(String csv, String[] names) {
        assertEquals(csv, toCSV(names));
    }

    private String toCSV(String[] names) {
        StringBuffer sb = new StringBuffer();
        for ( int i = 0; i < names.length; i++ ){
            sb.append(names[i]);
            if ( i < names.length -1 ){
                sb.append(",");
            }            
        }
        return sb.toString();
    }
}
