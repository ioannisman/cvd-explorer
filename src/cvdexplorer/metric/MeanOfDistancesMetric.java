package cvdexplorer.metric;

import cvdexplorer.model.ClusterMember;
import xyz.marsavic.geometry.Vector;

import java.util.List;

public final class MeanOfDistancesMetric implements ClusterMetric {
    private static final SumOfDistancesMetric SUM = new SumOfDistancesMetric();

    @Override
    public Result evaluate(Vector point, List<ClusterMember> members) {
        if (members.isEmpty()) {
            return new Result(Double.POSITIVE_INFINITY, -1);
        }
        return new Result(SUM.score(point, members) / members.size(), -1);
    }
}
