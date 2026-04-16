package cvdexplorer.metric;

import cvdexplorer.model.ClusterMember;
import xyz.marsavic.geometry.Vector;

import java.util.List;

public final class MeanOfDistancesMetric implements ClusterMetric {
    private static final SumOfDistancesMetric SUM = new SumOfDistancesMetric();

    @Override
    public double score(Vector point, List<ClusterMember> members) {
        if (members.isEmpty()) {
            return Double.POSITIVE_INFINITY;
        }
        return SUM.score(point, members) / members.size();
    }
}
