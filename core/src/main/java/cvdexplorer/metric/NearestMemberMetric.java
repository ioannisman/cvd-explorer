package cvdexplorer.metric;

import cvdexplorer.model.ClusterMember;
import xyz.marsavic.geometry.Vector;

import java.util.List;

public final class NearestMemberMetric implements ClusterMetric {
    @Override
    public Result evaluate(Vector point, List<ClusterMember> members) {
        double best = Double.POSITIVE_INFINITY;
        int bestIndex = -1;
        for (int i = 0; i < members.size(); i++) {
            double d = members.get(i).distanceTo(point);
            if (d < best) {
                best = d;
                bestIndex = i;
            }
        }
        return new Result(best, bestIndex);
    }
}
