package cvdexplorer.render;

import cvdexplorer.model.ClusterSite;
import javafx.scene.paint.Color;

import java.util.List;

public final class ClusterColorizer {
    private static final double SHADING_SCORE_SCALE = 480.0;
    private static final int STRIPE_WIDTH_PIXELS = 8;

    private final List<ClusterSite> clusters;
    private final Color background;
    private final boolean shadingEnabled;

    public ClusterColorizer(List<ClusterSite> clusters, Color background, boolean shadingEnabled) {
        this.clusters = clusters;
        this.background = background;
        this.shadingEnabled = shadingEnabled;
    }

    public int color(int x, int y, RasterDiagramRenderer.Classification classification) {
        if (classification.clusterMask() == 0L) {
            return toArgb(background);
        }

        ClusterSite cluster = clusterForStripe(classification.clusterMask(), stripeOrdinal(x, y, classification.clusterMask()));
        double brightness = shadingEnabled
                ? 0.6 + 0.4 * Math.exp(-classification.boundaryScore() / SHADING_SCORE_SCALE)
                : 1.0;
        Color shaded = cluster.color().deriveColor(0.0, 1.0, brightness, 1.0);
        return toArgb(shaded);
    }

    private int stripeOrdinal(int x, int y, long clusterMask) {
        int stripeIndex = (x + y) / STRIPE_WIDTH_PIXELS;
        return Math.floorMod(stripeIndex, Long.bitCount(clusterMask));
    }

    private ClusterSite clusterForStripe(long clusterMask, int ordinal) {
        long remaining = clusterMask;
        int remainingOrdinal = ordinal;
        while (remaining != 0L) {
            int clusterIndex = Long.numberOfTrailingZeros(remaining);
            if (remainingOrdinal == 0) {
                return clusters.get(clusterIndex);
            }
            remaining &= remaining - 1;
            remainingOrdinal--;
        }
        throw new IllegalArgumentException("clusterMask must contain at least one cluster");
    }

    private static int toArgb(Color color) {
        int a = (int) Math.round(color.getOpacity() * 255.0);
        int r = (int) Math.round(color.getRed() * 255.0);
        int g = (int) Math.round(color.getGreen() * 255.0);
        int b = (int) Math.round(color.getBlue() * 255.0);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
