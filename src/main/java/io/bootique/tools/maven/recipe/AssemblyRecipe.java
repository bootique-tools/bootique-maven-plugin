package io.bootique.tools.maven.recipe;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import io.bootique.tools.maven.MavenArtifact;
import io.bootique.tools.maven.PluginExecutor;
import org.apache.maven.plugin.MojoExecutionException;

import static java.util.Collections.singletonList;
import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * Assembly recipe that packages project into jar + lib folder packed to tar.gz archive
 */
public class AssemblyRecipe extends Recipe {

    private static final String VERSION_BUNDLE = "io.bootique.tools.maven.version";
    private static final String PLUGIN_VERSION;
    static {
        PLUGIN_VERSION = getVersionBundle().getString("project.version");
    }

    public AssemblyRecipe(PluginExecutor pluginExecutor) {
        super(pluginExecutor);
    }

    @Override
    public void execute() throws MojoExecutionException {
        executeJarPlugin();
        executeDependencyPlugin();
        executeAssemblyPlugin();
    }

    void executeJarPlugin() throws MojoExecutionException {
        MavenArtifact artifact = new MavenArtifact(
                "org.apache.maven.plugins",
                "maven-jar-plugin",
                "3.2.0"
        );

        pluginExecutor.execute(
                artifact,
                goal("jar"),
                configuration(
                        element(name("archive"),
                                element("manifest",
                                        element("mainClass", "${main.class}"),
                                        element("addClasspath", "true"),
                                        element("classpathPrefix", "lib/"),
                                        element("useUniqueVersions", "false")
                                )
                        )
                )
        );
    }

    void executeDependencyPlugin() throws MojoExecutionException {
        MavenArtifact artifact = new MavenArtifact(
                "org.apache.maven.plugins",
                "maven-dependency-plugin",
                "3.1.1"
        );

        pluginExecutor.execute(
                artifact,
                goal("copy-dependencies"),
                configuration(
                        element(name("outputDirectory"), "${project.build.directory}/lib"),
                        element(name("includeScope"), "compile")
                )
        );
    }

    void executeAssemblyPlugin() throws MojoExecutionException {
        MavenArtifact artifact = new MavenArtifact(
                "org.apache.maven.plugins",
                "maven-assembly-plugin",
                "3.2.0",
                singletonList(
                        dependency("io.bootique.tools", "bootique-maven-plugin", PLUGIN_VERSION)
                )
        );

        pluginExecutor.execute(
                artifact,
                goal("single"),
                configuration(
                        element(name("appendAssemblyId"), "false"),
                        element(name("tarLongFileMode"), "posix"),
                        element(name("descriptorRefs"),
                                element("descriptorRef", "bq-assembly")
                        )
                )
        );
    }

    private static ResourceBundle getVersionBundle() {
        try {
            return ResourceBundle.getBundle(VERSION_BUNDLE);
        } catch (MissingResourceException e) {
            throw new RuntimeException("Can't load properties: " + VERSION_BUNDLE, e);
        }
    }
}
