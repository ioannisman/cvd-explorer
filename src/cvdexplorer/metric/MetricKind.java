package cvdexplorer.metric;

public enum MetricKind {
    MINIMUM_DISTANCE,
    MAXIMUM_DISTANCE,
    SUM_OF_DISTANCES,
    MEAN_DISTANCE;

    public MetricKind nextInCycle() {
        return switch (this) {
            case MINIMUM_DISTANCE -> MAXIMUM_DISTANCE;
            case MAXIMUM_DISTANCE -> SUM_OF_DISTANCES;
            case SUM_OF_DISTANCES -> MEAN_DISTANCE;
            case MEAN_DISTANCE -> MINIMUM_DISTANCE;
        };
    }
}
