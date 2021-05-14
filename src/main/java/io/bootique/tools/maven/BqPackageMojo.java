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
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.Map;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * Goal that packages Bootique app
 */
@Mojo(
        name = "bq-package",
        defaultPhase = LifecyclePhase.PACKAGE,
        requiresDependencyResolution = ResolutionScope.COMPILE
)
@Execute(
        phase = LifecyclePhase.COMPILE,
        goal = "compile"
)
public class BqPackageMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject mavenProject;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;

    /**
     * Assembly mode. Supported modes are 'assembly' and 'shade'.
     */
    @Parameter(defaultValue = "assembly")
    private String mode;

    /**
    * If custom maven-jar-plugin declared in pom files of project, this flag must be on to use it
    */
    @Parameter(name = "useCustomJar", defaultValue = "false")
    private String useCustomJar;


    public void execute() throws MojoExecutionException {
        ExecutionEnvironment environment = executionEnvironment(
                mavenProject,
                mavenSession,
                pluginManager
        );

        getLog().info("Building Bootique app assembly.");

        PluginExecutor pluginExecutor = new PluginExecutor(environment, mavenProject, getLog());

        Recipe recipe;
        switch (mode) {
            case "shade":
                recipe = new ShadeRecipe(pluginExecutor);
                break;
            case "assembly":
                recipe = new AssemblyRecipe(pluginExecutor);
                break;
            default:
                getLog().warn("Unknown packaging mode '" + mode + "', will use 'assembly' instead.");
                getLog().warn("Supported modes are 'assembly' and 'shade'.");
                recipe = new AssemblyRecipe(pluginExecutor);
        }

        recipe.setUserJarPluginRequired(Boolean.parseBoolean(useCustomJar));
        recipe.execute();
    }

}
