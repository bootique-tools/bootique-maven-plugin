package io.bootique.tools.maven.recipe;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import io.bootique.tools.maven.MavenArtifact;
import io.bootique.tools.maven.PluginExecutor;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.xml.Xpp3Dom;

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

    public AssemblyRecipe(PluginExecutor pluginExecutor, boolean useCustomJar) {
        super(pluginExecutor, useCustomJar);
    }

    @Override
    public void execute() throws MojoExecutionException {
        executeJarPlugin();
        executeDependencyPlugin();
        executeAssemblyPlugin();
    }

    protected void executeJarPlugin() throws MojoExecutionException {
        if(pluginExecutor.hasJarPluginExecutions()) {
            if(!useCustomJar) {
                Log log = pluginExecutor.getLog();
                log.warn("maven-jar-plugin default execution is enabled and the final jar could be incorrect");
                log.warn("use <extensions>true</extensions> in the bootique-maven-plugin to fix this automatically");
                log.warn("or set <useCustomJar>true</useCustomJar> to generate a separate jar for the package.");
                return;
            }
        }

        MavenArtifact artifact = new MavenArtifact(
                "org.apache.maven.plugins",
                "maven-jar-plugin",
                "3.2.0"
        );

        Element baseConfig = element(name("archive"),
                element("manifest",
                        element("mainClass", "${main.class}"),
                        element("addClasspath", "true"),
                        element("classpathPrefix", "lib/"),
                        element("useUniqueVersions", "false")
                )
        );

        Xpp3Dom config = useCustomJar
                ? configuration(baseConfig, element("classifier", "bq"))
                : configuration(baseConfig);

        pluginExecutor.execute(
                artifact,
                goal("jar"),
                config
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
