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
                new long[] {
                        1L, 1L, 1L,
                        1L, 1L, 1L
                }
        );

        List<SkeletonOverlayRenderer.Segment> segments = SkeletonOverlayRenderer.extractSegments(ownershipGrid);

        assertTrue(segments.isEmpty());
    }

    @Test
    void extractSegmentsUseEdgeMidpointsForSingleBoundary() {
        RasterDiagramRenderer.OwnershipGrid ownershipGrid = new RasterDiagramRenderer.OwnershipGrid(
                2,
                2,
                new long[] {
                        1L, 2L,
                        1L, 2L
                }
        );

        List<SkeletonOverlayRenderer.Segment> segments = SkeletonOverlayRenderer.extractSegments(ownershipGrid);

        assertEquals(1, segments.size());
        assertHasSegment(segments, 1.0, 0.5, 1.0, 1.5);
    }

    @Test
    void extractSegmentsCreateJunctionForThreeOwnerCell() {
        RasterDiagramRenderer.OwnershipGrid ownershipGrid = new RasterDiagramRenderer.OwnershipGrid(
                2,
                2,
                new long[] {
                        1L, 2L,
                        4L, 4L
                }
        );

        List<SkeletonOverlayRenderer.Segment> segments = SkeletonOverlayRenderer.extractSegments(ownershipGrid);

        assertEquals(3, segments.size());
        assertHasSegment(segments, 1.0, 0.5, 1.0, 1.0);
        assertHasSegment(segments, 1.5, 1.0, 1.0, 1.0);
        assertHasSegment(segments, 0.5, 1.0, 1.0, 1.0);
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
