package com.thoughtworks.paranamer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import junit.framework.TestCase;

/**
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public abstract class AbstractParanamerTestCase extends TestCase {

    protected Paranamer paranamer;

    public void testLookupMethodReturnsNullIfMethodNotFound()
            throws IOException {
        Object method = paranamer.lookupMethod(
                Paranamer.class.getClassLoader(),
                "com.thoughtworks.paranamer.DefaultParanamer",
                "lookupParameterNames", "hello,goodbye");
        assertNull(method);
    }

    public void testLookupMethodReturnsNullIfClassNotFound()
            throws IOException {
        Object method = paranamer.lookupMethod(
                Paranamer.class.getClassLoader(), "paranamer.Footle",
                "generate", "sourcePath,rootPackage");
        assertNull(method);
    }

    public void testLookupParanamerCanIndicateThatUnableToGetParameterNamesForBogusClass()
            throws IOException {
        ClassLoader cl = new ClassLoader(){
            public InputStream getResourceAsStream(String resource){
                return null;
            }
        };
        Object method = paranamer.lookupMethod(cl, "Blah", "doBlah", "blah");
        assertNull(method);
        int x = paranamer.areParameterNamesAvailable(cl,"Blah", "doBlah");
        assertEquals(Paranamer.NO_PARAMETER_NAMES_LIST, x);
    }

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

    public void testLookupMethodEndsWithUnknownClass() throws IOException {
        Object method = paranamer.lookupMethod(
                Paranamer.class.getClassLoader(), "foo.Bar", "generate",
                "hello,goodbye");
        assertNull(method);
    }

    public void testLookupFailsIfResourceMissing() throws IOException {
        Paranamer paranamer = new DefaultParanamer("/inexistent/resource");
        Method m = paranamer.lookupMethod(Paranamer.class.getClassLoader(),
                "com.thoughtworks.paranamer.DefaultParanamer",
                "lookupMethod", "classLoader,c,m,p");
        assertTrue("null expected", m == null);
    }

    public void testMethodWithNoArgsCanBeRetrievedByParameterNames()
            throws IOException, NoSuchMethodException {
        Method method = paranamer.lookupMethod(
                Paranamer.class.getClassLoader(),
                "com.thoughtworks.paranamer.DefaultParanamer", "toString", "");
        assertEquals(DefaultParanamer.class.getMethod("toString", new Class[0]),
                method);
    }

    public void testLookupParameterNamesForMethodWhenNoArg() throws Exception {
        Method method = DefaultParanamer.class.getMethod("toString", new Class[0]);
        String[] names = paranamer.lookupParameterNames(method);
        assertThatParameterNamesMatch("", names);
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
