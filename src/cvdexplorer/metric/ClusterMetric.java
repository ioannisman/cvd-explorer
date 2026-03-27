package cvdexplorer.metric;

import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.ClusterSite;
import xyz.marsavic.geometry.Vector;

import java.util.List;

public interface ClusterMetric {
    double score(Vector point, List<ClusterMember> members);

    default double score(Vector point, ClusterSite cluster) {
        return score(point, cluster.members());
    }
}
