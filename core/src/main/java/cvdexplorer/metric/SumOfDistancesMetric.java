package cvdexplorer.metric;

import cvdexplorer.model.ClusterMember;
import cvdexplorer.geometry.Vector;

import java.util.List;

public final class SumOfDistancesMetric implements ClusterMetric {
    @Override
    public Result evaluate(Vector point, List<ClusterMember> members) {
        if (members.isEmpty()) {
            return new Result(Double.POSITIVE_INFINITY, -1);
        }
        double sum = 0.0;
        for (ClusterMember member : members) {
            sum += member.distanceTo(point);
        }
        return new Result(sum, -1);
    }
}
