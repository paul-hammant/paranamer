package com.thoughtworks.paranamer;

import junit.framework.TestCase;

import java.io.File;

public abstract class AbstractParanamerTestCase extends TestCase {


    //Ignore this. You, the end user, will use the Ant task to generate parameter names.
    protected void setUp() throws Exception {
        ParanamerGeneration paranamerGeneration = new ParanamerGeneration();
        String parameterSignatures = paranamerGeneration.generate(new File(".").getAbsolutePath() + "/src/java");
        paranamerGeneration.write(new File(".").getAbsolutePath() + "/target/classes/", parameterSignatures);
    }


}
