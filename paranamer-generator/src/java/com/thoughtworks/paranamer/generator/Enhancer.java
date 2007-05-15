package com.thoughtworks.paranamer.generator;

import org.objectweb.asm.*;

import java.io.*;

/**
 * A bytecode enhancer which adds a new static variable to some specific class.
 *
 * @author Guilherme Silveira
 * @since upcoming
 */
public class Enhancer implements Opcodes {

    public void enhance(File classFile, String parameterNameData) throws IOException {

        byte[] classBytecode = addExtraStaticField(classFile, parameterNameData);
        FileOutputStream os = new FileOutputStream(classFile);
        os.write(classBytecode);
        os.close();
    }

    private byte[] addExtraStaticField(File classFile, final String parameterNameData) throws IOException {

        InputStream inputStream = new FileInputStream(classFile);
        ClassReader reader = new ClassReader(inputStream);

        ClassWriter writer = new ClassWriter(reader, 0);
        // TODO fix problem with inner classes, two classes in one classFile and so on...
        // TODO doc typo on page 21: recommanded
        ClassAdapter adapter = new ClassAdapter(writer) {

            public void visit(int version, int access, String name, String s1, String s2, String[] strings) {
                super.visit(version, access, name, s1, s2, strings);
                FieldVisitor fv = visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "__PARANAMER_DATA", "Ljava/lang/String;", null, parameterNameData);
                fv.visitEnd();
            }
            
        };

        reader.accept(adapter, 0);
        
        inputStream.close();
        return writer.toByteArray();
    }

}
