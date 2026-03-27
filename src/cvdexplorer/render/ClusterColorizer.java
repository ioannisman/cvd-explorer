package cvdexplorer.render;

import cvdexplorer.model.ClusterSite;
import javafx.scene.paint.Color;

import java.util.List;

public final class ClusterColorizer {
    private final List<ClusterSite> clusters;
    private final Color background;

    public ClusterColorizer(List<ClusterSite> clusters, Color background) {
        this.clusters = clusters;
        this.background = background;
    }

    public int color(RasterDiagramRenderer.Classification classification) {
        if (classification.clusterIndex() < 0) {
            return toArgb(background);
        }

        ClusterSite cluster = clusters.get(classification.clusterIndex());
        double brightness = 0.6 + 0.4 * Math.exp(-classification.score() / 180.0);
        Color shaded = cluster.color().deriveColor(0.0, 1.0, brightness, 1.0);
        return toArgb(shaded);
    }

    private static int toArgb(Color color) {
        int a = (int) Math.round(color.getOpacity() * 255.0);
        int r = (int) Math.round(color.getRed() * 255.0);
        int g = (int) Math.round(color.getGreen() * 255.0);
        int b = (int) Math.round(color.getBlue() * 255.0);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
