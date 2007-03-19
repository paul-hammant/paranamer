package com.thoughtworks.paranamer;

import java.lang.reflect.Modifier;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * The type collector waits for an specific method in order to start a method
 * collector.
 * 
 * @author Guilherme Silveira
 * @since upcoming
 */
public class TypeCollector implements ClassVisitor {

	private final String methodName;

	private final Class[] parameterTypes;

	private MethodCollector collector;
    private boolean methodFound = false;
    private boolean classFound = false;

    public TypeCollector(String methodName, Class[] parameterTypes) {
		this.methodName = methodName;
		this.parameterTypes = parameterTypes;
		this.collector = null;
	}

	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		// already found the method, skip any processing
        classFound = true;
        if (collector != null) {
			return null;
		}
		// not the same name
		if (!name.equals(methodName)) {
			return null;
		}
        methodFound = true;
        Type[] argumentTypes = Type.getArgumentTypes(desc);
		int longOrDoubleQuantity = 0;
		for (int i = 0; i < argumentTypes.length; i++) {
			Type t = argumentTypes[i];
			if (t.getClassName().equals("long")
					|| t.getClassName().equals("double")) {
				longOrDoubleQuantity++;
			}
		}
		int paramCount = argumentTypes.length;
		// not the same quantity of parameters
		if (paramCount != this.parameterTypes.length) {
			return null;
		}
		for (int i = 0; i < argumentTypes.length; i++) {
			if (!argumentTypes[i].getClassName().equals(
					this.parameterTypes[i].getName())) {
				return null;
			}
		}
		this.collector = new MethodCollector(null,
				(Modifier.isStatic(access) ? 0 : 1), argumentTypes.length
						+ longOrDoubleQuantity);
		return collector;
	}

	public String getParameterNamesForMethod() {
		if (collector == null) {
			return null;
		}
		if (!collector.isDebugInfoPresent()) {
			return null;
		}
		return collector.getResult();
	}

	public void visit(int arg0, int arg1, String arg2, String arg3,
			String arg4, String[] arg5) {
	}

	public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
		return null;
	}

	public void visitAttribute(Attribute arg0) {
	}

	public void visitEnd() {
	}

	public FieldVisitor visitField(int arg0, String arg1, String arg2,
			String arg3, Object arg4) {
		return null;
	}

	public void visitInnerClass(String arg0, String arg1, String arg2, int arg3) {
	}

	public void visitOuterClass(String arg0, String arg1, String arg2) {
	}

	public void visitSource(String arg0, String arg1) {
	}


    public boolean isMethodFound() {
        return methodFound;
    }

    public boolean isClassFound() {
        return classFound;
    }
}
