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

    public void enhance(File file, String content) throws IOException {
        byte[] results = read(file, content);
        FileOutputStream os = new FileOutputStream(file);
        os.write(results);
        os.close();
    }

    private byte[] read(File file, final String extraContent) throws IOException {

        InputStream inputStream = new FileInputStream(file);
        ClassReader reader = new ClassReader(inputStream);

        ClassWriter writer = new ClassWriter(reader, 0);
        // TODO fix problem with inner classes, two classes in one file and so on...
        // TODO doc typo on page 21: recommanded
        ClassAdapter adapter = new ClassAdapter(writer) {

            public void visit(int version, int access, String name, String s1, String s2, String[] strings) {
                super.visit(version, access, name, s1, s2, strings);
                FieldVisitor fv = visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "__PARANAMER_DATA", "Ljava/lang/String;", null, extraContent);
                fv.visitEnd();
            }
            
        };

        reader.accept(adapter, 0);
        
        inputStream.close();
        return writer.toByteArray();
    }

}
