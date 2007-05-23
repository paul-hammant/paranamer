package com.thoughtworks.paranamer;

public class DefaultParanamerTestCase extends AbstractParanamerTestCase {
    
    protected void setUp() throws Exception {
        paranamer = new DefaultParanamer();
    }


    public void testLookupParameterNamesForConstructorWithStringArg() throws Exception {
        //for (int i = 0; i < 1000000; i++) {
            super.testLookupParameterNamesForConstructorWithStringArg();
        //}
    }
}
