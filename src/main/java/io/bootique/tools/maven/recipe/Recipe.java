package io.bootique.tools.maven.recipe;

import java.util.Objects;

import io.bootique.tools.maven.PluginExecutor;
import org.apache.maven.plugin.MojoExecutionException;

public abstract class Recipe {

    protected final PluginExecutor pluginExecutor;

    protected final boolean useCustomJar;

    public Recipe(PluginExecutor pluginExecutor, boolean useCustomJar) {
        this.pluginExecutor = Objects.requireNonNull(pluginExecutor);
        this.useCustomJar = useCustomJar;
    }

    public abstract void execute() throws MojoExecutionException;

}
