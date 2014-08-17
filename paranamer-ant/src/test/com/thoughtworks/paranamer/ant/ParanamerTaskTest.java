package com.thoughtworks.paranamer.ant;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.types.FileSet;
import org.junit.Assert;
import org.junit.Test;

import package1.A;
import package1.B;
import package2.C;

import com.thoughtworks.paranamer.generator.Enhancer;
import com.thoughtworks.paranamer.generator.QdoxParanamerGenerator;
import com.thoughtworks.qdox.model.JavaClass;

public class ParanamerTaskTest {
    private static final File BASE = new File(ParanamerTaskTest.class
            .getProtectionDomain()
            .getCodeSource()
            .getLocation()
            .getFile())
        .getParentFile()
        .getParentFile();

    private final Project makeProject() {
        Project project = new Project();
        project.init();
        project.setBasedir(BASE
                + File.separator
                + "src"
                + File.separator
                + "test");
        return project;
    }

    /**
     * Test that the defaults behave as expected when no attributes and no
     * file set has been specifie for the task. By default, the paranamer
     * task will generate name data for all java files under the base directory
     * of the project, and expect the class files to be in this same directory.
     */
    @Test
    public void testNoAttributesNoFileSets() {
        final Set<String> result = new HashSet<String>();
        class Paranamer extends ParanamerTask {
            public Paranamer() {
                project = makeProject();
                taskType = "paranamer";
                taskName = "paranamer";
                target = new Target();
            }

            public void log(java.lang.String msg, int msgLevel) {
            }

            protected QdoxParanamerGenerator makeQdoxParanamerGenerator() {
                return new QdoxParanamerGenerator() {
                	@Override
                    public void processClasses(Collection<JavaClass> classes, String outputPath)
                        throws IOException {
                        for (JavaClass aClass : classes) {
                            result.add(aClass.getFullyQualifiedName());
                        }
                    }
                };
            }
        };
        Paranamer paranamer = new Paranamer();
        paranamer.execute();
        // when no attributes are set, paranamer will attempt to generate
        // parameter names for all java files under the project base directory
        final Set<String> expected = new HashSet<String>(Arrays.asList("Unpackaged",
                ParanamerTaskTest.class.getName(),
                A.class.getName(),
                B.class.getName(),
                C.class.getName(),
                ParanamerGeneratorTaskTest.class.getName()));
        Assert.assertTrue("Expected: "
                + expected
                + " given basedir="
                + paranamer.getProject().getBaseDir()
                + "\". Found: "
                + result,
                result.equals(expected));
    }

    /**
     * Test that a specification of the srcdir will result in all java files
     * under srcdir being included for parameter name generation.
     */
    @Test
    public void testSrcDirBaseDirNoIncludesNoFileSets() {
        final Set<String> result = new HashSet<String>();
        class Paranamer extends ParanamerTask {
            public Paranamer() {
                project = makeProject();
                taskType = "paranamer";
                taskName = "paranamer";
                target = new Target();
            }

            public void log(java.lang.String msg, int msgLevel) {
            }

            protected QdoxParanamerGenerator makeQdoxParanamerGenerator() {
                return new QdoxParanamerGenerator() {
                	@Override
                    public void processClasses(Collection<JavaClass> classes, String outputPath)
                        throws IOException {
                        for (JavaClass aClass : classes) {
                            result.add(aClass.getFullyQualifiedName());
                        }
                    }
                };
            }
        };
        Paranamer paranamer = new Paranamer();
        paranamer.setSrcdir(BASE
                        + File.separator
                        + "src"
                        + File.separator
                        + "test");
        paranamer.execute();
        final Set<String> expected = new HashSet<String>(Arrays.asList("Unpackaged",
                A.class.getName(),
                B.class.getName(),
                C.class.getName(),
                ParanamerTaskTest.class.getName(),
                ParanamerGeneratorTaskTest.class.getName()));
        Assert.assertTrue("Expected: "
                + expected
                + " given basedir="
                + paranamer.getProject().getBaseDir()
                + "\". Found: "
                + result,
                result.equals(expected));
    }

