package com.thoughtworks.paranamer.generator;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import com.thoughtworks.paranamer.ParanamerConstants;

public class QdoxParanamerGeneratorTestCase extends AbstractQDoxParanamerTestCase {

    String allParameters = 
              "com.thoughtworks.paranamer.generator.Enhancer enhance java.io.File,java.lang.String classFile,parameterNameData \n"
            + "com.thoughtworks.paranamer.generator.QdoxParanamerGenerator QdoxParanamerGenerator \n"
            + "com.thoughtworks.paranamer.generator.QdoxParanamerGenerator QdoxParanamerGenerator java.lang.String paranamerResource \n"
            + "com.thoughtworks.paranamer.generator.QdoxParanamerGenerator generate java.lang.String sourcePath \n"
            + "com.thoughtworks.paranamer.generator.QdoxParanamerGenerator write java.lang.String,java.lang.String outputPath,content \n";

    private ParanamerGenerator generator;
    
    protected void setUp() throws Exception {
        generator = new QdoxParanamerGenerator();
    }

    public void testCanGenerateParameterNamesFromSource() throws Exception {
        System.out.println(generator.generate(getSourcePath()));
        assertEquals(allParameters, generator.generate(getSourcePath()));
    }



    private String getSourcePath() {
        return new File(".").getAbsolutePath() + "/src/java";
    }

    public void testCanWriteParameterNames() throws IOException {
        File dir = createOutputDirectory();
        generator.write(dir.getAbsolutePath(), allParameters);
        String file = new File(dir.getPath()+File.separator+
                ParanamerConstants.DEFAULT_PARANAMER_RESOURCE).getAbsolutePath();
        assertTrue(new File(file).exists());
        assertEquals(ParanamerConstants.HEADER,
                new LineNumberReader(new FileReader(file)).readLine());
    }

    private File createOutputDirectory() {
        File dir = new File("target");
        dir.mkdirs();
        return dir;
    }


}
