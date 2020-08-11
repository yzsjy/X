package neu.lab.conflict.data.dao;

import neu.lab.conflict.data.po.ArtifactVersion;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashSet;
import java.util.Set;

public interface ArtifactVersionDao {
    LinkedHashSet<ArtifactVersion> selectAllArtifactVersionByMavenArtifactId(int mavenArtifactId);

    void insertArtifactVersion(ArtifactVersion ArtifactVersion);

    void insertArtifactVersionSet(@Param("set") Set<ArtifactVersion> artifactVersions);

    int isExist(int mavenArtifactId);
}
