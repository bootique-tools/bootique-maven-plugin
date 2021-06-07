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
        Xpp3Dom configuration = configuration();
        if (plugin != null) {
            Object objConfiguration = plugin.getConfiguration();
            configuration = (Xpp3Dom) objConfiguration;
            String newClassifier = generateClassifierForConfiguration(configuration);

            MojoExecutor.Element classifierElement = element("classifier", newClassifier);
            if (configuration == null)
                configuration = configuration(classifierElement);
            else
                configuration.addChild(classifierElement.toDom());

        }
        return configuration;
    }

    private static String generateClassifierForConfiguration(Xpp3Dom basicConfiguration) {
        if (basicConfiguration == null)
            return UUID.randomUUID().toString();
        Xpp3Dom classifier = basicConfiguration.getChild("classifier");
        if (classifier == null)
            return UUID.randomUUID().toString();
        String newClassifier;
        do {
            newClassifier = UUID.randomUUID().toString();
        } while (classifier.getValue().equals(newClassifier));
        return newClassifier;
    }

    /**
     * @param artifact text representation of default maven-jar-plugin artifact in format groupId:artifactId:version
     * @return custom maven-jar-plugin artifact if it declared in pom files and set as needed or default
     * <p>
     * Log info message if custom plugin not found and warning if custom maven-jar-plugin detected but not selected to use
     */
    public MavenArtifact getMavenArtifactOrDefault(String artifact) {
        String[] parts = artifact.split(":");
        if (parts.length != 3) {
            throw new RuntimeException("incorrect artifact syntax");
        }

        boolean jarPluginDeclaredInPomFileOrParents = jarPluginDeclaredInPomFileOrParents(parts[0], parts[1]);

        if (recipe.isUserJarPluginRequired()) {
            if (!jarPluginDeclaredInPomFileOrParents) {
                pluginExecutor.getLog().warn("Custom maven-jar-plugin not found; Default plugin will be in use");
            } else {
                Plugin plugin = pluginExecutor.getMavenProject().getPlugin(parts[0] + ":" + parts[1]);
                if (plugin != null) {
                    return new MavenArtifact(plugin.getGroupId(), plugin.getArtifactId(), plugin.getVersion());
                }
            }
        } else if (jarPluginDeclaredInPomFileOrParents) {
            pluginExecutor.getLog().info("Custom maven-jar-plugin detected; To use it set useCustomJar" +
                    " parameter on true in bootique-maven-plugin configuration");
        }

        return new MavenArtifact(parts[0], parts[1], parts[2]);
    }

    private boolean jarPluginDeclaredInPomFileOrParents(String groupId, String artifactId) {
        MavenProject currentMavenProject = pluginExecutor.getMavenProject();
        while (currentMavenProject != null) {
            File pomFile = currentMavenProject.getFile();
            if (pluginDeclaredInFile(pomFile, groupId, artifactId))
                return true;
            currentMavenProject = currentMavenProject.getParent();
        }
        return false;
    }

    private boolean pluginDeclaredInFile(File pomFile, String groupId, String artifactId) {
        Map<String, String> nodesWithValues = new HashMap<String, String>() {{
            put("groupId", groupId);
            put("artifactId", artifactId);
        }};

        return XmlUtils.hasNodeWithCurrentChildNodesValues(pomFile, "plugin", nodesWithValues);
    }
}
