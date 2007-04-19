package com.thoughtworks.paranamer.generator;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import com.thoughtworks.paranamer.ParanamerConstants;

public class QdoxParanamerGeneratorTestCase extends AbstractQDoxParanamerTestCase {

    String allParameters =
                    "com.thoughtworks.paranamer.generator.QdoxParanamerGenerator QdoxParanamerGenerator \n" +
                    "com.thoughtworks.paranamer.generator.QdoxParanamerGenerator QdoxParanamerGenerator paranamerResource java.lang.String \n" +
                    "com.thoughtworks.paranamer.generator.QdoxParanamerGenerator generate sourcePath java.lang.String \n" +
                    "com.thoughtworks.paranamer.generator.QdoxParanamerGenerator write outputPath,content java.lang.String,java.lang.String \n";

    private ParanamerGenerator generator;
    
    protected void setUp() throws Exception {
        generator = new QdoxParanamerGenerator();
    }

    public void testCanGenerateParameterNamesFromSource() throws Exception {
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
