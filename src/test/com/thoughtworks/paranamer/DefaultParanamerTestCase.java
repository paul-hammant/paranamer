package com.thoughtworks.paranamer;

import java.io.IOException;

public class DefaultParanamerTestCase extends AbstractParanamerTestCase {
    
    protected void setUp() throws Exception {
        paranamer = new DefaultParanamer();
    }

}
