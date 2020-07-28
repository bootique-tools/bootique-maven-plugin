package io.bootique.tools.maven;

import java.util.Collections;
import java.util.List;

import org.apache.maven.model.Dependency;

public class MavenArtifact {

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final List<Dependency> dependencies;

    public MavenArtifact(String groupId, String artifactId, String version) {
        this(groupId, artifactId, version, Collections.emptyList());
    }

    public MavenArtifact(String groupId, String artifactId, String version, List<Dependency> dependencies) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.dependencies = dependencies;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }
}
