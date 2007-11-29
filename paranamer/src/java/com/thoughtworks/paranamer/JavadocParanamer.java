/*
 * Copyright 2007 Paul Hammant
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
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
 * ---
 *
 * Original Source donated to Paranamer project by Sam Halliday, ThinkTank Mathematics Limited
 * A grant of copyright to this source was given to Paul Hammant by Sam, November 2007, who
 * also retains a right to his original version.
 *
 */
package com.thoughtworks.paranamer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Implementation of {@link Paranamer} which can access Javadocs at runtime to extract
 * parameter names of methods. Works with:-
 * <ul>
 * <li>Javadoc in zip file</li>
 * <li>Javadoc in directory</li>
 * <li>Javadoc at remote URL</li>
 * </ul>
 * Future implementations may be able to take multiple sources, but this version must be
 * instantiated with the correct location of the Javadocs for the package you wish to
 * extract the parameter names. Note that if a zip archive contains multiple
 * "package-list" files, the first one will be used to index the packages which may be
 * queried.
 * <p>
 * Note that this does not perform any caching of entries (except what it finds in the
 * package-list file, which is very lightweight)... every lookup will involve a disc hit.
 * If you want to speed up performance, use a {@link CachingParanamer}.
 * <p>
 * Implementation note: the constructors of this implementation let the client know if I/O
 * problems will stop the recovery of parameter names. It might be preferable to suppress
 * exceptions and simply return NO_PARAMETER_NAMES_LIST.
 * <p>
 * TODO: example use code
 * 
 * @author Samuel Halliday, ThinkTank Mathematics Limited
 */
public class JavadocParanamer implements Paranamer {

	private static final String IE =
			"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)";

	/**
	 * Timeout in milliseconds for the URL implementation. Not that Java ever pays any
	 * attention to this value.
	 */
	private static final int TIMEOUT = 5000;

	/** In the case of an archive, this stores the path up to the base of the Javadocs */
	private String base = null;
	private final boolean isArchive;
	private final boolean isDirectory;

	private final boolean isURI;

	/**
	 * Regardless of the implementation, this stores the base location of the remote or
	 * local file or directory.
	 */
	private final URI location;

	/** The packages which are supported by this instance. */
	private final Set packages = new HashSet();

	/**
	 * Construct a Javadoc reading implementation of {@link Paranamer} using a local
	 * directory or zip archive as a source.
	 * 
	 * @param archiveOrDirectory
	 *            either a zip archive of Javadocs or the base directory of Javadocs.
	 * @throws IOException
	 *             if there was an error when reading from either the archive or the
	 *             package-list file.
	 * @throws NullPointerException
	 *             if any parameter is null
	 * @throws IllegalArgumentException
	 *             if the given parameter is neither a directory nor a file. Or if it is a
	 *             file but not a zip archive of Javadocs. Or if it is a directory but not
	 *             the base of a Javadoc directory (must contain a file named
	 *             "package-list").
	 */
	public JavadocParanamer(File archiveOrDirectory) throws IOException {
		if (archiveOrDirectory == null)
			throw new NullPointerException();

		if (!archiveOrDirectory.exists())
			throw new FileNotFoundException(
				archiveOrDirectory.getAbsolutePath());

		isURI = false;
		location = archiveOrDirectory.toURI();

		if (archiveOrDirectory.isDirectory()) {
			// is a directory
			isArchive = false;
			isDirectory = true;
			// check that "package-list" exists
			File dir = archiveOrDirectory;
			File packageList =
					new File(dir.getAbsolutePath() + "/package-list");
			if (!packageList.isFile())
				throw new IllegalArgumentException("No package-list found at "
						+ dir.getAbsolutePath()
						+ ". Not a valid Javadoc directory.");
			// it appear to be a valid Javadoc directory
			FileInputStream input = new FileInputStream(packageList);
			try {
				String packageListString = streamToString(input);
				parsePackageList(packageListString);
			} finally {
				input.close();
			}
		} else if (archiveOrDirectory.isFile()) {
			// is a file
			isArchive = true;
			isDirectory = false;
			File archive = archiveOrDirectory;
			if (!archive.getAbsolutePath().toLowerCase().endsWith(".jar"))
				throw new IllegalArgumentException(archive.getAbsolutePath()
						+ " is not a zip file.");
			// check that a "package-list" exists somewhere in the archive
			ZipFile zip = new ZipFile(archive);
			try {
				// we need to check for a file named "package-list".
				// There may be multiple files in the archive
				// but we cannot use ZipFile.getEntry for suffix names
				// so we have to look through all the entries.
				Enumeration entries = zip.entries();
				// grr... http://javablog.co.uk/2007/11/25/enumeration-and-iterable
				while (entries.hasMoreElements()) {
					ZipEntry entry = (ZipEntry) entries.nextElement();
					String name = entry.getName();
					if (name.endsWith("package-list")) {
						base =
								name.substring(0, name.length()
										- "package-list".length());
						InputStream input = zip.getInputStream(entry);
						try {
							String packageListString = streamToString(input);
							parsePackageList(packageListString);
						} finally {
							input.close();
						}
						// TODO: handle multiple package lists
						return;
					}
				}
				throw new IllegalArgumentException("No package-list found in "
						+ archive.getAbsolutePath()
						+ ". Not a valid Javadoc archive.");
			} finally {
				zip.close();
			}
		} else
			throw new IllegalArgumentException(
				archiveOrDirectory.getAbsolutePath()
						+ " is neither a directory nor a file.");
	}

