package io.bootique.tools.maven;

import io.bootique.tools.maven.recipe.AssemblyRecipe;
import io.bootique.tools.maven.recipe.Recipe;
import io.bootique.tools.maven.recipe.ShadeRecipe;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * Goal that packages application into a runnable jar.
 * <br/>
 * This plugins supports two packaging modes: <ul>
 *     <li> assembly - application will be packaged into a runnable jar + /lib folder with dependencies
 *     <li> shade - application will be package into a single jar via maven-shade-plugin
 * </ul>
 */
@Mojo(
        name = "bq-package",
        defaultPhase = LifecyclePhase.PACKAGE,
        requiresDependencyResolution = ResolutionScope.COMPILE
)
public class BqPackageMojo extends AbstractMojo {

    private static final String SHADE = "shade";
    private static final String ASSEMBLY = "assembly";

    @Component
    private BuildPluginManager pluginManager;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject mavenProject;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession mavenSession;

    /**
     * Packaging mode. Supported modes are 'assembly' and 'shade'.
     */
    @Parameter(defaultValue = "assembly")
    private String mode;

    /**
     * Build and use separate Jar for the package.
     * Default value is false, i.e. this plugin will use default jar performing additional configuration.
     */
    @Parameter(defaultValue = "false")
    private boolean useCustomJar;

    public void execute() throws MojoExecutionException {
        ExecutionEnvironment environment = executionEnvironment(
                mavenProject,
                mavenSession,
                pluginManager
        );

        if(mavenSession.getResult().hasExceptions()) {
            getLog().warn("Build has failures, stop.");
            return;
        }

        getLog().info("Building Bootique app assembly.");

        PluginExecutor pluginExecutor = new PluginExecutor(environment, mavenProject, getLog());

        Recipe recipe;
        switch (mode) {
            case SHADE:
                recipe = new ShadeRecipe(pluginExecutor, useCustomJar);
                break;
            case ASSEMBLY:
                recipe = new AssemblyRecipe(pluginExecutor, useCustomJar);
                break;
            default:
                getLog().warn("Unknown packaging mode '" + mode + "', will use 'assembly' instead.");
                getLog().warn("Supported modes are 'assembly' and 'shade'.");
                recipe = new AssemblyRecipe(pluginExecutor, useCustomJar);
        }

        recipe.execute();
    }

}
