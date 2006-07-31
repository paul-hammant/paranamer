package com.thoughtworks.paranamer;

import junit.framework.TestCase;

import java.io.File;

public abstract class AbstractParanamerTestCase extends TestCase {


    //Ignore this. You, the end user, will use the Ant task to generate parameter names.
    protected void setUp() throws Exception {
        QdoxParanamerGenerator generator = new QdoxParanamerGenerator();
        String parameterSignatures = generator.generate(new File(".").getAbsolutePath() + "/src/java");
        generator.write(new File(".").getAbsolutePath() + "/target/classes/", parameterSignatures);
    }


}