	/**
	 * @param url
	 * @throws IOException
	 *             if there was a problem connecting to the remote Javadocs
	 * @throws NullPointerException
	 *             if any parameter is null
	 */
	public JavadocParanamer(URL url) throws IOException {
		if (url == null)
			throw new NullPointerException();

		isArchive = false;
		isDirectory = false;
		isURI = true;
		try {
			location = url.toURI();
		} catch (URISyntaxException e) {
			throw new IOException(e.getMessage());
		}

		// check the package-list
		URL packageListURL = new URL(url.toString() + "/package-list");
		InputStream input = urlToInputStream(packageListURL);
		try {
			String packageList = streamToString(input);
			parsePackageList(packageList);
		} finally {
			input.close();
		}
	}

	public int areParameterNamesAvailable(Class clazz, String constructorOrMethodName) {
		if ((clazz == null) || (constructorOrMethodName == null))
			throw new NullPointerException();

		// quick check to see if we support the package
		if (!packages.contains(clazz.getPackage().getName()))
			return NO_PARAMETER_NAMES_FOR_CLASS;

		// due to general problems with this method, we just delegate
		// the first match we find to lookupParameterNames
		AccessibleObject accessible = null;
		if (constructorOrMethodName.equals("<init>"))
			accessible = clazz.getDeclaredConstructors()[0];
		else {
			Method[] methods = clazz.getMethods();
			if (methods == null)
				// actually, this method doesn't exist
				return NO_PARAMETER_NAMES_FOR_CLASS_AND_MEMBER;
			for (int i = 0; i < methods.length; i++) {
				if (methods[i].getName().equals(constructorOrMethodName)) {
					accessible = methods[i];
					break;
				}
			}
		}
		if (accessible == null)
			// actually, this method doesn't exist
			return NO_PARAMETER_NAMES_FOR_CLASS_AND_MEMBER;

		try {
			lookupParameterNames(accessible);
			return PARAMETER_NAMES_FOUND;
		} catch (ParameterNamesNotFoundException e) {
			return NO_PARAMETER_NAMES_FOR_CLASS_AND_MEMBER;
		}
	}

	public String[] lookupParameterNames(AccessibleObject methodOrConstructor) {
		if (methodOrConstructor == null)
			throw new NullPointerException();

		Class klass;
		String name;
		Class[] types;

		if (methodOrConstructor instanceof Constructor) {
			Constructor constructor = (Constructor) methodOrConstructor;
			klass = constructor.getDeclaringClass();
			name = constructor.getName();
			types = constructor.getParameterTypes();
		} else if (methodOrConstructor instanceof Method) {
			Method method = (Method) methodOrConstructor;
			klass = method.getDeclaringClass();
			name = method.getName();
			types = method.getParameterTypes();
		} else
			throw new IllegalArgumentException();

		String[] names;
		try {
			names = getParameterNames(klass, name, types);
			if (names == null)
				throw new ParameterNamesNotFoundException(
					methodOrConstructor.toString()
							+ " parameter names not found in the Javadocs.");
			return names;
		} catch (IOException e) {
			throw new ParameterNamesNotFoundException(
				methodOrConstructor.toString() + " due to an I/O error: "
						+ e.getMessage());
		}
	}

