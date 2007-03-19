package com.thoughtworks.paranamer;

import java.io.IOException;

public class AsmParanamerTestCase extends AbstractParanamerTestCase {
    
    protected void setUp() throws Exception {
        paranamer = new AsmParanamer();
    }

}
