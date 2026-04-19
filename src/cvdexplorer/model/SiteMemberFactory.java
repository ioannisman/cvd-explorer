package cvdexplorer.model;

import xyz.marsavic.geometry.Vector;

public final class SiteMemberFactory {
    private SiteMemberFactory() {
    }

    public static ClusterMember createDefault(SiteMemberKind kind, int clusterIndex, int memberIndex, Vector hint) {
        return switch (kind) {
            case POINT -> new PointMember(hint);
            case LINE_SEGMENT -> {
                double angle = 2 * Math.PI * (memberIndex * 0.618033988749895 + clusterIndex * 0.31);
                Vector half = Vector.polar(22, angle);
                yield new SegmentMember(hint.sub(half), hint.add(half));
            }
            case CIRCLE -> {
                double angle = 2 * Math.PI * (memberIndex * 0.618033988749895 + clusterIndex * 0.31);
                Vector radius = Vector.polar(22, angle);
                yield new CircleMember(hint, hint.add(radius));
            }
            case LINE -> {
                double angle = 2 * Math.PI * (memberIndex * 0.618033988749895 + clusterIndex * 0.31);
                Vector half = Vector.polar(22, angle);
                yield new LineMember(hint.sub(half), hint.add(half));
            }
        };
    }
}
