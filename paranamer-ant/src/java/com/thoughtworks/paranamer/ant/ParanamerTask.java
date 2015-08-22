/**
 * Copyright (c) 2009 Timothy Cleaver. All rights reserved.
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
package com.thoughtworks.paranamer.ant;

import com.thoughtworks.paranamer.generator.QdoxParanamerGenerator;
import com.thoughtworks.qdox.JavaProjectBuilder;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;

/**
 * Ant Task to process parameter names. This ant task facilitates the
 * specification of the java source and class files to process as attributes,
 * nested file sets or a combination of both. The attributes applicable to
 * this ant task include a source directory, class directory, include pattern
 * and exclude pattern. i.e.:
 * &lt;paranamer srcdir="src" classdir="classes" includes="*.java" excludes="excluded.java"/&gt;
 * Each of these attributes contains a default:
 *  - srcdir: the base directory of the project
 *  - classdir: srcdir
 *  - includes: "**//*.java" (all java files below the current directory)
 *              this is to be consistent with the default fileset includes.
 *  - excludes: "" (none are excluded)
 * Thus,
 * &lt;paranamer srcdir="." classdir="." includes="*.java"/&gt;
 * &lt;paranamer srcdir="." includes="*.java"/&gt;
 * &lt;paranamer srcdir="."/&gt;
 * &lt;paranamer/&gt;
 * are equivalent.
 *
 * Files can be included in nested file sets via:
 * &lt;paranamer&gt;
 *  &lt;fileset dir="." includes="*.java"/&gt;
 * &lt;/paranamer&gt;
 * In this case, for each file set the defaults are:
 *  - srcdir: dir
 *  - classdir: srcdir
 *  - includes: the default for ant standard file sets
 *  - excludes: the default for ant standard file sets
 * No additional fileset generated from the attribute defaults will be
 * constructed when embedded filesets are used. i.e.
 * &lt;paranamer&gt;
 *  &lt;fileset dir="directory" includes="*.java"/&gt;
 * &lt;/paranamer&gt;
 * will not apply paranamer to the java files in ".".
 *
 * When mixing file sets and attributes, the classdir attribute is treated
 * differently. The classdir attribute will be applied to any files included
 * via srcdir or an embedded file set element. For example:
 * &lt;paranamer classdir="classes"/&gt;
 *  &lt;fileset dir="." includes="*.java"/&gt;
 *  &lt;fileset dir="directory" includes="*.java"/&gt;
 * &lt;/paranamer&gt;
 * will require the classes to modify for the first fileset to reside in
 * the classes directory and those of the second fileset to reside in
 * classes/directory. Note that the default srcdir is not applied when embedded
 * filesets are used instead.
 *
 * @author Timothy Cleaver
 */
public class ParanamerTask extends Task {
    /**
     * The directory that contains the java source from which to extract the
     * parameter names. By default this is the current directory.
     */
    private String srcdir = null;

    /**
     * Boolean that is true when the srcdir was set as an attribute of the
     * target, and false otherwise.
     */
    private boolean srcdirSet = false;

    /**
     * The directory that contains the class files to modify. By default
     * this is the current directory.
     */
    private String classdir = srcdir;

    /**
     * Boolean that is true when the classdir was set as an attribute of the
     * target, and false otherwise.
     */
    private boolean classdirSet = false;

    /**
     * The pattern used to include java files to be processed within the
     * specified source directory. By default this is the set of java files
     * in the current directory and all its sub-directories. This is to
     * match the default matching semantics of the fileset target.
     */
    private String includes = "**/*.java";

    /**
     * Boolean that is true when the includes was set as an attribute of the
     * target, and false otherwise.
     */
    private boolean includesSet = false;

    /**
     * The pattern used to exclude java files from processing. By default this
     * is empty.
     */
    private String excludes = "";

    /**
     * Boolean that is true when the excludes  was set as an attribute of the
     * target, and false otherwise.
     */
    private boolean excludesSet = false;

    /**
     * The collection of nested file sets containing the files to be processed.
     */
    private Collection<FileSet> filesets = new Vector<FileSet>();

    /**
     * Execute the task.
     */
    public void execute() throws BuildException {
        if (srcdir == null) { srcdir = getProject().getBaseDir().getPath(); }
        if (filesets.isEmpty() || srcdirSet || includesSet || excludesSet) {
            FileSet set = new FileSet();
            set.setDir(getProject().resolveFile(srcdir));
            // ensure whitespace is ignored from around the components of the includes
            for (Object o : Arrays.asList(includes.split(","))) {
                set.appendIncludes(new String[]{((String) o).trim()});
            }
            // ensure whitespace is ignored from around the components of the excludes
            for (Object o : Arrays.asList(excludes.split(","))) {
                set.appendExcludes(new String[]{((String) o).trim()});
            }
            filesets.add(set);
        }
        for (Object fileset : filesets) {
            FileSet fs = (FileSet) fileset;
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            String[] includedFiles = ds.getIncludedFiles();
            log("Generating parameter names for"
                    + includedFiles.length
                    + " files in "
                    + ds.getBasedir());
            for (Object o : Arrays.asList(includedFiles)) {
                String file = (String) o;
                JavaProjectBuilder builder = new JavaProjectBuilder();
                try {
                    builder.addSource(new File(ds.getBasedir(), file));
                    // if the classdir is set then we source the classes
                    // relative to classdir. otherwise we source the classes
                    // relative to the base directory of the file set.
                    if (classdirSet) {
                        makeQdoxParanamerGenerator()
                                .processClasses(builder.getClasses(),
                                        classdir);
                    } else {
                        makeQdoxParanamerGenerator()
                                .processClasses(builder.getClasses(),
                                        ds.getBasedir().getPath());
                    }
                } catch (final IOException exception) {
                    throw new BuildException("Error processing: "
                            + file
                            + ". "
                            + exception.getMessage());
                }
            }
        }
    }

    /**
     * Called automatically by ant when an embedded fileset element is present.
     *
     * @param fileset
     *  the fileset specification
     */
    public void addFileset(final FileSet fileset) {
        this.filesets.add(fileset);
    }

    /**
     * Called automatically by ant when the srcdir attribute is present.
     *
     * @param srcdir
     *  the content of the srcdir attribute.
     */
    public void setSrcdir(final String srcdir) {
        this.srcdir = srcdir;
        this.srcdirSet = true;
    }

    /**
     * Called automatically by ant when the includes attribute is present.
     *
     * @param includes
     *  the content of the includes attribute.
     */
    public void setIncludes(final String includes) {
        this.includes = includes;
        this.includesSet = true;
    }

    /**
     * Called automatically by ant when the excludes attribute is present.
     *
     * @param excludes
     *  the content of the excludes attribute.
     */
    public void setExcludes(final String excludes) {
        this.excludes = excludes;
        this.excludesSet = true;
    }

    /**
     * Called automatically by ant when the classdir attribute is present.
     *
     * @param classdir
     *  the content of the classdir attribute.
     */
    public void setClassdir(final String classdir) {
        this.classdir = classdir;
        this.classdirSet = true;
    }

    /**
     * Provide this as a method so that it can be overridden and custom
     * QdoxParanamerGenerators can be returned in place of the default.
     * This is used for testing purposes.
     *
     * @return
     *  the qdox paranamer generated instance to use to generate the
     *  paranamer data.
     */
    protected QdoxParanamerGenerator makeQdoxParanamerGenerator() {
        return new QdoxParanamerGenerator();
    }
}
