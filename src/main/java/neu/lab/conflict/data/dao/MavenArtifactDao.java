package neu.lab.conflict.data.dao;

import neu.lab.conflict.data.po.MavenArtifact;

import java.util.List;

public interface MavenArtifactDao {
    List<MavenArtifact> selectAllMavenArtifact();

    int isExist(String groupId, String artifactId);

    void insertMavenArtifact(MavenArtifact mavenArtifact);

    MavenArtifact selectMavenArtifact(String groupId, String artifactId);

    void deleteMavenArtifact(String groupId, String artifactId);
}
