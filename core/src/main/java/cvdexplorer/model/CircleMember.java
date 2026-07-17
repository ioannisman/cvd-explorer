package cvdexplorer.model;

import xyz.marsavic.geometry.Vector;

public record CircleMember(Vector center, Vector radiusHandle) implements ClusterMember {
    public double radius() {
        return center.distanceTo(radiusHandle);
    }

    @Override
    public double distanceTo(Vector point) {
        return Math.abs(center.distanceTo(point) - radius());
    }

    @Override
    public int handleCount() {
        return 2;
    }

    @Override
    public Vector getHandle(int index) {
        return switch (index) {
            case 0 -> center;
            case 1 -> radiusHandle;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    @Override
    public ClusterMember withHandle(int index, Vector v) {
        return switch (index) {
            case 0 -> {
                Vector offset = radiusHandle.sub(center);
                yield new CircleMember(v, v.add(offset));
            }
            case 1 -> new CircleMember(center, v);
            default -> throw new IndexOutOfBoundsException(index);
        };
    }
}
