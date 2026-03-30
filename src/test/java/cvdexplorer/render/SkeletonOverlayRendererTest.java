package cvdexplorer.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkeletonOverlayRendererTest {

    @Test
    void extractBoundaryMaskLeavesUniformOwnershipEmpty() {
        RasterDiagramRenderer.OwnershipGrid ownershipGrid = new RasterDiagramRenderer.OwnershipGrid(
                3,
                2,
                new int[] {
                        0, 0, 0,
                        0, 0, 0
                }
        );

        SkeletonOverlayRenderer.BoundaryMask boundaryMask = SkeletonOverlayRenderer.extractBoundaryMask(ownershipGrid);

        for (int y = 0; y < boundaryMask.height(); y++) {
            for (int x = 0; x < boundaryMask.width(); x++) {
                assertFalse(boundaryMask.boundaryAt(x, y));
            }
        }
    }

    @Test
    void extractBoundaryMaskMarksPixelsAdjacentToOwnershipChange() {
        RasterDiagramRenderer.OwnershipGrid ownershipGrid = new RasterDiagramRenderer.OwnershipGrid(
                3,
                3,
                new int[] {
                        0, 0, 0,
                        0, 1, 0,
                        0, 0, 0
                }
        );

        SkeletonOverlayRenderer.BoundaryMask boundaryMask = SkeletonOverlayRenderer.extractBoundaryMask(ownershipGrid);

        assertFalse(boundaryMask.boundaryAt(0, 0));
        assertTrue(boundaryMask.boundaryAt(1, 0));
        assertFalse(boundaryMask.boundaryAt(2, 0));

        assertTrue(boundaryMask.boundaryAt(0, 1));
        assertTrue(boundaryMask.boundaryAt(1, 1));
        assertTrue(boundaryMask.boundaryAt(2, 1));

        assertFalse(boundaryMask.boundaryAt(0, 2));
        assertTrue(boundaryMask.boundaryAt(1, 2));
        assertFalse(boundaryMask.boundaryAt(2, 2));
    }
}
