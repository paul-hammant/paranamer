package com.thoughtworks.paranamer.ant;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import package1.A;
import package1.B;
import package2.C;

import com.thoughtworks.paranamer.generator.Enhancer;
import com.thoughtworks.paranamer.generator.QdoxParanamerGenerator;
import com.thoughtworks.qdox.model.JavaClass;

public class ParanamerGeneratorTaskTest {
    private static final File BASE = new File(ParanamerGeneratorTaskTest.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParentFile().getParentFile();

    @Test
    public void testGenTaskCanFindClassesToProcessWithSimpleSourceRoot() {

        final Set<String> result = new HashSet<String>();

        ParanamerGeneratorTask paranamer = new ParanamerGeneratorTask() {
            public void log(java.lang.String msg, int msgLevel) {
            }

            protected QdoxParanamerGenerator makeQdoxParanamerGenerator() {
                return new QdoxParanamerGenerator() {
                	@Override
                    public void processClasses(Collection<JavaClass> classes, String outputPath) throws IOException {
                        for (JavaClass aClass : classes) {
                            result.add(aClass.getFullyQualifiedName());
                        }
                    }
                };
            }
        };
        paranamer.setSourceDirectory(BASE.getAbsolutePath() + File.separator + "src" + File.separator + "test");
        paranamer.setOutputDirectory("nowhere");
        paranamer.execute();
        final Set<String> expected = new HashSet<String>(Arrays.asList("Unpackaged",
                A.class.getName(),
                B.class.getName(),
                C.class.getName(),
                ParanamerGeneratorTaskTest.class.getName(),
                ParanamerTaskTest.class.getName()));
        Assert.assertTrue("Expected: "
                + expected
                + ". Found: "
                + result,
                result.equals(expected));
    }

    @Test
    public void testGenTaskCanDetermineWhatToEnhance() {
        final boolean[] didIt = new boolean[1];
        ParanamerGeneratorTask paranamer = new ParanamerGeneratorTask() {
            protected QdoxParanamerGenerator makeQdoxParanamerGenerator() {
                return new QdoxParanamerGenerator() {
                    public Enhancer makeEnhancer() {
                        return new Enhancer() {
                        	@Override
                            public void enhance(File classFile, CharSequence parameterNameData) throws IOException {
                                didIt[0] = true;
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
        paranamer.setSourceDirectory(BASE.getAbsolutePath() + File.separator + "src" + File.separator + "test" + File.separator + "package2");
        paranamer.setOutputDirectory(BASE.getAbsolutePath() + File.separator + "target" + File.separator + "test-classes");
        paranamer.execute();
        Assert.assertTrue(didIt[0]);
    }


}