    /**
     * Test that a specification of the srcdir will result in all java files
     * under srcdir being included for parameter name generation.
     */
    @Test
    public void testSrcDirPackage1NoIncludesNoFileSets() {
        final Set<String> result = new HashSet<String>();
        class Paranamer extends ParanamerTask {
            public Paranamer() {
                project = makeProject();
                taskType = "paranamer";
                taskName = "paranamer";
                target = new Target();
            }

            public void log(java.lang.String msg, int msgLevel) {
            }

            protected QdoxParanamerGenerator makeQdoxParanamerGenerator() {
                return new QdoxParanamerGenerator() {
                	@Override
                    public void processClasses(Collection<JavaClass> classes, String outputPath)
                        throws IOException {
                        for (JavaClass aClass : classes) {
                            result.add(aClass.getFullyQualifiedName());
                        }
                    }
                };
            }
        };
        Paranamer paranamer = new Paranamer();
        paranamer.setSrcdir(BASE
                        + File.separator
                        + "src"
                        + File.separator
                        + "test"
                        + File.separator
                        + "package1");
        paranamer.execute();
        final Set<String> expected = new HashSet<String>(Arrays.asList(A.class.getName(),
                B.class.getName()));
        Assert.assertTrue("Expected: "
                + expected
                + " given basedir="
                + paranamer.getProject().getBaseDir()
                + "\". Found: "
                + result,
                result.equals(expected));
    }

    /**
     * Test that a specification of the includes without a srcdir declaration
     * will use the default srcdir value and include only those files that
     * satisfy the include pattern.
     */
    @Test
    public void testNoSrcDirIncludeUnpackagedNoFileSets() {
        final Set<String> result = new HashSet<String>();
        class Paranamer extends ParanamerTask {
            public Paranamer() {
                project = makeProject();
                taskType = "paranamer";
                taskName = "paranamer";
                target = new Target();
            }

            public void log(java.lang.String msg, int msgLevel) {
            }

            protected QdoxParanamerGenerator makeQdoxParanamerGenerator() {
                return new QdoxParanamerGenerator() {
                	@Override
                    public void processClasses(Collection<JavaClass> classes, String outputPath)
                        throws IOException {
                        for (JavaClass aClass : classes) {
                            result.add(aClass.getFullyQualifiedName());
                        }
                    }
                };
            }
        };
        Paranamer paranamer = new Paranamer();
        paranamer.setIncludes("*.java");
        paranamer.execute();
        final Set<String> expected = new HashSet<String>(Arrays.asList("Unpackaged"));
        Assert.assertTrue("Expected: "
                + expected
                + " given basedir="
                + paranamer.getProject().getBaseDir()
                + "\". Found: "
                + result,
                result.equals(expected));
    }

    /**
     * Test that a specification of the excludes without a srcdir declaration
     * and include declaration will use the default srcdir value and include
     * only those files that satisfy the default include pattern and the input
     * exclude pattern.
     */
    @Test
    public void testNoSrcDirExcludePackage1NoFileSets() {
        final Set<String> result = new HashSet<String>();
        class Paranamer extends ParanamerTask {
            public Paranamer() {
                project = makeProject();
                taskType = "paranamer";
                taskName = "paranamer";
                target = new Target();
            }

            public void log(java.lang.String msg, int msgLevel) {
            }

            protected QdoxParanamerGenerator makeQdoxParanamerGenerator() {
                return new QdoxParanamerGenerator() {
                	@Override
                    public void processClasses(Collection<JavaClass> classes, String outputPath)
                        throws IOException {
                        for (JavaClass aClass : classes) {
                            result.add(aClass.getFullyQualifiedName());
                        }
                    }
                };
            }
        };
        Paranamer paranamer = new Paranamer();
        paranamer.setExcludes("package1/**");
        paranamer.execute();
        final Set<String> expected = new HashSet<String>(Arrays.asList("Unpackaged",
                C.class.getName(),
                ParanamerTaskTest.class.getName(),
                ParanamerGeneratorTaskTest.class.getName()));
        Assert.assertTrue("Expected: "
                + expected
                + " given basedir="
                + paranamer.getProject().getBaseDir()
                + "\". Found: "
                + result,
                result.equals(expected));
    }

