package com.thoughtworks.paranamer.moreintegtests;

import junit.framework.TestCase;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.AdaptiveParanamer;
import com.thoughtworks.paranamer.Paranamer;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;

import java.lang.reflect.Method;

/**
 * This test has to be run with -g:none for Javac.
 * Thus, use the Maven build for this rather than in IDEA/Ecplise
 */
public class Paranamer0009TestCase extends TestCase {

    public void methodToFind(String name) {
		assert name != null;
	}
                 
	// fails when compiled with {{-g:none}}, works with {{-g}}. Should skip the lookup with {{-g:none}}.
	public void testParanamer() throws Exception {
		BytecodeReadingParanamer paranamer = new BytecodeReadingParanamer();

		Method method = getClass().getMethod("methodToFind", new Class[] { String.class });

        assertEquals (Paranamer.NO_PARAMETER_NAMES_FOR_CLASS_AND_MEMBER, paranamer.areParameterNamesAvailable(
				Paranamer0009TestCase.class, method.getName()));
	}

	// fails when compiled with {{-g:none}}, works with {{-g}}. Should skip the lookup with {{-g:none}}.
	public void testParanamer2() throws Exception {
		CachingParanamer paranamer = new CachingParanamer(new AdaptiveParanamer());

		Method method = getClass().getMethod("methodToFind", new Class[] { String.class });

        assertEquals (Paranamer.NO_PARAMETER_NAMES_FOR_CLASS_AND_MEMBER, paranamer.areParameterNamesAvailable(
				Paranamer0009TestCase.class, method.getName()));
	}

}
