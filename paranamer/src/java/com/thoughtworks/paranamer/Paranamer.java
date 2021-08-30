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
 *
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

    String[] EMPTY_NAMES = new String[0];

	/**
	 * Lookup the parameter names of a given method.
     *
     * The default implementation calls
     * <code>{@link #lookupParameterNames(AccessibleObject, boolean) lookupParameterNames(methodOrConstructor, true)}</code>.
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
	default String[] lookupParameterNames(AccessibleObject methodOrConstructor) {
        return lookupParameterNames(methodOrConstructor, true);
    }

	/**
	 * Lookup the parameter names of a given method.
	 *
	 * @param methodOrConstructor
	 *            the {@link Method} or {@link Constructor} for which the parameter names
	 *            are looked up.
	 * @param throwExceptionIfMissing whether to throw an exception if no Paranamer data found (versus return empty array).
     * @return A list of the parameter names.
	 * @throws ParameterNamesNotFoundException
	 *             if no parameter names were found.
	 * @throws NullPointerException
	 *             if the parameter is null.
	 * @throws SecurityException
	 *             if reflection is not permitted on the containing {@link Class} of the
	 *             parameter
	 */
	String[] lookupParameterNames(AccessibleObject methodOrConstructor, boolean throwExceptionIfMissing);


}
