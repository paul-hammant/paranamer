package com.thoughtworks.paranamer;

import junit.framework.TestCase;

public class AsmParanamerTest extends TestCase {

	public void testRetrievesParameterNamesFromAMethod() throws SecurityException, NoSuchMethodException {
		AsmParanamer asm = new AsmParanamer();
		String names = asm.lookupParameterNamesForMethod(TestType.class.getMethod("singleString", String.class));
		assertEquals("s", names);
	}

	public void testRetrievesParameterNamesFromAMethodWithoutParameters() throws SecurityException, NoSuchMethodException {
		AsmParanamer asm = new AsmParanamer();
		String names = asm.lookupParameterNamesForMethod(TestType.class.getMethod("noParameters"));
		assertEquals("", names);
	}

	public void testRetrievesParameterNamesFromAMethodWithoutParametersWithLocalVariable() throws SecurityException, NoSuchMethodException {
		AsmParanamer asm = new AsmParanamer();
		String names = asm.lookupParameterNamesForMethod(TestType.class.getMethod("noParametersOneLocalVariable"));
		assertEquals("", names);
	}

	public void testRetrievesParameterNamesFromAStaticMethod() throws SecurityException, NoSuchMethodException {
		AsmParanamer asm = new AsmParanamer();
		String names = asm.lookupParameterNamesForMethod(TestType.class.getMethod("staticWithParameter", new Class[]{int.class}));
		assertEquals("i", names);
	}

	public void testRetrievesParameterNamesFromMethodWithLong() throws SecurityException, NoSuchMethodException {
		AsmParanamer asm = new AsmParanamer();
		String names = asm.lookupParameterNamesForMethod(TestType.class.getMethod("hasLong", new Class[]{long.class}));
		assertEquals("l", names);
	}

	public void testRetrievesParameterNamesFromMethodWithDoubleMixedInTheParameters() throws SecurityException, NoSuchMethodException {
		AsmParanamer asm = new AsmParanamer();
		String names = asm.lookupParameterNamesForMethod(TestType.class.getMethod("mixedParameters", new Class[]{double.class,String.class}));
		assertEquals("d,s", names);
	}

	public void testDoesNotRetrieveParameterNamedArg0() throws SecurityException, NoSuchMethodException {
		AsmParanamer asm = new AsmParanamer();
		String names = asm.lookupParameterNamesForMethod(TestType.class.getMethod("unsupportedParameterNames", new Class[]{String.class}));
		assertNull(names);
	}
	
	public static class TestType {
		
		public void singleString(String s) {
			int k = 3;
			System.out.println("Mycode");
		}
		
		public void noParametersOneLocalVariable() {
			int k = 3;
		}
		
		public static void staticWithParameter(int i) {
			
		}
		
		public void noParameters() {
		}
		
		public void hasLong(long l) {
			
		}
		
		public void mixedParameters(double d, String s) {
			
		}
		
		public void unsupportedParameterNames(String arg0) {
			
		}

	}


}
