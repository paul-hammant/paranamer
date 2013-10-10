# [![ParaNamer](http://paulhammant.com/images/ParaNamer.jpg)](/)

## Method Parameter Name Access for Java*

* versions PRIOR TO Java <del>7.0</del> 8.0

# What is it?

It is a library that allows the parameter names of non-private methods and constructors to be accessed at runtime. Normally this information is dropped by the compiler. In effect, methods like <code>doSometing(mypkg.Person **toMe**)</code>
currently look like <code>doSomething(mypackage.Person **???**)</code> to people using Java's reflection to inspect methods. 

To date parameter name access has not been very useful to Java application developers, but with the advent of advanced scripting languages and web action frameworks for the JVM it is of increasing importance to be able to leverage a method's parameter names. Scripting languages like [Groovy](http://groovy.codehaus.org/) &amp; [JRuby](http://jruby.codehaus.org/), web action frameworks like [Waffle](http://waffle.codehaus.org) and [VRaptor](# "http://www.vraptor.org/") (that verge on the transparent) and the compelling [Grails](http://grails.codehaus.org/). SOAP and REST designs could also benefit.

ParaNamer allows you to generate and use parameter name info for versions of Java prior to JDK 5.0 and above. Parameter name access was scheduled for JDK 6.0, but was cancelled at a late stage as the spec-lead suggested the development team ran out of time to implement it. It is sadly not shipping in JDK 7.0 either.  Sun also had misgivings about the appropriateness of the this change to Java. It was felt that applications could end up depending on parameter names, and that they essentially became part of constructor/method signatures and could never be changed if you wanted to be backwards compatible.  The view of the authors of Paranamer is that you should be aware that parameter names may change between releases, and code to not depend on them.

Paranamer is Open Source, and licensed as BSD. It is compatible with commercial/proprietary, GPL, Apache use.

# Accessing Parameter Name data

There is a method called <code>lookupParameterNames</code> that returns an array of strings for a method or constructor.

```java
// MySomethingOrOther.java**
Method method = Foo.class.getMethod(...);

Paranamer paranamer = new CachingParanamer();

String[] parameterNames = paranamer.lookupParameterNames(method) // throws ParameterNamesNotFoundException if not found

// or:

parameterNames = paranamer.lookupParameterNames(method, false) // will return null if not found
```

ParaNamer does not have any runtime jar dependencies while looking up parameter info previously generated that's been zipped into a jar.

## DefaultParanamer

DefaultParanamer tries to read parameter name data from an extra public static field on the class. This field need to be added after compilation of the class, and before you put the resulting classes in a jar. 

The static field essentially looks like the following. You really do not need to know this unless your going to make something compatible with Paranamer:

```java
private static final String __PARANAMER_DATA = "v1.0 \n"
      + "&lt;init&gt; com.example.PeopleService peopleService \n"
      + "setName java.lang.String,java.lang.String givenName,familyName \n";
      + "setDateOfBirth int,int,int day,month,year \n";</pre></div>
```

Clearly the method's source needs to be analysed and lines added per method to that __PARANAMER_DATA field. See below.

## BytecodeReadingParanamer

If generating meta data for parameter names at compile time is not for you, try class <code>BytecodeReadingParanamer</code> as a runtime only solution. This uses a cut down forked and cut-down version of ASM to extract debug information from a class at runtime. As it happens this is the fallback implementation for <code>CachingParanamer</code> when <code>DefaultParanamer</code> reports that there is no meta data for a class.

## AdaptiveParanamer

Give it a list of Paranamer implementations to try in turn for parameter names. In its default constructor it has <code>BytecodeReadingParanamer</code> and <code>DefaultParanamer</code> in that order.

## JavadocParanamer

Pulls its parameter names from a Javadoc zip/jar (named in the constructor). Courtesy of Sam Halliday

## PositionalParanamer

Numbers it's parameter names arg0, arg1, arg2 (etc), intended as a fall-back if you need it. From Stefan Fleiter.

## AnnotationParanamer

AnnotationParanamer uses the @Named annotation from JSR 330 and extracts names pertinent to parameters from that.

```java
public static class Something {
    public void doSomething(@Named("usedName") String ignoredName) {
    }
}
```

AnnotationParanamer takes a delegate paranamer instance as an optional constructor arg.  This will allow constructors and methods to only partly leverage @Named, with other parameters having non-annotated parameter names (the via say DefaulParanamer or <code>BytecodeReadingParanamer</code>).

