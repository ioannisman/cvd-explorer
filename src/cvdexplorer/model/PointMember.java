package cvdexplorer.model;

import xyz.marsavic.geometry.Vector;

public record PointMember(Vector position) implements ClusterMember {
    @Override
    public double distanceTo(Vector point) {
        return position.distanceTo(point);
    }

    @Override
    public int handleCount() {
        return 1;
    }

    @Override
    public Vector getHandle(int index) {
        if (index != 0) {
            throw new IndexOutOfBoundsException(index);
        }
        return position;
    }

    @Override
    public ClusterMember withHandle(int index, Vector v) {
        if (index != 0) {
            throw new IndexOutOfBoundsException(index);
        }
        return new PointMember(v);
    }
}
