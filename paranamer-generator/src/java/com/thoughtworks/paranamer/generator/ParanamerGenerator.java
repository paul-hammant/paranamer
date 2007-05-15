package com.thoughtworks.paranamer.generator;

import java.io.IOException;

/**
 * Generates parameter names from a source path and writes content
 * to a given output path
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public interface ParanamerGenerator {

    void processSourcePath(String sourcePath, String outputPath) throws IOException;

}