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
import java.util.ArrayList;
import java.util.List;

public class ParanamerGeneratorTaskTest extends TestCase {
    private static final File BASE = new File(ParanamerGeneratorTaskTest.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParentFile().getParentFile();

    public void testGenTaskCanFindClassesToProcessWithSimpleSourceRoot() {

        final List classList = new ArrayList();

        ParanamerGeneratorTask paranamer = new ParanamerGeneratorTask() {
            public void log(java.lang.String msg, int msgLevel) {
            }

            protected QdoxParanamerGenerator makeQdoxParanamerGenerator() {
                return new QdoxParanamerGenerator() {
                    public void processClasses(JavaClass[] classes, String outputPath) throws IOException {
                        for (int i = 0; i < classes.length; i++) {
                            JavaClass aClass = classes[i];
                            classList.add(aClass.getFullyQualifiedName());
                        }
                    }
                };
            }
        };
        paranamer.setSourceDirectory(BASE.getAbsolutePath() + File.separator + "src" + File.separator + "test");
        paranamer.setOutputDirectory("nowhere");
        paranamer.execute();
        assertTrue(classList.contains("Unpackaged"));
        assertTrue(classList.contains(A.class.getName()));
        assertTrue(classList.contains(B.class.getName()));
        assertTrue(classList.contains(C.class.getName()));

    }

    public void testGenTaskCanDetermineWhatToEnhance() {
        final boolean[] didIt = new boolean[1];
        ParanamerGeneratorTask paranamer = new ParanamerGeneratorTask() {
            protected QdoxParanamerGenerator makeQdoxParanamerGenerator() {
                return new QdoxParanamerGenerator() {
                    public Enhancer makeEnhancer() {
                        return new Enhancer() {
                            public void enhance(File classFile, String parameterNameData) throws IOException {
                                didIt[0] = true;
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
        paranamer.setSourceDirectory(BASE.getAbsolutePath() + File.separator + "src" + File.separator + "test" + File.separator + "package2");
        paranamer.setOutputDirectory(BASE.getAbsolutePath() + File.separator + "target" + File.separator + "test-classes");
        paranamer.execute();
        assertTrue(didIt[0]);
    }


}
