package cvdexplorer.model;

import xyz.marsavic.geometry.Vector;

public record LineMember(Vector a, Vector b) implements ClusterMember {
    @Override
    public double distanceTo(Vector point) {
        Vector ap = point.sub(a);
        Vector ab = b.sub(a);
        double ab2 = ab.lengthSquared();
        if (ab2 <= 0.0) {
            return point.distanceTo(a);
        }
        double t = ap.dot(ab) / ab2;
        Vector closest = a.add(ab.mul(t));
        return point.distanceTo(closest);
    }

    @Override
    public int handleCount() {
        return 2;
    }

    @Override
    public Vector getHandle(int index) {
        return switch (index) {
            case 0 -> a;
            case 1 -> b;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    @Override
    public ClusterMember withHandle(int index, Vector v) {
        return switch (index) {
            case 0 -> new LineMember(v, b);
            case 1 -> new LineMember(a, v);
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    @Override
    public Vector placementCentroid() {
        return a.add(b).mul(0.5);
    }
}
