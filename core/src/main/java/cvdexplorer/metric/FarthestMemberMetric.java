package cvdexplorer.metric;

import cvdexplorer.model.ClusterMember;
import xyz.marsavic.geometry.Vector;

import java.util.List;

public final class FarthestMemberMetric implements ClusterMetric {
    @Override
    public Result evaluate(Vector point, List<ClusterMember> members) {
        if (members.isEmpty()) {
            return new Result(Double.POSITIVE_INFINITY, -1);
        }

        double best = Double.NEGATIVE_INFINITY;
        int bestIndex = -1;
        for (int i = 0; i < members.size(); i++) {
            double d = members.get(i).distanceTo(point);
            if (d > best) {
                best = d;
                bestIndex = i;
            }
        }
        return new Result(best, bestIndex);
    }
}