    /**
     * Test that an included fileset does not engage the default srcdir
     * and include directives. Ensure only the files required by the embedded
     * fileset are to be processed.
     */
    @Test
    public void testNoSrcDirAandCFileSet() {
        final Set<String> result = new HashSet<String>();
        class Paranamer extends ParanamerTask {
            public Paranamer() {
                project = makeProject();
                taskType = "paranamer";
                taskName = "paranamer";
                target = new Target();
            }

            public void log(java.lang.String msg, int msgLevel) {
            }

            protected QdoxParanamerGenerator makeQdoxParanamerGenerator() {
                return new QdoxParanamerGenerator() {
                	@Override
                    public void processClasses(Collection<JavaClass> classes, String outputPath)
                        throws IOException {
                        for (JavaClass aClass : classes) {
                            result.add(aClass.getFullyQualifiedName());
                        }
                    }
                };
            }
        };
        Paranamer paranamer = new Paranamer();
        FileSet fileset = new FileSet();
        fileset.setDir(paranamer.getProject().getBaseDir());
        fileset.appendIncludes(new String[] { "**/A.java", "**/C.java" });
        paranamer.addFileset(fileset);
        paranamer.execute();
        final Set<String> expected = new HashSet<String>(Arrays.asList(A.class.getName(),
                C.class.getName()));
        Assert.assertTrue("Expected: "
                + expected
                + " given basedir="
                + paranamer.getProject().getBaseDir()
                + "\". Found: "
                + result,
                result.equals(expected));
    }

    /**
     * Test that multiple embedded filesets are all processed and the
     * defaults for srcdir and includes are ignored.
     */
    @Test
    public void testNoSrcDirAandCFileSets() {
        final Set<String> result = new HashSet<String>();
        class Paranamer extends ParanamerTask {
            public Paranamer() {
                project = makeProject();
                taskType = "paranamer";
                taskName = "paranamer";
                target = new Target();
            }

            public void log(java.lang.String msg, int msgLevel) {
            }

            protected QdoxParanamerGenerator makeQdoxParanamerGenerator() {
                return new QdoxParanamerGenerator() {
                	@Override
                    public void processClasses(Collection<JavaClass> classes, String outputPath)
                        throws IOException {
                        for (JavaClass aClass : classes) {
                            result.add(aClass.getFullyQualifiedName());
                        }
                    }
                };
            }
        };
        Paranamer paranamer = new Paranamer();
        FileSet a = new FileSet();
        a.setDir(paranamer.getProject().getBaseDir());
        a.appendIncludes(new String[] { "**/A.java" });
        paranamer.addFileset(a);
        FileSet c = new FileSet();
        c.setDir(paranamer.getProject().getBaseDir());
        c.appendIncludes(new String[] { "**/C.java" });
        paranamer.addFileset(c);
        paranamer.execute();
        final Set<String> expected = new HashSet<String>(Arrays.asList(A.class.getName(),
                C.class.getName()));
        Assert.assertTrue("Expected: "
                + expected
                + " given basedir="
                + paranamer.getProject().getBaseDir()
                + "\". Found: "
                + result,
                result.equals(expected));
    }

