package io.bootique.tools.maven;

import java.util.Objects;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.xml.Xpp3Dom;

public class ConfigMerger {

    private final Plugin plugin;
    private final Log log;

    public ConfigMerger(Plugin plugin, Log log) {
        this.plugin = plugin;
        this.log = Objects.requireNonNull(log);
    }

    Xpp3Dom mergeWith(Xpp3Dom additionalConfig) {
        if(plugin != null && plugin.getConfiguration() != null && plugin.getConfiguration() instanceof Xpp3Dom) {
            Xpp3Dom existingConfig = (Xpp3Dom)plugin.getConfiguration();
            int i=0;
            for(Xpp3Dom child : additionalConfig.getChildren()) {
                if(existingConfig.getChild(child.getName()) != null) {
                    getLog().warn("Option <" + child.getName() + "> will be overridden for plugin "
                            + plugin.getGroupId() + ":" + plugin.getArtifactId());
                    existingConfig.removeChild(i);
                }
                existingConfig.addChild(child);
                i++;
            }
            return existingConfig;
        }

        return additionalConfig;
    }

    private Log getLog() {
        return log;
    }
}
