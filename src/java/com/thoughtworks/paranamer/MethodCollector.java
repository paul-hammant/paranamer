package com.thoughtworks.paranamer;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * Objects of this class collects information from a specific method.  
 * @author Guilherme Silveira
 * @since upcoming
 */
public class MethodCollector implements MethodVisitor {

	private final int paramCount;

	private final int ignoreCount;

	private int currentParameter;

	private final StringBuffer result;

	private boolean debugInfoPresent;

	public MethodCollector(MethodVisitor previous, int ignoreCount,
			int paramCount) {
		this.ignoreCount = ignoreCount;
		this.paramCount = paramCount;
		this.result = new StringBuffer();
		this.currentParameter = 0;
		// if there are 0 parameters, there is no need for debug info
		this.debugInfoPresent = paramCount == 0 ? true : false;
	}

	public void visitLocalVariable(String name, String desc, String signature,
			Label start, Label end, int index) {
		if (index >= ignoreCount && index < ignoreCount  + paramCount) {
			if (!name.equals("arg" + currentParameter)) {
				debugInfoPresent = true;
			}
			result.append(',');
			result.append(name);
			currentParameter++;
		}
	}

	public String getResult() {
		return result.length() != 0 ? result.substring(1) : "";
	}

	public boolean isDebugInfoPresent() {
		return debugInfoPresent;
	}

	public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
		return null;
	}

	public AnnotationVisitor visitAnnotationDefault() {
		return null;
	}

	public void visitAttribute(Attribute arg0) {
	}

	public void visitCode() {
	}

	public void visitEnd() {
	}

	public void visitFieldInsn(int arg0, String arg1, String arg2, String arg3) {
	}

	public void visitFrame(int arg0, int arg1, Object[] arg2, int arg3, Object[] arg4) {
	}

	public void visitIincInsn(int arg0, int arg1) {
	}

	public void visitInsn(int arg0) {
	}

	public void visitIntInsn(int arg0, int arg1) {
	}

	public void visitJumpInsn(int arg0, Label arg1) {
	}

	public void visitLabel(Label arg0) {
	}

	public void visitLdcInsn(Object arg0) {
	}

	public void visitLineNumber(int arg0, Label arg1) {
	}

	public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {
	}

	public void visitMaxs(int arg0, int arg1) {
	}

	public void visitMethodInsn(int arg0, String arg1, String arg2, String arg3) {
	}

	public void visitMultiANewArrayInsn(String arg0, int arg1) {
	}

	public AnnotationVisitor visitParameterAnnotation(int arg0, String arg1, boolean arg2) {
		return null;
	}

	public void visitTableSwitchInsn(int arg0, int arg1, Label arg2, Label[] arg3) {
	}

	public void visitTryCatchBlock(Label arg0, Label arg1, Label arg2, String arg3) {
	}

	public void visitTypeInsn(int arg0, String arg1) {
	}

	public void visitVarInsn(int arg0, int arg1) {
	}

}
