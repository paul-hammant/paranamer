package com.thoughtworks.paranamer.ant;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import com.thoughtworks.paranamer.generator.ParanamerGenerator;
import com.thoughtworks.paranamer.generator.QdoxParanamerGenerator;

import java.io.IOException;

/**
* Ant Task to generate parameter names via ParanamerGenerator
* 
* @author Paul Hammant
* @author Mauro Talevi
*/
public class ParanamerGeneratorTask extends Task {
    private final ParanamerGenerator generator = new QdoxParanamerGenerator();
    private String sourceDirectory;
    private String outputDirectory;

    public void execute() throws BuildException {
        String parameterText = generator.generate(sourceDirectory);
        try {
            generator.write(outputDirectory, parameterText);
            System.out.println("Generated " + parameterText.length() + " characters of Parameter Name Data in '" + outputDirectory + "'");
        } catch (IOException e) {
            throw new BuildException("Paranamer encountered an IOException", e);
        }
    }

    public void setSourceDirectory(String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
}
