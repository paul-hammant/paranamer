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
