package io.bootique.tools.maven;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor.ExecutionEnvironment;

import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;

public class PluginExecutor {

    private static final int UNKNOWN_VERSION = Integer.MIN_VALUE;

    private final ExecutionEnvironment environment;
    private final Log log;
    private final MavenProject mavenProject;

    public PluginExecutor(ExecutionEnvironment environment,
                   MavenProject mavenProject,
                   Log log) {
        this.environment = Objects.requireNonNull(environment);
        this.log = Objects.requireNonNull(log);
        this.mavenProject = Objects.requireNonNull(mavenProject);
    }

    public void execute(MavenArtifact mavenArtifact, String goal, Xpp3Dom config) throws MojoExecutionException {
        Plugin plugin = createPlugin(mavenArtifact);
        ConfigMerger configMerger = new ConfigMerger(plugin, log);
        executeMojo(plugin, goal, configMerger.mergeWith(config), environment);
    }

    Plugin createPlugin(MavenArtifact mavenArtifact) {
        Plugin plugin = getExistingPlugin(mavenArtifact.getGroupId(), mavenArtifact.getArtifactId());
        if(plugin != null) {
            int majorVersion = getMajorVersion(plugin.getVersion());
            if(majorVersion > 2) {
                getLog().info("Plugin version "
                        + plugin.getGroupId() + ":" + plugin.getArtifactId() + ":" + plugin.getVersion()
                        + " set in the pom.xml will be used.");
                // add additional dependencies that we need to the existing plugin configuration
                List<Dependency> dependencies = new ArrayList<>(plugin.getDependencies());
                dependencies.addAll(mavenArtifact.getDependencies());
                plugin.setDependencies(dependencies);
                return plugin;
            }
        }

        return plugin(
                mavenArtifact.getGroupId(),
                mavenArtifact.getArtifactId(),
                mavenArtifact.getVersion(),
                mavenArtifact.getDependencies()
        );
    }

    Plugin getExistingPlugin(String groupId, String artifactId) {
        for(Plugin next : mavenProject.getBuildPlugins()) {
            if(groupId.equals(next.getGroupId()) && artifactId.equals(next.getArtifactId())) {
                return next;
            }
        }

        return null;
    }

    int getMajorVersion(String version) {
        try {
            return Integer.parseInt(version.substring(0, version.indexOf('.')));
        } catch (Exception ex) {
            return UNKNOWN_VERSION;
        }
    }

    public MavenProject getMavenProject() {
        return mavenProject;
    }

    public Log getLog() {
        return log;
    }
}