    /**
     * Test that a specified includes attribute and an embedded fileset are both
     * processed with the includes attribute processed relative to basedir.
     */
    @Test
    public void testIncludesAandCFileSet() {
        final Set<String> result = new HashSet<String>();
        class Paranamer extends ParanamerTask {
            public Paranamer() {
                project = makeProject();
                taskType = "paranamer";
                taskName = "paranamer";
                target = new Target();
            }

            public void log(java.lang.String msg, int msgLevel) {
            }

            protected QdoxParanamerGenerator makeQdoxParanamerGenerator() {
                return new QdoxParanamerGenerator() {
                	@Override
                    public void processClasses(Collection<JavaClass> classes, String outputPath)
                        throws IOException {
                        for (JavaClass aClass : classes) {
                            result.add(aClass.getFullyQualifiedName());
                        }
                    }
                };
            }
        };
        Paranamer paranamer = new Paranamer();
        paranamer.setIncludes("**/A.java");
        FileSet fileset = new FileSet();
        fileset.setDir(paranamer.getProject().getBaseDir());
        fileset.appendIncludes(new String[] { "**/C.java" });
        paranamer.addFileset(fileset);
        paranamer.execute();
        final Set<String> expected = new HashSet<String>(Arrays.asList(A.class.getName(),
                C.class.getName()));
        Assert.assertTrue("Expected: "
                + expected
                + " given basedir="
                + paranamer.getProject().getBaseDir()
                + "\". Found: "
                + result,
                result.equals(expected));
    }

    /**
     * Test that a specified srcdir attribute and an embedded fileset are both
     * processed.
     */
    @Test
    public void testSrcDirPackage1andCFileSet() {
        final Set<String> result = new HashSet<String>();
        class Paranamer extends ParanamerTask {
            public Paranamer() {
                project = makeProject();
                taskType = "paranamer";
                taskName = "paranamer";
                target = new Target();
            }

            public void log(java.lang.String msg, int msgLevel) {
            }

            protected QdoxParanamerGenerator makeQdoxParanamerGenerator() {
                return new QdoxParanamerGenerator() {
                	@Override
                    public void processClasses(Collection<JavaClass> classes, String outputPath)
                        throws IOException {
                        for (JavaClass aClass : classes) {
                            result.add(aClass.getFullyQualifiedName());
                        }
                    }
                };
            }
        };
        Paranamer paranamer = new Paranamer();
        paranamer.setSrcdir("package1");
        FileSet fileset = new FileSet();
        fileset.setDir(paranamer.getProject().getBaseDir());
        fileset.appendIncludes(new String[] { "**/C.java" });
        paranamer.addFileset(fileset);
        paranamer.execute();
        final Set<String> expected = new HashSet<String>(Arrays.asList(A.class.getName(),
                B.class.getName(),
                C.class.getName()));
        Assert.assertTrue("Expected: "
                + expected
                + " given basedir="
                + paranamer.getProject().getBaseDir()
                + "\". Found: "
                + result,
                result.equals(expected));
    }

    @Test
    public void testEnhance() {
        // need to use a single element array (or other wrapper object such as
        // a collection) so that the value can change but the variable itself
        // can be final. this is required so the value of the boolean can be set
        // in the inner class definition and the result available in the outer
        // scope.
        final boolean[] enhanced = new boolean[] { false };
        class Paranamer extends ParanamerTask {
            public Paranamer() {
                project = makeProject();
                taskType = "paranamer";
                taskName = "paranamer";
                target = new Target();
            }

            protected QdoxParanamerGenerator makeQdoxParanamerGenerator() {
                return new QdoxParanamerGenerator() {
                    public Enhancer makeEnhancer() {
                        return new Enhancer() {
                        	@Override
                            public void enhance(File classFile, CharSequence parameterNameData)
                                throws IOException {
                                enhanced[0] = true;
                                super.enhance(classFile, parameterNameData);
                                Assert.assertTrue(classFile.getAbsolutePath().endsWith("C.class"));
                                Assert.assertEquals(
                                        "method1OfC int,int arg1,arg2 \n" +
                                                "method2OfC int arg \n", parameterNameData.toString());
                            }
                        };
                    }
                };
            }
        };
        Paranamer paranamer = new Paranamer();
        paranamer.setSrcdir(BASE.getAbsolutePath()
                + File.separator
                + "src"
                + File.separator
                + "test"
                + File.separator
                + "package2");
        paranamer.setClassdir(BASE.getAbsolutePath()
                + File.separator
                + "target"
                + File.separator
                + "test-classes");
        paranamer.execute();
        Assert.assertTrue(enhanced[0]);
    }
}
