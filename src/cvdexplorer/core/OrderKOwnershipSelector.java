package cvdexplorer.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class OrderKOwnershipSelector implements ClusterOwnershipSelector {
    private static final Comparator<ClusterScore> BY_SCORE_THEN_INDEX =
            Comparator.comparingDouble(ClusterScore::score)
                    .thenComparingInt(ClusterScore::clusterIndex);

    private final int orderKOneBased;

    public OrderKOwnershipSelector(int orderKOneBased) {
        this.orderKOneBased = orderKOneBased;
    }

    @Override
    public RegionMembership select(List<ClusterScore> clusterScores) {
        if (clusterScores.isEmpty()) {
            throw new IllegalArgumentException("clusterScores must not be empty");
        }

        List<ClusterScore> rankedScores = new ArrayList<>(clusterScores);
        rankedScores.sort(BY_SCORE_THEN_INDEX);

        int selectedCount = Math.max(1, Math.min(rankedScores.size(), orderKOneBased));
        long clusterMask = 0L;
        double boundaryScore = rankedScores.get(selectedCount - 1).score();
        for (int i = 0; i < selectedCount; i++) {
            clusterMask |= 1L << rankedScores.get(i).clusterIndex();
        }
        return new RegionMembership(clusterMask, boundaryScore);
    }
}
