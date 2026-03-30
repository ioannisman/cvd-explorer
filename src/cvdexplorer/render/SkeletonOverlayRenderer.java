package cvdexplorer.render;

import javafx.scene.paint.Color;
import xyz.marsavic.drawingfx.drawing.View;
import xyz.marsavic.geometry.Vector;

import java.util.ArrayList;
import java.util.List;

public final class SkeletonOverlayRenderer {
    public record Segment(Vector a, Vector b) {
    }

    @FunctionalInterface
    public interface PixelClusterScorer {
        // Pixel-space helper used to refine crossings without rebuilding the ownership grid.
        double score(Vector pixelPoint, int clusterIndex);
    }

    public void draw(
            View view,
            RasterDiagramRenderer.OwnershipGrid ownershipGrid,
            PixelClusterScorer scorer
    ) {
        List<Segment> segments = extractSegments(ownershipGrid, scorer);
        if (segments.isEmpty()) {
            return;
        }

        view.setStroke(Color.gray(0.0, 0.8));
        view.setLineWidth(1.5);
        for (Segment segment : segments) {
            // Segments are already expressed in pixel coordinates.
            view.strokeLineSegment(segment.a(), segment.b());
        }
    }

    static List<Segment> extractSegments(
            RasterDiagramRenderer.OwnershipGrid ownershipGrid,
            PixelClusterScorer scorer
    ) {
        int width = ownershipGrid.width();
        int height = ownershipGrid.height();
        List<Segment> segments = new ArrayList<>();

        if (width < 2 || height < 2) {
            return segments;
        }

        for (int y = 0; y < height - 1; y++) {
            for (int x = 0; x < width - 1; x++) {
                int topLeft = ownershipGrid.clusterIndexAt(x, y);
                int topRight = ownershipGrid.clusterIndexAt(x + 1, y);
                int bottomLeft = ownershipGrid.clusterIndexAt(x, y + 1);
                int bottomRight = ownershipGrid.clusterIndexAt(x + 1, y + 1);

                // The ownership grid tells us which cell edges the contour must cross.
                List<Vector> crossings = new ArrayList<>(4);
                if (topLeft != topRight) {
                    Vector a = Vector.xy(x + 0.5, y + 0.5);
                    Vector b = Vector.xy(x + 1.5, y + 0.5);
                    crossings.add(interpolateCrossing(a, b, topLeft, topRight, scorer));
                }
                if (topRight != bottomRight) {
                    Vector a = Vector.xy(x + 1.5, y + 0.5);
                    Vector b = Vector.xy(x + 1.5, y + 1.5);
                    crossings.add(interpolateCrossing(a, b, topRight, bottomRight, scorer));
                }
                if (bottomLeft != bottomRight) {
                    Vector a = Vector.xy(x + 0.5, y + 1.5);
                    Vector b = Vector.xy(x + 1.5, y + 1.5);
                    crossings.add(interpolateCrossing(a, b, bottomLeft, bottomRight, scorer));
                }
                if (topLeft != bottomLeft) {
                    Vector a = Vector.xy(x + 0.5, y + 0.5);
                    Vector b = Vector.xy(x + 0.5, y + 1.5);
                    crossings.add(interpolateCrossing(a, b, topLeft, bottomLeft, scorer));
                }

                if (crossings.size() == 2) {
                    segments.add(new Segment(crossings.get(0), crossings.get(1)));
                } else if (crossings.size() >= 3) {
                    // Multi-owner cells form a junction; the center fallback keeps the topology readable.
                    Vector center = Vector.xy(x + 1.0, y + 1.0);
                    for (Vector crossing : crossings) {
                        segments.add(new Segment(crossing, center));
                    }
                }
            }
        }

        return segments;
    }

    private static Vector interpolateCrossing(
            Vector a,
            Vector b,
            int ownerA,
            int ownerB,
            PixelClusterScorer scorer
    ) {
        // Interpolate the zero of score(ownerA) - score(ownerB) along the edge.
        double diffA = scorer.score(a, ownerA) - scorer.score(a, ownerB);
        double diffB = scorer.score(b, ownerA) - scorer.score(b, ownerB);
        double denominator = diffA - diffB;

        double t;
        if (Math.abs(denominator) < 1e-9) {
            // Flat edges fall back to the midpoint.
            t = 0.5;
        } else {
            t = diffA / denominator;
            t = Math.max(0.0, Math.min(1.0, t));
        }

        return a.add(b.sub(a).mul(t));
    }
}
