package cvdexplorer.model;

import xyz.marsavic.geometry.Vector;

public interface ClusterMember {
    double distanceTo(Vector point);

    Vector anchor();
}
