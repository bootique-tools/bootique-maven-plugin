# Bootique Maven Plugin

Experimental maven plugin that simplifies packaging of Bootique apps.

## Usage

Include to your project:

```xml
<plugin>
    <groupId>io.bootique.tools</groupId>
    <artifactId>bootique-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
</plugin>
```

Run:

```shell script
mvn bootique:bq-package
```

## Configuration

Plugin supports two recipes of app packaging.

- Archive with runnable jar and lib folder with all dependencies.
This is a default mode, but you can set it explicitly:
```xml
<configuration>
    <mode>assembly</mode>
</configuration>
```

- Single jar file with all dependencies repacked inside: 
```xml
<configuration>
    <mode>shade</mode>
</configuration>
```