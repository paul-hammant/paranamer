package com.thoughtworks.paranamer;

import java.io.IOException;

/**
 * Generates parameter names from a source path and writes content
 * to a given output path
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public interface ParanamerGenerator {

    String generate(String sourcePath);

    void write(String outputPath, String content) throws IOException;

}