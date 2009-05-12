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

package com.thoughtworks.paranamer;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Paranamer allows lookups of methods and constructors by parameter names.
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public interface Paranamer {

    static final String[] EMPTY_NAMES = new String[]{};

    /**
	 * Parameter names are available, but not for that class.
	 */
	int NO_PARAMETER_NAMES_FOR_CLASS = 2;

	/**
	 * Parameter names are available for that class, but not for that constructor or
	 * method.
	 */
	int NO_PARAMETER_NAMES_FOR_CLASS_AND_MEMBER = 3;

	/**
	 * Parameter names are generally not available.
	 */
	int NO_PARAMETER_NAMES_LIST = 1;

	/**
	 * Parameter names are available for that class and constructor/method.
	 */
	int PARAMETER_NAMES_FOUND = 0;

	/**
	 * Determine if the parameter names are available.
	 * <p>
	 * Known issues:
	 * <ul>
	 * <li>If a method that belongs to the superclass is requested, this call may fail
	 * where {@link #lookupParameterNames(AccessibleObject)} will succeed.</li>
	 * <li>If a method or constructor is overloaded, this call may pass where
	 * {@link #lookupParameterNames(AccessibleObject)} will fail.</li>
	 * </ul>
	 * Use of this method is discouraged.
	 * 
	 * @param clazz
	 *            the name of the class to which the method or constructor belongs.
	 * @param constructorOrMethodName
	 *            the base name of the {@link Method} or {@link Constructor}. If a
	 *            request is being made for the constructor, this should be
	 *            <code>"&lt;init&gt;"</code>.
	 * @return An int encoding the parameter names availability.
	 * @throws NullPointerException
	 *             if either parameter is null.
	 * @throws SecurityException
	 *             if reflection is not permitted on clazz
     * @deprecated since 1.5, use the overloaded lookupParameterNames instead
	 */
    public int areParameterNamesAvailable(Class clazz,
			String constructorOrMethodName);

	/**
	 * Lookup the parameter names of a given method.
	 * 
	 * @param methodOrConstructor
	 *            the {@link Method} or {@link Constructor} for which the parameter names
	 *            are looked up.
	 * @return A list of the parameter names.
	 * @throws ParameterNamesNotFoundException
	 *             if no parameter names were found.
	 * @throws NullPointerException
	 *             if the parameter is null.
	 * @throws SecurityException
	 *             if reflection is not permitted on the containing {@link Class} of the
	 *             parameter
	 */
	public String[] lookupParameterNames(AccessibleObject methodOrConstructor);

	/**
	 * Lookup the parameter names of a given method.
	 *
	 * @param methodOrConstructor
	 *            the {@link Method} or {@link Constructor} for which the parameter names
	 *            are looked up.
	 * @return A list of the parameter names.
	 * @throws ParameterNamesNotFoundException
	 *             if no parameter names were found.
	 * @throws NullPointerException
	 *             if the parameter is null.
	 * @throws SecurityException
	 *             if reflection is not permitted on the containing {@link Class} of the
	 *             parameter
	 */
	public String[] lookupParameterNames(AccessibleObject methodOrConstructor, boolean throwExceptionIfMissing);

}
