package io.bootique.tools.maven;

import java.util.ArrayList;
import java.util.Map;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.xml.Xpp3Dom;

@Component(role = AbstractMavenLifecycleParticipant.class,
        hint = "io.bootique.tools.maven.JarPluginInterceptor")
public class JarPluginInterceptor extends AbstractMavenLifecycleParticipant implements LogEnabled {

    private static final String JAR_PLUGIN = "org.apache.maven.plugins:maven-jar-plugin";
    private static final String BQ_PLUGIN = "io.bootique.tools:bootique-maven-plugin";

    private Logger logger;

    public void afterProjectsRead( MavenSession session ) {
        for(MavenProject project : session.getProjects()) {
            Build build = project.getModel().getBuild();
            Map<String, Plugin> plugins = build.getPluginsAsMap();
            Plugin bqPlugin = plugins.get(BQ_PLUGIN);
            Plugin jarPlugin = plugins.get(JAR_PLUGIN);
            if(bqPlugin != null && jarPlugin != null) {
                Xpp3Dom configuration = (Xpp3Dom)bqPlugin.getConfiguration();
                if(configuration != null) {
                    Xpp3Dom useCustomJar = configuration.getChild("useCustomJar");
                    if (useCustomJar != null && "true".equals(useCustomJar.getValue())) {
                        break;
                    }
                }
                logger.info("Detected BQ plugin v" + bqPlugin.getVersion() + " for " + project.getArtifactId());
                for(PluginExecution execution : jarPlugin.getExecutions()) {
                    logger.info("Disabling maven-jar-plugin execution '" + execution.getId() + "' for " + project.getArtifactId());
                }
                jarPlugin.setExecutions(new ArrayList<>());
            }
        }
    }

    @Override
    public void enableLogging(Logger logger) {
        this.logger = logger;
    }
}
