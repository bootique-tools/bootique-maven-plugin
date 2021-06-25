# Bootique Maven Plugin

Experimental maven plugin that simplifies packaging of Bootique apps.

## Usage

Include to your project:

```xml
<plugin>
    <groupId>io.bootique.tools</groupId>
    <artifactId>bootique-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    <extensions>true</extensions> <!-- this is optional, allows to override jar plugin executions -->
    <executions>
        <execution>
            <id>bq-package-assembly</id>
            <goals>
                <goal>bq-package</goal>
            </goals>
        </execution>
    </executions>
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

By default, `bq-package` will suppress all jar plugin executions and run it by itself with additional configuration.
If you need to use default (on any other) jar plugin executions you could use `useCustomJar` configuration option:

```xml
<configuration>
    <useCustomJar>true</useCustomJar>
</configuration>
```