If you have an alternate annotation to @Named, then you can specify that in a subclass of AnnotationParanamer that overrides two methods isNamed and getNamedValue.  Your overridden methods should do the equivalent of 'return ann instanceof Named;' and 'return ((Named) ann).value();' respectively.

If you are using @Named from JSR 330, you will need it in your classpath of course.  In Maven terms, Paranamer is built with the 'javax.atinject' module as an optional dependency.

## CachingParanamer

CachingParanamer stores the results of each parameter name lookup, so that second and subsequent invocations will be far quicker.

## AdaptiveParanamer

AdaptiveParanamer is designed for using a series of Paranamer implementations together. The first supplied is asked if it can supply parameter name data for a constructor/method.  If it cannot, then the next one is asked and so on.  The default constructor for this uses DefaultParanamer with <code>ByteCodeReadingParanamer</code> as its contingency.

# Creating Parameter Name data and modifying compiled class files

## Optional Generation of Parameter Name data with Ant

This for <code>DefaultParanamer</code> usage of course, as <code>BytecodeReadingParanamer</code> does not need it.

You need to download:

*   [latest paranamer jar](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22paranamer%22)
*   [latest paranamer-generator jar](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22paranamer-generator%22)
*   [latest paranamer-ant jar](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22paranamer-ant%22)
*   [latest qdox jar](http://repo1.maven.org/maven2/qdox/qdox/) (1.8 or above)

... and declare in your Ant script the following after &lt;javac/&gt; (remember to add to the taskdef classpath all the above jars):

```xml
&lt;taskdef name="paranamer" classname="com.thoughtworks.paranamer.ant.ParanamerGeneratorTask"/&gt;
  &lt;paranamer sourceDirectory="src/java" outputDirectory="target/classes"/&gt;
```

Classes are changed to have an extra static String member variable that contains the member functions and their parameter names. Be sure to zip them up in to you final jar.

## Optional Generation of Parameter Name data with Maven 2

For Maven, configuration is simpler. Just add this to the build/plugins section of your pom.xml:

```xml
&lt;plugin&gt;
&nbsp;&nbsp;&nbsp; &lt;groupId&gt;com.thoughtworks.paranamer&lt;/groupId&gt;
&nbsp;&nbsp;&nbsp; &lt;artifactId&gt;paranamer-maven-plugin&lt;/artifactId&gt;
&nbsp;&nbsp; &nbsp;&lt;executions&gt;
&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &lt;execution&gt;
&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &lt;id&gt;run&lt;/id&gt;  &lt;!-- id is optional --&gt;
&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &lt;configuration&gt;
&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &lt;sourceDirectory&gt;${project.build.sourceDirectory}&lt;/sourceDirectory&gt;
&nbsp; &nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &lt;outputDirectory&gt;${project.build.outputDirectory}&lt;/outputDirectory&gt;
&nbsp;&nbsp;&nbsp;  &nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &lt;/configuration&gt;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;
 &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &lt;goals&gt;
&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &lt;goal&gt;generate&lt;/goal&gt;
&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &lt;/goals&gt;
&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &lt;/execution&gt;
&nbsp;&nbsp;&nbsp; &lt;/executions&gt;
&nbsp;&nbsp;&nbsp; &lt;dependencies&gt;
        &lt;!-- if some of parameter names you need to retain are held in pre-existing jars, they need to be added to the classpath --&gt;
        &lt;dependency&gt;
            &lt;groupId&gt;some-artifact-group&lt;/groupId&gt;
            &lt;artifactId&gt;some-artifact&lt;/artifactId&gt;
            &lt;version&gt;1.0&lt;/version&gt;
        &lt;/dependency&gt;
    &lt;/dependencies&gt;
&lt;/plugin&gt;
```

The classes in the ultimate jar file will automatically be made with parameter name data.

## Using Paranamer in your application without depending on Paranamer's jar

There are already too many jar's for day to day Java development
right?&nbsp; Simply consume the runtime paranamer jar into your project's jar using the Maven2 'shade' plugin.

```xml
<!-- Put in your POM.xml file -->
&lt;build&gt;
    &lt;plugins&gt;
      &lt;plugin&gt;
        &lt;groupId&gt;org.apache.maven.plugins&lt;/groupId&gt;
        &lt;artifactId&gt;maven-shade-plugin&lt;/artifactId&gt;
        &lt;executions&gt;
          &lt;execution&gt;
            &lt;phase&gt;package&lt;/phase&gt;
            &lt;goals&gt;
              &lt;goal&gt;shade&lt;/goal&gt;
            &lt;/goals&gt;
            &lt;configuration&gt;
              &lt;shadedArtifactId&gt;artifact-shaded&lt;/shadedArtifactId&gt;
              &lt;relocations&gt;
                &lt;relocation&gt;
                  &lt;pattern&gt;com.thoughtworks.paranamer&lt;/pattern&gt;
                  &lt;shadedPattern&gt;com.yourcompany.yourapp.paranamer&lt;/shadedPattern&gt;
                &lt;/relocation&gt;
              &lt;/relocations&gt;
            &lt;/configuration&gt;
          &lt;/execution&gt;
        &lt;/executions&gt;
      &lt;/plugin&gt;
    &lt;/plugins&gt;
  &lt;/build&gt;
```

# How Paranamer works

## What if a parameter names changes?

The general answer to this question is that you should not ship something to third parties where they are going to hard-core your parameter names into their application. For you own in-house stuff, accessing parameter names is harmless. You should be able to ripple though the source code of even large applications, and change say &quot;badSpeeldWord&quot; to &quot;badlySpelledWorld&quot; if you need to.

In a later version we may store previous parameter names too. It won't be magic, you'll have to code the previous parameter names in your source with a doclet tag. The lookup mechanism will change too. It will likely leverage a doclet tag.

# Other Modules

* paranamer-ant - Ant tasks
* paranamer-maven-plugin - a Maven plugin (as shown above)

# Paranamer's Future

The need for Paranamer will go away when [JEP-118](http://openjdk.java.net/jeps/118) lands as part of Java8.

# Projects using it

[Mandragora](http://mandragora.sourceforge.net)

[Oval - Object Validation Framework](http://oval.sourceforge.net/),

[PicoContainer - Dependency Injection Container](http://picocontainer.org/),

[PicoContainer Web-Remoting](http://picocontainer.org/web/remoting),

[VRaptor - Web Framework](http://vraptor.org/),

[Waffle - Web Framework](http://waffle.codehaus.org/)

[JBehave - BDD tests for Java](http://jbehave.codehaus.org/)

[CDI Interceptors](https://github.com/sfleiter/cdi-interceptors)

# Releases

Release 2.6 - Oct 9 2013 (JDK 5.0 and above) - adding PositionalParanamer from Stefan Fleiter

Release 2.5 - Apr 15 2012 (JDK 5.0 and above)

Release 2.4 - Oct 29 2011 (JDK 5.0 and above)

Release 2.3 - Oct 19 2010 (JDK 5.0 and above)

Release 2.2 - Jan 2 2010 (JDK 5.0 and above)

Release 2.1 - Sep 6 2009 (last version that is compatible with JDK 1.4)

Release 2.0 - Jul 10 2009

Release 1.5 - May 19 2009

Release 1.4 - May 9 2009

Release 1.3 - Feb 22 2009

Release 1.2 - Feb 2 2009

Release 1.1.7 - Jan 20 2009

Release 1.1.6 - Dec 22 2008

Release 1.1.5 - Aug 14 2008

Release 1.1.4 - Jun 19 2008

Release 1.1.3 - May 16 2008

Release 1.1.2 - Mar 16 2008

Release 1.1.1 - Nov 28 2007

Release 1.1 - Oct 14 2007

Release 1.0.1 - Jul 07 2007

Release 1.0 - Jul 01 2007

# Project Information

## Source Code

[See Project On Github](http://github.com/paul-hammant/paranamer) (yes pull requests are fine), mirrored from Codehaus - see the  [repo connection details](http://xircles.codehaus.org/projects/paranamer/repos/git/repo) for that.

## More Examples

The unit tests for Paranamer illustrate some more way to use
it: [http://github.com/paul-hammant/paranamer/tree/master/paranamer/src/test/com/thoughtworks/paranamer](http://github.com/paul-hammant/paranamer/tree/master/paranamer/src/test/com/thoughtworks/paranamer)

## Other Codehaus Project Info

Developers, Maven 2 repository, lists, sites are detailed here: [http://xircles.codehaus.org/projects/paranamer](http://xircles.codehaus.org/projects/paranamer)

## Downloads

Latest Released Jar file here (auto-download)

[href="http://nexus.codehaus.org/service/local/artifact/maven/redirect?r=releases&g=com.thoughtworks.paranamer&a=paranamer&v=2.6](http://nexus.codehaus.org/service/local/artifact/maven/redirect?r=releases&g=com.thoughtworks.paranamer&a=paranamer&v=2.6)
