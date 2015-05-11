# ![ParaNamer](http://paulhammant.com/images/ParaNamer.jpg)

## Method Parameter Name Access for Java*

* versions PRIOR TO Java <del>7.0</del> 8.0

# What is it?

It is a library that allows the parameter names of non-private methods and constructors to be accessed at runtime. Normally this information is dropped by the compiler. In effect, methods like <code>doSometing(mypkg.Person **toMe**)</code>
currently look like <code>doSomething(mypackage.Person **???**)</code> to people using Java's reflection to inspect methods. 

To date parameter name access has not been very useful to Java application developers, but with the advent of advanced scripting languages and web action frameworks for the JVM it is of increasing importance to be able to leverage a method's parameter names. Scripting languages like [Groovy](http://groovy.codehaus.org/) &amp; [JRuby](http://jruby.codehaus.org/), web action frameworks like [Waffle](http://waffle.codehaus.org) and [VRaptor](# "http://www.vraptor.org/") (that verge on the transparent) and the compelling [Grails](http://grails.codehaus.org/). SOAP and REST designs could also benefit.

ParaNamer allows you to generate and use parameter name info for versions of Java prior to JDK 5.0 and above. Parameter name access was scheduled for JDK 6.0, but was cancelled at a late stage as the spec-lead suggested the development team ran out of time to implement it. It didn't ship in JDK 7.0 either, though it did in JDK 8 (see below). Historically, it was felt that applications could end up depending on parameter names, and that they essentially became part of constructor/method signatures and could never be changed if you wanted to be backwards compatible.  The view of the authors of Paranamer is that you should be aware that parameter names may change between releases, and code to not depend on them.

Paranamer is Open Source, and licensed as BSD, and first created in in July 2006. It is compatible with commercial/proprietary, GPL, BSD, and Apache (or any open/free source) use.

# Java8 has parameter name access built in!

