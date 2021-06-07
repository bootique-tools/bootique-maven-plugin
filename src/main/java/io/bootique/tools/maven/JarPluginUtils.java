package io.bootique.tools.maven;

import io.bootique.tools.maven.recipe.Recipe;
import io.bootique.tools.maven.xml.XmlUtils;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;

public class JarPluginUtils {
    private final PluginExecutor pluginExecutor;
    private final Recipe recipe;


    public JarPluginUtils(Recipe recipe, PluginExecutor pluginExecutor) {
        this.recipe = recipe;
        this.pluginExecutor = pluginExecutor;
    }

    /**
     * @return configuration of maven-jar-plugin with added classifier tag if conflict detected
     */
    public Xpp3Dom getAdditionalConfigurationIfNeededOrEmpty() {
        Plugin plugin = pluginExecutor.getMavenProject().getPlugin("org.apache.maven.plugins:maven-jar-plugin");
        if(plugin == null) {
            return configuration();
        }

        Xpp3Dom configuration = (Xpp3Dom) plugin.getConfiguration();
        MojoExecutor.Element classifierElement = element("classifier", generateClassifier());
        if (configuration == null) {
            configuration = configuration(classifierElement);
        } else {
            configuration.addChild(classifierElement.toDom());
        }
        return configuration;
    }

    private static String generateClassifier() {
        return UUID.randomUUID().toString();
    }

    /**
     * @param defaultArtifact default artifact to use if there's no plugin defined in the pom
     * @return custom maven-jar-plugin artifact if it declared in pom files and set as needed or default
     * <p>
     * Log info message if custom plugin not found and warning if custom maven-jar-plugin detected but not selected to use
     */
    public MavenArtifact getMavenArtifactOrDefault(MavenArtifact defaultArtifact) {
        boolean jarPluginDeclared = isJarPluginDeclaredInPomFileOrParents(defaultArtifact.getGroupId(), defaultArtifact.getArtifactId());
        if (recipe.isUserJarPluginRequired()) {
            if (!jarPluginDeclared) {
                pluginExecutor.getLog().warn("Custom maven-jar-plugin not found; Default plugin will be in use");
            } else {
                Plugin plugin = pluginExecutor.getMavenProject().getPlugin(defaultArtifact.getGroupId() + ":" + defaultArtifact.getArtifactId());
                if (plugin != null) {
                    return new MavenArtifact(plugin.getGroupId(), plugin.getArtifactId(), plugin.getVersion());
                }
            }
        } else if (jarPluginDeclared) {
            pluginExecutor.getLog().info("Custom maven-jar-plugin detected; To use it set useCustomJar" +
                    " parameter on true in bootique-maven-plugin configuration");
        }
        return defaultArtifact;
    }

    private boolean isJarPluginDeclaredInPomFileOrParents(String groupId, String artifactId) {
        MavenProject currentMavenProject = pluginExecutor.getMavenProject();
        while (currentMavenProject != null) {
            File pomFile = currentMavenProject.getFile();
            if (pluginDeclaredInFile(pomFile, groupId, artifactId)) {
                return true;
            }
            currentMavenProject = currentMavenProject.getParent();
        }
        return false;
    }

    private boolean pluginDeclaredInFile(File pomFile, String groupId, String artifactId) {
        Map<String, String> nodesWithValues = new HashMap<>();
        nodesWithValues.put("groupId", groupId);
        nodesWithValues.put("artifactId", artifactId);
        return XmlUtils.hasNodeWithCurrentChildNodesValues(pomFile, "plugin", nodesWithValues);
    }
}
