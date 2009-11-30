package com.thoughtworks.paranamer.ant;

import com.thoughtworks.paranamer.generator.Enhancer;
import com.thoughtworks.paranamer.generator.QdoxParanamerGenerator;

import com.thoughtworks.qdox.model.JavaClass;

import junit.framework.TestCase;

import package1.A;
import package1.B;
import package2.C;

import java.io.File;
import java.io.IOException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;

import org.apache.tools.ant.types.FileSet;

public class ParanamerTaskTest extends TestCase {
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
    public void testNoAttributesNoFileSets() {
        final Set result = new HashSet();
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
                    public void processClasses(JavaClass[] classes, String outputPath)
                        throws IOException {
                        for (int i = 0; i < classes.length; i++) {
                            JavaClass aClass = classes[i];
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
        final Set expected = new HashSet(Arrays.asList(new Object[] {
                "Unpackaged",
                com.thoughtworks.paranamer.ant.ParanamerTaskTest.class.getName(),
                A.class.getName(),
                B.class.getName(),
                C.class.getName(),
                com.thoughtworks.paranamer.ant.ParanamerGeneratorTaskTest.class.getName()
            }));
        assertTrue("Expected: "
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
    public void testSrcDirBaseDirNoIncludesNoFileSets() {
        final Set result = new HashSet();
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
                    public void processClasses(JavaClass[] classes, String outputPath)
                        throws IOException {
                        for (int i = 0; i < classes.length; i++) {
                            JavaClass aClass = classes[i];
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
        final Set expected = new HashSet(Arrays.asList(new Object[] {
                "Unpackaged",
                A.class.getName(),
                B.class.getName(),
                C.class.getName(),
                ParanamerTaskTest.class.getName(),
                ParanamerGeneratorTaskTest.class.getName()
            }));
        assertTrue("Expected: "
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
    public void testSrcDirPackage1NoIncludesNoFileSets() {
        final Set result = new HashSet();
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
                    public void processClasses(JavaClass[] classes, String outputPath)
                        throws IOException {
                        for (int i = 0; i < classes.length; i++) {
                            JavaClass aClass = classes[i];
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
        final Set expected = new HashSet(Arrays.asList(new Object[] {
                A.class.getName(),
                B.class.getName()
            }));
        assertTrue("Expected: "
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
    public void testNoSrcDirIncludeUnpackagedNoFileSets() {
        final Set result = new HashSet();
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
                    public void processClasses(JavaClass[] classes, String outputPath)
                        throws IOException {
                        for (int i = 0; i < classes.length; i++) {
                            JavaClass aClass = classes[i];
                            result.add(aClass.getFullyQualifiedName());
                        }
                    }
                };
            }
        };
        Paranamer paranamer = new Paranamer();
        paranamer.setIncludes("*.java");
        paranamer.execute();
        final Set expected = new HashSet(Arrays.asList(new Object[] {
                "Unpackaged"
            }));
        assertTrue("Expected: "
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
    public void testNoSrcDirExcludePackage1NoFileSets() {
        final Set result = new HashSet();
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
                    public void processClasses(JavaClass[] classes, String outputPath)
                        throws IOException {
                        for (int i = 0; i < classes.length; i++) {
                            JavaClass aClass = classes[i];
                            result.add(aClass.getFullyQualifiedName());
                        }
                    }
                };
            }
        };
        Paranamer paranamer = new Paranamer();
        paranamer.setExcludes("package1/**");
        paranamer.execute();
        final Set expected = new HashSet(Arrays.asList(new Object[] {
                "Unpackaged",
                C.class.getName(),
                ParanamerTaskTest.class.getName(),
                ParanamerGeneratorTaskTest.class.getName()
            }));
        assertTrue("Expected: "
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
    public void testNoSrcDirAandCFileSet() {
        final Set result = new HashSet();
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
                    public void processClasses(JavaClass[] classes, String outputPath)
                        throws IOException {
                        for (int i = 0; i < classes.length; i++) {
                            JavaClass aClass = classes[i];
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
        final Set expected = new HashSet(Arrays.asList(new Object[] {
                A.class.getName(),
                C.class.getName()
            }));
        assertTrue("Expected: "
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
    public void testNoSrcDirAandCFileSets() {
        final Set result = new HashSet();
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
                    public void processClasses(JavaClass[] classes, String outputPath)
                        throws IOException {
                        for (int i = 0; i < classes.length; i++) {
                            JavaClass aClass = classes[i];
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
        final Set expected = new HashSet(Arrays.asList(new Object[] {
                A.class.getName(),
                C.class.getName()
            }));
        assertTrue("Expected: "
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
    public void testIncludesAandCFileSet() {
        final Set result = new HashSet();
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
                    public void processClasses(JavaClass[] classes, String outputPath)
                        throws IOException {
                        for (int i = 0; i < classes.length; i++) {
                            JavaClass aClass = classes[i];
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
        final Set expected = new HashSet(Arrays.asList(new Object[] {
                A.class.getName(),
                C.class.getName()
            }));
        assertTrue("Expected: "
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
    public void testSrcDirPackage1andCFileSet() {
        final Set result = new HashSet();
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
                    public void processClasses(JavaClass[] classes, String outputPath)
                        throws IOException {
                        for (int i = 0; i < classes.length; i++) {
                            JavaClass aClass = classes[i];
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
        final Set expected = new HashSet(Arrays.asList(new Object[] {
                A.class.getName(),
                B.class.getName(),
                C.class.getName()
            }));
        assertTrue("Expected: "
                    + expected
                    + " given basedir="
                    + paranamer.getProject().getBaseDir()
                    + "\". Found: "
                    + result,
                result.equals(expected));
    }

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
                            public void enhance(File classFile, String parameterNameData)
                                throws IOException {
                                enhanced[0] = true;
                                super.enhance(classFile, parameterNameData);
                                assertTrue(classFile.getAbsolutePath().endsWith("C.class"));
                                assertEquals(
                                        "method1OfC int,int arg1,arg2 \n" +
                                        "method2OfC int arg \n", parameterNameData);
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
        assertTrue(enhanced[0]);
    }
}
