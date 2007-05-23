package com.thoughtworks.paranamer.asm;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.AccessibleObject;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;

import com.thoughtworks.paranamer.Paranamer;

/**
 * An ASM-based implementation of Paranamer. It relies on debug information compiled
 * with the "-g" javac option to retrieve parameter names.
 * 
 * @author Guilherme Silveira
 */
public class AsmParanamer implements Paranamer {

    public String[] lookupParameterNames(AccessibleObject methodOrCtor) {

        Class[] types = null;
        Class declaringClass = null;
        String name = null;
        if (methodOrCtor instanceof Method) {
            Method method = (Method) methodOrCtor;
            types = method.getParameterTypes();
            name = method.getName();
            declaringClass = method.getDeclaringClass();
        } else {
            Constructor constructor = (Constructor) methodOrCtor;
            types = constructor.getParameterTypes();
            declaringClass = constructor.getDeclaringClass();
            name = "<init>";
        }

        InputStream content = getClassAsStream(declaringClass);
		try {
			ClassReader reader = new ClassReader(content);
			TypeCollector visitor = new TypeCollector(name, types);
			reader.accept(visitor, 0);
			return visitor.getParameterNamesForMethod();
		} catch (IOException e) {
			return null;
		}
    }

    public int areParameterNamesAvailable(Class clazz, String constructorOrMethodName) {
        InputStream content = getClassAsStream(clazz.getClassLoader(), clazz.getName());
        try {
            ClassReader reader = new ClassReader(content);
            //TODO - also for constructors
            List methods = getMatchingMethods(clazz.getClassLoader(), clazz.getName(), constructorOrMethodName);
            if (methods.size() == 0) {
                return Paranamer.NO_PARAMETER_NAMES_FOR_CLASS_AND_MEMBER;
            }
            TypeCollector visitor = new TypeCollector(constructorOrMethodName, ((Method) methods.get(0)).getParameterTypes());
            reader.accept(visitor, 0);
            if (visitor.isClassFound()) {
                if (!visitor.isMethodFound()) {
                    return Paranamer.NO_PARAMETER_NAMES_FOR_CLASS_AND_MEMBER;
                }
            } else {
                return Paranamer.NO_PARAMETER_NAMES_FOR_CLASS;
            }
            return Paranamer.PARAMETER_NAMES_FOUND;
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

    private List getMatchingMethods(ClassLoader classLoader, String className, String name) throws ClassNotFoundException {
        List list = new ArrayList();
        Method[] methods = classLoader.loadClass(className).getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equals(name)) {
                list.add(method);
            }
        }
        return list;
    }
}
