package io.bootique.tools.maven.recipe;

import java.util.Objects;

import io.bootique.tools.maven.PluginExecutor;
import org.apache.maven.plugin.MojoExecutionException;

public abstract class Recipe {

    protected final PluginExecutor pluginExecutor;

    public Recipe(PluginExecutor pluginExecutor) {
        this.pluginExecutor = Objects.requireNonNull(pluginExecutor);
    }

    public abstract void execute() throws MojoExecutionException;
}
