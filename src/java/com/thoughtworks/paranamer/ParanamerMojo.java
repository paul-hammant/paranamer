package com.thoughtworks.paranamer;

import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;


/**
 * Mojo to generate parameter names
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
     * @parameter
     * @required
     */
    protected String sourceDirectory;

    /**
     * @parameter
     * @required
     */
    protected String outputDirectory;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().debug( "Generating parameter names from " + sourceDirectory + " to " + outputDirectory );
        QdoxParanamerGenerator generator = new QdoxParanamerGenerator();
        String parameterText = generator.generate(sourceDirectory);
        try {
            generator.write(outputDirectory, parameterText);
        } catch (IOException e) {            
            throw new MojoExecutionException("Failed to generate parameter names", e);
        }
    }

}