Paranamer is gaining JDK 8 compatibility. JDK 8 though has native support though, and [stackoverflow](http://stackoverflow.com/questions/21455403/how-to-get-method-parameter-names-in-java-8-using-reflection) shows you how to make that work.

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
      + "<init> com.example.PeopleService peopleService \n"
      + "setName java.lang.String,java.lang.String givenName,familyName \n";
      + "setDateOfBirth int,int,int day,month,year \n";
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

AnnotationParanamer takes a delegate paranamer instance as an optional constructor arg.  This will allow constructors and methods to only partly leverage <code>@Named</code>, with other parameters having non-annotated parameter names (the via say <code>DefaulParanamer</code> or <code>BytecodeReadingParanamer</code>).

If you have an alternate annotation to <code>@Named</code>, then you can specify that in a subclass of <code>AnnotationParanamer</code> that overrides two methods isNamed and getNamedValue.  Your overridden methods should do the equivalent of 'return an instance of Named' and <code>return ((Named) ann).value();</code> respectively.

If you are using @Named from JSR 330, you will need it in your classpath of course.  In Maven terms, Paranamer is built with the <code>javax.atinject</code> module as an optional dependency.

## CachingParanamer

CachingParanamer stores the results of each parameter name lookup, so that second and subsequent invocations will be far quicker.

There's a subclass of <code>CachingParanamer</code> called <code>CachingParanamer.WithoutWeakReferences</code>. It does not use a WeakHashMap as an internal implementation. If you're great with profiling of applications under load, you might be able to justify use of this implementation for your particular app.

## AdaptiveParanamer

AdaptiveParanamer is designed for using a series of Paranamer implementations together. The first supplied is asked if it can supply parameter name data for a constructor/method.  If it cannot, then the next one is asked and so on.  The default constructor for this uses DefaultParanamer with <code>ByteCodeReadingParanamer</code> as its contingency.

# Feeding DefaultParanamer
##  Generating __PARANAMER_DATA with Ant

This for <code>DefaultParanamer</code> usage of course, as <code>BytecodeReadingParanamer</code> does not need it.

You need to download:

*   [latest paranamer jar](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22paranamer%22)
*   [latest paranamer-generator jar](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22paranamer-generator%22)
*   [latest paranamer-ant jar](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22paranamer-ant%22)
*   [latest qdox jar](http://repo1.maven.org/maven2/qdox/qdox/) (1.8 or above)

... and declare in your Ant script the following after &lt;javac/&gt; (remember to add to the taskdef classpath all the above jars):

```xml
<taskdef name="paranamer" 
       classname="com.thoughtworks.paranamer.ant.ParanamerGeneratorTask"/>
  <paranamer sourceDirectory="src/java" outputDirectory="target/classes"/>
```

Classes are changed to have an extra static String member variable that contains the member functions and their parameter names. Be sure to zip them up in to you final jar.

##  Generating __PARANAMER_DATA with Maven 2 or 3

For Maven, configuration is simpler. Just add this to the build/plugins section of your pom.xml:

```xml
<plugin>
    <groupId>com.thoughtworks.paranamer</groupId>
    <artifactId>paranamer-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>run</id>  <!-- id is optional -->
            <configuration>
                <sourceDirectory>${project.build.sourceDirectory}</sourceDirectory>
                <outputDirectory>${project.build.outputDirectory}</outputDirectory>
            </configuration>        
             <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
    <dependencies>
        <!-- if some of parameter names you need to retain are held in pre-existing jars, they need to be added to the classpath -->
        <dependency>
            <groupId>some-artifact-group</groupId>
            <artifactId>some-artifact</artifactId>
            <version>1.0</version>
        </dependency>
    </dependencies>
</plugin>
```

The classes in the ultimate jar file will automatically be made with parameter name data.

## Embedding Paranamer in your jar

There are already too many jar's for day to day Java development right? Simply consume the runtime paranamer jar into your project's jar using the Maven2 'shade' plugin.

```xml
<!-- Put in your POM.xml file -->
<build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
        <executions>
          <execution>
            <phase>package</phase>
            <goals>
              <goal>shade</goal>
            </goals>
            <configuration>
              <shadedArtifactId>artifact-shaded</shadedArtifactId>
              <relocations>
                <relocation>
                  <pattern>com.thoughtworks.paranamer</pattern>
                  <shadedPattern>com.yourcompany.yourapp.paranamer</shadedPattern>
                </relocation>
              </relocations>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
```

# What if a parameter names changes?

The general answer to this question is that you should not ship something to third parties where they are going to hard-core your parameter names into their application. For you own in-house stuff, accessing parameter names is harmless. You should be able to ripple though the source code of even large applications, and change say <code>badSpeeldWord</code> to <code>badlySpelledWorld</code> if you need to.

# Other Modules

* paranamer-ant - Ant tasks
* paranamer-maven-plugin - a Maven plugin (as shown above)

# Paranamer's Future

The need for Paranamer will go away when [JEP-118](http://openjdk.java.net/jeps/118) lands as part of Java8.  Despite that, we intend to maintain the library so that it continues to work.

# Projects using it

* [Mandragora - extensible framework for business layers](http://mandragora.sourceforge.net)
* [Oval - Object Validation Framework](http://oval.sourceforge.net/),
* [PicoContainer - Dependency Injection Container](http://picocontainer.org/),
* [PicoContainer Web-Remoting](http://picocontainer.org/web/remoting),
* [VRaptor - Web Framework](http://vraptor.org/),
* [Waffle - Web Framework](http://waffle.codehaus.org/)
* [JBehave - BDD testing](http://jbehave.codehaus.org/)
* [CDI Interceptors](https://github.com/sfleiter/cdi-interceptors)
* [A Jackson extension - serialization](https://github.com/FasterXML/jackson-module-paranamer)

# Releases

* Release 2.7 - Aug 16 2014 - QDox changed from 1.x to 2.x meaning Java7 features are supported. Thanks to [PR16](https://github.com/paul-hammant/paranamer/pull/16) by Robert Scholte
* Release 2.6.1 - Jul 18 2014 - New OSGi bundle info [PR15](https://github.com/paul-hammant/paranamer/pull/15) by Raghu Devarakonda, a new -DskipParanamer CLI option [PR14](https://github.com/paul-hammant/paranamer/pull/14) by Nicholas Whitehead, a new and optional non-WeakHashMap caching impl [Issue 8](https://github.com/paul-hammant/paranamer/issues/8), Maven fixups and Code refactoring from Otávio Garcia (PR13, PR14), 
* Release 2.6 - Oct 9 2013  - adding PositionalParanamer from Stefan Fleiter
* Release 2.5 - Apr 15 2012 
* Release 2.4 - Oct 29 2011 
* Release 2.3 - Oct 19 2010 
* Release 2.2 - Jan 2 2010 
* Release 2.1 - Sep 6 2009  - last version that is compatible with JDK 1.4, all others after this require Java 5
* Release 2.0 - Jul 10 2009
* Release 1.5 - May 19 2009
* Release 1.4 - May 9 2009
* Release 1.3 - Feb 22 2009
* Release 1.2 - Feb 2 2009
* Release 1.1 - Oct 14 2007
* Release 1.0 - Jul 01 2007

# Project Information

## Source Code

[See Project On Github](http://github.com/paul-hammant/paranamer) (yes pull requests are fine), mirrored from Codehaus - see the  [repo connection details](http://xircles.codehaus.org/projects/paranamer/repos/git/repo) for that.

## Committers & Contributors

* Paul Hammant
* Mauro Talevi
* Guilherme Silveira
* Sam Halliday
* Stefan Fleiter
* Robert Scholte
* Raghuram Devarakonda
* Nicholas Whitehead
* Otávio Garcia

## Other Codehaus Project Info

Github's [issue tracker for Paranamer](https://github.com/paul-hammant/paranamer/issues) is kinda how conversations happen re Paranamer.

## Downloads

Download the latest released jar files (2.7.1) [here](http://central.maven.org/maven2/com/thoughtworks/paranamer/paranamer/2.7.1/)

# More Examples

The unit tests for Paranamer illustrate some more way to use it: [http://github.com/paul-hammant/paranamer/tree/master/paranamer/src/test/com/thoughtworks/paranamer](http://github.com/paul-hammant/paranamer/tree/master/paranamer/src/test/com/thoughtworks/paranamer)

