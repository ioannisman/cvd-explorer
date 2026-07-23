package cvdexplorer.model;

import cvdexplorer.geometry.Vector;

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
            case ELLIPSE -> {
                double angle = 2 * Math.PI * (memberIndex * 0.618033988749895 + clusterIndex * 0.31);
                Vector half = Vector.polar(18, angle);
                Vector focusA = hint.sub(half);
                Vector focusB = hint.add(half);
                Vector control = hint.add(Vector.polar(40, angle + Math.PI * 0.5));
                yield new EllipseMember(focusA, focusB, control);
            }
            case LINE -> {
                double angle = 2 * Math.PI * (memberIndex * 0.618033988749895 + clusterIndex * 0.31);
                Vector half = Vector.polar(22, angle);
                yield new LineMember(hint.sub(half), hint.add(half));
            }
        };
    }
}
