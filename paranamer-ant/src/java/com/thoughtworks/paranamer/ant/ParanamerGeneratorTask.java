package com.thoughtworks.paranamer.ant;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import com.thoughtworks.paranamer.generator.ParanamerGenerator;
import com.thoughtworks.paranamer.generator.QdoxParanamerGenerator;

import java.io.IOException;

/**
* Ant Task to processSourcePath parameter names via ParanamerGenerator
* 
* @author Paul Hammant
* @author Mauro Talevi
*/
public class ParanamerGeneratorTask extends Task {
    private final ParanamerGenerator generator = new QdoxParanamerGenerator();
    private String sourceDirectory;
    private String outputDirectory;

    public void execute() throws BuildException {
        try {
            generator.processSourcePath(sourceDirectory, outputDirectory);
            System.out.println("Generated Parameter Name Data for '"+sourceDirectory+"' in '" + outputDirectory + "'");
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
