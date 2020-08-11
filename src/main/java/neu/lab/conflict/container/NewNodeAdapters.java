package neu.lab.conflict.container;

import neu.lab.conflict.util.Conf;
import neu.lab.conflict.util.MavenUtil;
import neu.lab.conflict.vo.DependencyInfo;
import org.apache.maven.shared.dependency.tree.DependencyNode;

import java.util.ArrayList;
import java.util.List;

public class NewNodeAdapters {
    private static NewNodeAdapters instance;

    public static NewNodeAdapters i() {
        return instance;
    }

    public static void init(DependencyNode root) {
        instance = new NewNodeAdapters();
        for (DependencyNode childDirect : root.getChildren()) {
            if (MavenUtil.i().getMojo().ignoreTestScope && "test".equals(childDirect.getArtifact().getScope())) {
                continue;
            }
            ArtifactNodes artifactNodes = new ArtifactNodes(childDirect.getArtifact().getGroupId(),
                    childDirect.getArtifact().getArtifactId(), childDirect.getArtifact().getVersion());
            NewNodeAdapters.i().addArtifactNodes(artifactNodes);
        }
        for (DependencyNode childDirect : root.getChildren()) {
            addIndirectArtifactNodes(childDirect, 2);
        }
    }

    private static void addIndirectArtifactNodes(DependencyNode dependencyNode, int depth) {
        if (depth <= Conf.maxDependencyDepth) {
            depth = depth + 1;
            for (DependencyNode child : dependencyNode.getChildren()) {
                if (MavenUtil.i().getMojo().ignoreTestScope && "test".equals(child.getArtifact().getScope())) {
                    continue;
                }
                if (hasThisArtifactNodes(child)) {
                    MavenUtil.i().getLog().info(child.getArtifact() + "this artifact node has exist");
                    continue;
                }
                ArtifactNodes artifactNodes = new ArtifactNodes(child.getArtifact().getGroupId(),
                        child.getArtifact().getArtifactId(), child.getArtifact().getVersion());
                NewNodeAdapters.i().addArtifactNodes(artifactNodes);
            }
            for (DependencyNode child : dependencyNode.getChildren()) {
                addIndirectArtifactNodes(child, depth);
            }
        }
    }

    private List<ArtifactNodes> container;

    public List<ArtifactNodes> getContainer() {
        return container;
    }

    private NewNodeAdapters() {
        container = new ArrayList<ArtifactNodes>();
    }

    public void addArtifactNodes(ArtifactNodes artifactNodes) {
        container.add(artifactNodes);
    }

    public ArtifactNodes getArtifactNodes(DependencyInfo dependencyInfo) {
        for (ArtifactNodes artifactNodes : container) {
            if (artifactNodes.isSelf(dependencyInfo.getGroupId(), dependencyInfo.getArtifactId())) {
                return artifactNodes;
            }
        }
        return null;
    }

    private static boolean hasThisArtifactNodes(DependencyNode dependencyNode) {
        for (ArtifactNodes artifactNodes : NewNodeAdapters.i().getContainer()) {
            if (artifactNodes.isSelf(dependencyNode.getArtifact().getGroupId(), dependencyNode.getArtifact().getArtifactId())) {
                return true;
            }
        }
        return false;
    }
}
