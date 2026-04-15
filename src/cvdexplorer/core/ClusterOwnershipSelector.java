package cvdexplorer.core;

import java.util.List;

public interface ClusterOwnershipSelector {
    RegionMembership select(List<ClusterScore> clusterScores);

    record ClusterScore(int clusterIndex, double score) {
    }

    record RegionMembership(long clusterMask, double boundaryScore) {
        public boolean containsCluster(int clusterIndex) {
            return (clusterMask & (1L << clusterIndex)) != 0L;
        }

        public int clusterCount() {
            return Long.bitCount(clusterMask);
        }
    }
}