	private String[] getParameterNames(Class klass, String constructorOrMethodName, Class[] types)
			throws IOException {
		// silly request for names of a parameterless method/constructor!
		if ((types != null) && (types.length == 0))
			return new String[0];

		String path = klass.getCanonicalName().replace(".", "/");
		if (isArchive) {
			ZipFile archive = new ZipFile(new File(location));
			ZipEntry entry = archive.getEntry(base + path + ".html");
			if (entry == null)
				return null;
			InputStream input = archive.getInputStream(entry);
			return getParameterNames2(input, constructorOrMethodName, types);
		} else if (isDirectory) {
			File file = new File(location.getPath() + "/" + path + ".html");
			if (!file.isFile())
				return null;
			FileInputStream input = new FileInputStream(file);
			return getParameterNames2(input, constructorOrMethodName, types);
		} else if (isURI) {
			URL url = new URL(location.toString() + "/" + path + ".html");
			InputStream input = urlToInputStream(url);
			return getParameterNames2(input, constructorOrMethodName, types);
		}
		throw new RuntimeException(
			"bug in JavadocParanamer. Should not reach here.");
	}

	/*
	 * Parse the Javadoc String and return the parameter names for the given constructor
	 * or method. Return null if no method/constructor is found. Note that types will
	 * never have length zero... we already deal with that situation higher up in the
	 * chain. Don't forget to close the input!
	 */
	private String[] getParameterNames2(InputStream input,
			String constructorOrMethodName, Class[] types)
			throws IOException {
		String javadoc = streamToString(input);
		input.close();

		// String we're looking for is like
		// 
		// NAME="constructorOrMethodName(obj.ClassName, ...)"...noise...
		// <DT><B>Parameters:</B><DD><CODE>parameter_name_1</CODE>...noise...
		// <DD><CODE>parameter_name_2</CODE>...noise...
		// ...
		// <DD><CODE>parameter_name_N</CODE>...noise...
		// 
		// We cannot rely on the Parameters line existing as it depends on the author
		// having correctly marked-up their code. The NAME element is auto-generated
		// and should be checked for aggressively.
		// 
		// Also note that Javadoc parameter names may differ from the names in the source.
		StringBuilder regex = new StringBuilder();
		regex.append("NAME=\"");
		regex.append(constructorOrMethodName);
		// quotes needed to escape array brackets
		regex.append("\\(\\Q");
		for (int i = 0; i < types.length; i++) {
			if (i != 0)
				regex.append(", ");
			// canonical name deals with arrays
			regex.append(types[i].getCanonicalName());
		}
		regex.append("\\E\\)\"");

		Pattern pattern = Pattern.compile(regex.toString());
		Matcher matcher = pattern.matcher(javadoc);
		if (!matcher.find())
			// not found
			return null;

		// found it. Lookup the parameter names.
		String[] names = new String[types.length];
		// now we're sure we have the right method, find the parameter names!
		String regexParams = "<DD><CODE>([^<]*)</CODE>";
		Pattern patternParams = Pattern.compile(regexParams);
		int start = matcher.end();
		Matcher matcherParams = patternParams.matcher(javadoc);
		matcherParams.region(start, javadoc.length());
		for (int i = 0; i < types.length; i++) {
			boolean find = matcherParams.find();
			assert find;
			names[i] = matcherParams.group(1);
		}
		return names;
	}

	// storing the list of packages that we support is very lightweight
	private void parsePackageList(String packageList) throws IOException {
		StringReader reader = new StringReader(packageList);
		BufferedReader breader = new BufferedReader(reader);
		String line;
		while ((line = breader.readLine()) != null) {
			packages.add(line);
		}
	}

	// read an InputStream into a UTF-8 String
	private String streamToString(InputStream input) throws IOException {
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(input, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// this should never happen
			reader = new InputStreamReader(input);
		}
		BufferedReader breader = new BufferedReader(reader);
		String line;
		StringBuilder builder = new StringBuilder();
		while ((line = breader.readLine()) != null) {
			builder.append(line);
			builder.append("\n");
		}
		return builder.toString();
	}

	private InputStream urlToInputStream(URL url) throws IOException {
		URLConnection conn = url.openConnection();
		// pretend to be IE6
		conn.setRequestProperty("User-Agent", IE);
		conn.setReadTimeout(TIMEOUT);
		// allow both GZip and Deflate (ZLib) encodings
		conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
		conn.connect();
		String encoding = conn.getContentEncoding();
		if ((encoding != null) && encoding.equalsIgnoreCase("gzip"))
			return new GZIPInputStream(conn.getInputStream());
		else if ((encoding != null) && encoding.equalsIgnoreCase("deflate"))
			return new InflaterInputStream(conn.getInputStream(), new Inflater(
				true));
		else
			return conn.getInputStream();
	}

}
