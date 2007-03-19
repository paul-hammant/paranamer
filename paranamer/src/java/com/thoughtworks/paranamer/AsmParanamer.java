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
        try {
            Constructor[] constructors = classLoader.loadClass(className).getConstructors();
            for (int i = 0; i < constructors.length; i++) {
                Constructor constructor = constructors[i];
                String parms = lookupParameterNamesForConstructor(constructor);
                if (parms.equals(paramNames)) {
                    return constructor;
                }
            }
            return null;
        } catch (SecurityException e) {
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
	}

	public Method lookupMethod(ClassLoader classLoader, String className,
			String methodName, String paramNames) {
        try {
            Method[] methods = classLoader.loadClass(className).getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method.getName().equals(methodName)) {
                    String parms = lookupParameterNamesForMethod(method);
                    if (parms.equals(paramNames)) {
                        return method;
                    }
                }
            }
            return null;
        } catch (SecurityException e) {
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
	}

	public String[] lookupParameterNames(ClassLoader classLoader,
			String className, String methodName) {
		try {
			Method[] methods = classLoader.loadClass(className).getMethods();
			List names = new ArrayList();
			for (int i = 0; i < methods.length; i++) {
				Method method = methods[i];
				if (method.getName().equals(methodName)) {
                    String s = lookupParameterNamesForMethod(method);
                    if (!s.equals("")) {
                        names.add(s);
                    }
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
        InputStream content = getClassAsStream(method.getDeclaringClass());
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

    public String lookupParameterNamesForConstructor(Constructor ctor) {
        InputStream content = getClassAsStream(ctor.getDeclaringClass());
        try {
            ClassReader creader = new ClassReader(content);
            TypeCollector visitor = new TypeCollector(ctor.getName(), ctor
                    .getParameterTypes());
            creader.accept(visitor, 0);
            return visitor.getParameterNamesForMethod();
        } catch (IOException e) {
            return null;
        }
    }

    public int isParameterNameDataAvailable(ClassLoader classLoader, String className, String ctorOrMethodName) {
        InputStream content = getClassAsStream(classLoader, className);
        try {
            ClassReader creader = new ClassReader(content);
            //TODO - also for constructors
            List methods = getMatchingMethods(classLoader, className, ctorOrMethodName);
            if (methods.size() == 0) {
                return Paranamer.NO_PARAMETER_NAME_DATA_FOR_THAT_CLASS_AND_MEMBER;
            }
            TypeCollector visitor = new TypeCollector(ctorOrMethodName, ((Method) methods.get(0)).getParameterTypes());
            creader.accept(visitor, 0);
            if (visitor.isClassFound()) {
                if (!visitor.isMethodFound()) {
                    return Paranamer.NO_PARAMETER_NAME_DATA_FOR_THAT_CLASS_AND_MEMBER;
                }
            } else {
                return Paranamer.NO_PARAMETER_NAME_DATA_FOR_THAT_CLASS;
            }
            return Paranamer.PARAMETER_NAME_DATA_FOUND;
        } catch (IOException e) {
            return Paranamer.NO_PARAMETER_NAMES_LIST;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private InputStream getClassAsStream(Class clazz) {
        ClassLoader classLoader = clazz.getClassLoader();
        return getClassAsStream(classLoader, clazz.getName());
    }

    private InputStream getClassAsStream(ClassLoader classLoader, String className) {
        String name = '/' + className.replace('.', '/') + ".class";
        // better pre-cache all methods otherwise this content will be loaded
        // multiple times
        InputStream asStream = classLoader.getResourceAsStream(name);
        if (asStream == null) {
            asStream = AsmParanamer.class.getResourceAsStream(name);
        }
        return asStream;
    }

    private List getMatchingMethods(ClassLoader classLoader, String className, String ctorOrMethodName) throws ClassNotFoundException {
        List methodz = new ArrayList();
        Method[] methods = classLoader.loadClass(className).getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equals(ctorOrMethodName)) {
                methodz.add(method);
            }
        }
        return methodz;
    }
}
