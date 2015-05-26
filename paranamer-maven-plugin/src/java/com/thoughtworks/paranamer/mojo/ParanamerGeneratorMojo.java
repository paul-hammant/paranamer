/***
 *
 * Copyright (c) 2007 Paul Hammant
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.thoughtworks.paranamer.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import com.thoughtworks.paranamer.generator.ParanamerGenerator;
import com.thoughtworks.paranamer.generator.QdoxParanamerGenerator;

import java.io.IOException;


/**
 * Mojo to generate parameter names via ParanamerGenerator
 *
 * @author Mauro Talevi
 */
@Mojo( name = "generate",
       defaultPhase = LifecyclePhase.COMPILE,
       requiresDependencyResolution = ResolutionScope.COMPILE)
public class ParanamerGeneratorMojo extends AbstractMojo {

	/** THe system property name, which if set, will skip execution of this mojo */
	public static final String skipProp = "skipParanamer";

	/**
	 * Determines if the skip property is set
	 *
	 * @return true if the skip property is set, false otherwise
	 */
	public boolean skip() {
		return System.getProperties().containsKey(skipProp);
	}

    /**
     * The directory containing the Java source files
     *
     */
    @Parameter(defaultValue = "${project.build.sourceDirectory}", required = true)
    protected String sourceDirectory;

    /**
     * The directory where the Paranamer generator will write the output
     *
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
    protected String outputDirectory;

    /**
     * The Paranamer generator
     */
    private ParanamerGenerator generator = new QdoxParanamerGenerator();

    public void execute() throws MojoExecutionException, MojoFailureException {
    	if(skip()) {
    		getLog().info("\n\tSkipping ParanamerGeneratorMojo as \"" + skipProp + "\" system property is set\n");
    		return;
    	}
        getLog().info("Generating parameter names from " + sourceDirectory + " to " + outputDirectory);
        try {
            generator.processSourcePath(sourceDirectory, outputDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to generate parameter names from "+sourceDirectory, e);
        }
    }

}
