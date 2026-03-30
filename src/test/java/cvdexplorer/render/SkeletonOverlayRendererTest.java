package cvdexplorer.render;

import org.junit.jupiter.api.Test;
import xyz.marsavic.geometry.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkeletonOverlayRendererTest {

    @Test
    void extractSegmentsLeavesUniformOwnershipEmpty() {
        RasterDiagramRenderer.OwnershipGrid ownershipGrid = new RasterDiagramRenderer.OwnershipGrid(
                3,
                2,
                new int[] {
                        0, 0, 0,
                        0, 0, 0
                }
        );

        List<SkeletonOverlayRenderer.Segment> segments = SkeletonOverlayRenderer.extractSegments(
                ownershipGrid,
                (point, clusterIndex) -> clusterIndex
        );

        assertTrue(segments.isEmpty());
    }

    @Test
    void extractSegmentsInterpolateSingleContourCrossing() {
        RasterDiagramRenderer.OwnershipGrid ownershipGrid = new RasterDiagramRenderer.OwnershipGrid(
                2,
                2,
                new int[] {
                        0, 1,
                        0, 1
                }
        );

        // The two scores balance at x = 0.85, so the contour should shift off the midpoint.
        List<SkeletonOverlayRenderer.Segment> segments = SkeletonOverlayRenderer.extractSegments(
                ownershipGrid,
                (point, clusterIndex) -> switch (clusterIndex) {
                    case 0 -> point.x();
                    case 1 -> 1.7 - point.x();
                    default -> 10.0;
                }
        );

        assertEquals(1, segments.size());
        assertHasSegment(segments, 0.85, 0.5, 0.85, 1.5);
    }

    @Test
    void extractSegmentsCreateJunctionForThreeOwnerCell() {
        RasterDiagramRenderer.OwnershipGrid ownershipGrid = new RasterDiagramRenderer.OwnershipGrid(
                2,
                2,
                new int[] {
                        0, 1,
                        2, 2
                }
        );

        // Three owners in one cell currently produce a small center junction.
        List<SkeletonOverlayRenderer.Segment> segments = SkeletonOverlayRenderer.extractSegments(
                ownershipGrid,
                (point, clusterIndex) -> switch (clusterIndex) {
                    case 0 -> point.x() + point.y();
                    case 1 -> 2.2 - point.x();
                    case 2 -> 2.1 - point.y();
                    default -> 10.0;
                }
        );

        assertEquals(3, segments.size());
        assertHasSegment(segments, 0.85, 0.5, 1.0, 1.0);
        assertHasSegment(segments, 1.5, 1.4, 1.0, 1.0);
        assertHasSegment(segments, 0.5, 0.8, 1.0, 1.0);
    }

    private static void assertHasSegment(
            List<SkeletonOverlayRenderer.Segment> segments,
            double ax,
            double ay,
            double bx,
            double by
    ) {
        Vector expectedA = Vector.xy(ax, ay);
        Vector expectedB = Vector.xy(bx, by);

        boolean found = segments.stream().anyMatch(segment ->
                samePoint(segment.a(), expectedA) && samePoint(segment.b(), expectedB) ||
                samePoint(segment.a(), expectedB) && samePoint(segment.b(), expectedA)
        );

        assertTrue(found);
    }

    private static boolean samePoint(Vector actual, Vector expected) {
        return Math.abs(actual.x() - expected.x()) < 1e-9 &&
                Math.abs(actual.y() - expected.y()) < 1e-9;
    }
}
