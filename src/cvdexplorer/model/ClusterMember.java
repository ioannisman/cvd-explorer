package cvdexplorer.model;

import xyz.marsavic.geometry.Vector;

public interface ClusterMember {
    double distanceTo(Vector point);

    int handleCount();

    Vector getHandle(int index);

    ClusterMember withHandle(int index, Vector v);

    default Vector placementCentroid() {
        return getHandle(0);
    }

    default Vector anchor() {
        return getHandle(0);
    }
}
