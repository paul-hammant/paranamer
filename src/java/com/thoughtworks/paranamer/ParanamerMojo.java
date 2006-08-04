package com.thoughtworks.paranamer;

import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;


/**
 * Mojo to generate parameter names via Paranamer
 * 
 * @author Mauro Talevi
 * @goal run
 * @phase compile
 * @requiresDependencyResolution compile
 */
public class ParanamerMojo
    extends AbstractMojo
{

    /**
     * The directory containing the Java source files
     * @parameter
     * @required
     */
    protected String sourceDirectory;

    /**
     * The directory where the Paranamer generator will write the output
     * @parameter
     * @required
     */
    protected String outputDirectory;

    /** The Paranamer generator */
    private QdoxParanamerGenerator generator = new QdoxParanamerGenerator();

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().debug( "Generating parameter names from " + sourceDirectory + " to " + outputDirectory );
        try {
            generator.write(outputDirectory, generator.generate(sourceDirectory));
        } catch (IOException e) {            
            throw new MojoExecutionException("Failed to generate parameter names", e);
        }
    }

}
