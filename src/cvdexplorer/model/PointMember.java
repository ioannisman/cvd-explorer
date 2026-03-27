package cvdexplorer.model;

import xyz.marsavic.geometry.Vector;

public record PointMember(Vector position) implements ClusterMember {
    @Override
    public double distanceTo(Vector point) {
        return position.distanceTo(point);
    }

    @Override
    public Vector anchor() {
        return position;
    }
}
