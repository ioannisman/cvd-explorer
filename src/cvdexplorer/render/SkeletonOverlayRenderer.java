package cvdexplorer.render;

import javafx.scene.paint.Color;
import xyz.marsavic.drawingfx.drawing.View;
import xyz.marsavic.geometry.Vector;

import java.util.ArrayList;
import java.util.List;

public final class SkeletonOverlayRenderer {
    public record Segment(Vector a, Vector b) {
    }

    public void draw(View view, RasterDiagramRenderer.OwnershipGrid ownershipGrid) {
        List<Segment> segments = extractSegments(ownershipGrid);
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
            RasterDiagramRenderer.OwnershipGrid ownershipGrid
    ) {
        int width = ownershipGrid.width();
        int height = ownershipGrid.height();
        List<Segment> segments = new ArrayList<>();

        if (width < 2 || height < 2) {
            return segments;
        }

        for (int y = 0; y < height - 1; y++) {
            for (int x = 0; x < width - 1; x++) {
                long topLeft = ownershipGrid.regionMaskAt(x, y);
                long topRight = ownershipGrid.regionMaskAt(x + 1, y);
                long bottomLeft = ownershipGrid.regionMaskAt(x, y + 1);
                long bottomRight = ownershipGrid.regionMaskAt(x + 1, y + 1);

                // Different order-k membership sets indicate a boundary between cells.
                List<Vector> crossings = new ArrayList<>(4);
                if (topLeft != topRight) {
                    crossings.add(Vector.xy(x + 1.0, y + 0.5));
                }
                if (topRight != bottomRight) {
                    crossings.add(Vector.xy(x + 1.5, y + 1.0));
                }
                if (bottomLeft != bottomRight) {
                    crossings.add(Vector.xy(x + 1.0, y + 1.5));
                }
                if (topLeft != bottomLeft) {
                    crossings.add(Vector.xy(x + 0.5, y + 1.0));
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
}
