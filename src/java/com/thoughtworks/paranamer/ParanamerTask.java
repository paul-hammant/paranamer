package com.thoughtworks.paranamer;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import java.io.IOException;

public class ParanamerTask extends Task {

    private String sourceDirectory;
    private String outputDirectory;

    public void execute() throws BuildException {
        ParanamerGeneration paranamer = new ParanamerGeneration();
        String parameterText = paranamer.generate(sourceDirectory);
        try {
            paranamer.write(outputDirectory, parameterText);
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
