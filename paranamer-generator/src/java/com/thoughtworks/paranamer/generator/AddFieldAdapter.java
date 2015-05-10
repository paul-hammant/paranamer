package com.thoughtworks.paranamer.generator;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;


/**
 * Adapted from ASM 3.0 pagg 23-25
 * @author Alessandro Colantoni
 */
public class AddFieldAdapter extends ClassVisitor {
    private int fAcc;
    private String fName;
    private String fDesc;
    private Object fValue; // not present in ASM 3.0
    private boolean isFieldPresent;

    public AddFieldAdapter(ClassVisitor cv, int fAcc, String fName, String fDesc, Object fValue) {
        super(org.objectweb.asm.Opcodes.ASM5, cv);
        this.fAcc = fAcc;
        this.fName = fName;
        this.fDesc = fDesc;
        this.fValue= fValue;
    }

    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (name.equals(fName)) {
            isFieldPresent = true;
        }
        return cv.visitField(access, name, desc, signature, value);
    }

    public void visitEnd() {
        if (!isFieldPresent) {
            FieldVisitor fv = cv.visitField(fAcc, fName, fDesc, null, fValue);
            if (fv != null) {
                fv.visitEnd();
            }
        }
        cv.visitEnd();
    }
}
