package io.bootique.tools.maven.recipe;

import java.util.Objects;

import io.bootique.tools.maven.JarPluginUtils;
import io.bootique.tools.maven.MavenArtifact;
import io.bootique.tools.maven.PluginExecutor;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

public abstract class Recipe {

    private boolean userJarPluginRequired = false;

    protected final PluginExecutor pluginExecutor;

    public Recipe(PluginExecutor pluginExecutor) {
        this.pluginExecutor = Objects.requireNonNull(pluginExecutor);
    }

    public abstract void execute() throws MojoExecutionException;

    public void setUserJarPluginRequired(boolean userJarPluginRequired) {
        this.userJarPluginRequired = userJarPluginRequired;
    }

    /**
     * Sets sequence of maven-jar-plugin execution steps
     */
    protected void executeJarPlugin() throws MojoExecutionException {
        JarPluginUtils jarPluginUtils = new JarPluginUtils(this, pluginExecutor);

        MavenArtifact artifact = jarPluginUtils.getMavenArtifactOrDefault(new MavenArtifact(
                "org.apache.maven.plugins",
                "maven-jar-plugin",
                "3.2.0"
        ));
        Xpp3Dom additionalConfig = jarPluginUtils.getAdditionalConfigurationIfNeededOrEmpty();
        Xpp3Dom defaultConfig = getDefaultJarConfig();

        pluginExecutor.execute(
                artifact,
                goal("jar"),
                Xpp3Dom.mergeXpp3Dom(defaultConfig, additionalConfig)
        );
    }

    protected abstract Xpp3Dom getDefaultJarConfig();

    public boolean isUserJarPluginRequired() {
        return userJarPluginRequired;
    }
}
