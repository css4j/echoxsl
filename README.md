# EchoXSL

 EchoXSL is a fork of Apache Xalan, an XSLT processor.

 Beware that this software is only maintained for compatibility purposes. Java
has built-in XSLT support, and [Saxonica](http://saxon.sourceforge.net/) has a
more complete XSL and XPath implementation (with an open source subset called
_Saxon HE_).

 You should only look at this project if you are willing to replace Apache Xalan
with something API-compatible.

<br/>

## Building from source

### Requirements

To build EchoXSL you should have the following software installed:

- The [Git version control system](https://git-scm.com/downloads) is required to
obtain the sources. Any recent version should suffice.

- Java 7 or later. You can install it from your favourite package manager or by
downloading from [Adoptium](https://adoptium.net/).

<br/>

### Building with Gradle

Execute the build script with `gradlew build` to build. For example:

```shell
git clone https://github.com/css4j/echoxsl.git
cd echoxsl
./gradlew build
```
or just `gradlew build` (without the `./`) on a Windows command prompt.

<br/>

### Deploying to a Maven repository

Use:

- `gradlew build publishToMavenLocal` to install in your local Maven repository.

- `gradlew publish` to deploy to a (generally remote) Maven repository.

If you plan to deploy to a repository, please configure the `mavenReleaseRepoUrl`
and/or `mavenSnapshotRepoUrl` properties (for example in
`GRADLE_USER_HOME/gradle.properties` or in the [command line](https://docs.gradle.org/current/userguide/build_environment.html#sec:project_properties)).
Otherwise, Gradle shall create a `build/repository` subdirectory and deploy there.

Properties `mavenRepoUsername` and `mavenRepoPassword` can also be set (generally
from the command line).

If you would rather look directly at the Gradle publish configurations, please
read the `publishing.repositories.maven` block of
[echoxsl.java-conventions.gradle](https://github.com/css4j/echoxsl/blob/master/buildSrc/src/main/groovy/echoxsl.java-conventions.gradle).

<br/>

## Open the project in your IDE

Modern IDEs are able to import Gradle projects and let it manage the
dependencies. In _IntelliJ IDEA_ you can just open the root directory and the
Gradle project is opened, while in the [Eclipse IDE](https://www.eclipse.org/)
you need to import it explicitly:
```
File > Import... > Gradle > Existing Gradle Project
```
Eclipse shall ask you if you want to use a wrapper or its own instance of
Gradle, select the "wrapper" choice.

In Eclipse, it is advisable to run a build with `./gradlew build` before
importing the project.

<br/>

##  Licensing

 For licensing issues, please read the [LICENSE.txt](LICENSE.txt) and
[NOTICE.txt](NOTICE.txt) files.
