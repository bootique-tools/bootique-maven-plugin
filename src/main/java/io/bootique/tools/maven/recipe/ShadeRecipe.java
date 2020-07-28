package io.bootique.tools.maven.recipe;

import io.bootique.tools.maven.MavenArtifact;
import io.bootique.tools.maven.PluginExecutor;
import org.apache.maven.plugin.MojoExecutionException;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * Assembly recipe that packages project into the "fat" jar
 */
public class ShadeRecipe extends Recipe {

    public ShadeRecipe(PluginExecutor pluginExecutor) {
        super(pluginExecutor);
    }

    @Override
    public void execute() throws MojoExecutionException {
        executeJarPlugin();
        executeShadePlugin();
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
                configuration()
        );
    }

    void executeShadePlugin() throws MojoExecutionException {
        MavenArtifact artifact = new MavenArtifact(
                "org.apache.maven.plugins",
                "maven-shade-plugin",
                "3.2.1"
        );

        pluginExecutor.execute(
                artifact,
                goal("shade"),
                configuration(
                        element("createDependencyReducedPom", "true"),
                        element(name("filters"),
                                element("filter",
                                        element("artifact", "*:*"),
                                        element("excludes",
                                                element("exclude", "META-INF/*.SF"),
                                                element("exclude", "META-INF/*.DSA"),
                                                element("exclude", "META-INF/*.RSA")
                                        )
                                )
                        ),
                        element(name("transformers"),
                                element("transformer",
                                        attributes(attribute("implementation", "org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"))),
                                element("transformer",
                                        attributes(attribute("implementation", "org.apache.maven.plugins.shade.resource.ManifestResourceTransformer")),
                                        element("mainClass", "${main.class}"))
                        )
                )
        );
    }
}
