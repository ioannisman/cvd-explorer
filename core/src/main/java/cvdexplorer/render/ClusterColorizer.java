package cvdexplorer.render;

import cvdexplorer.core.DiagramRasterizer;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.Rgba;

import java.util.List;

public final class ClusterColorizer {
    private static final double SHADING_SCORE_SCALE = 480.0;

    private final List<ClusterSite> clusters;
    private final Rgba background;
    private final boolean shadingEnabled;

    public ClusterColorizer(List<ClusterSite> clusters, Rgba background, boolean shadingEnabled) {
        this.clusters = clusters;
        this.background = background;
        this.shadingEnabled = shadingEnabled;
    }

    public int color(DiagramRasterizer.Classification classification) {
        if (classification.clusterIndex() < 0) {
            return background.toArgb();
        }

        ClusterSite cluster = clusters.get(classification.clusterIndex());
        double brightness = shadingEnabled
                ? 0.6 + 0.4 * Math.exp(-classification.score() / SHADING_SCORE_SCALE)
                : 1.0;
        Rgba shaded = cluster.color().withBrightnessFactor(brightness);
        return shaded.toArgb();
    }
}
