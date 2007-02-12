package com.thoughtworks.paranamer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;

/**
 * An asm implementation of paranamer. It relies on debug information compiled
 * with the "-g" javac option to retrieve parameter names.
 * 
 * @author Guilherme Silveira
 * @since upcoming
 */
public class AsmParanamer implements Paranamer {

	public Constructor lookupConstructor(ClassLoader classLoader,
			String className, String paramNames) {
		return null;
	}

	public Method lookupMethod(ClassLoader classLoader, String className,
			String methodName, String paramNames) {
		return null;
	}

	public String[] lookupParameterNames(ClassLoader classLoader,
			String className, String methodName) {
		try {
			Method[] methods = classLoader.loadClass(className).getMethods();
			List names = new ArrayList();
			for (int i = 0; i < methods.length; i++) {
				Method method = methods[i];
				if (method.getName().equals(methodName)) {
					names.add(lookupParameterNamesForMethod(method));
				}
			}
			return (String[]) names.toArray(new String[names.size()]);
		} catch (SecurityException e) {
			return null;
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	public String lookupParameterNamesForMethod(Method method) {
		// using the type's loader is not working!?
		// ClassLoader loader = method.getDeclaringClass().getClassLoader();
		String name = '/'
				+ method.getDeclaringClass().getName().replace('.', '/')
				+ ".class";
		// better pre-cache all methods otherwise this content will be loaded
		// multiple times
		InputStream content = AsmParanamer.class.getResourceAsStream(name);
		try {
			ClassReader creader = new ClassReader(content);
			TypeCollector visitor = new TypeCollector(method.getName(), method
					.getParameterTypes());
			creader.accept(visitor, 0);
			return visitor.getParameterNamesForMethod();
		} catch (IOException e) {
			return null;
		}
	}

}
