package cvdexplorer.render;

import javafx.scene.image.Image;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

import java.nio.IntBuffer;

public final class SkeletonOverlayRenderer {
    private static final int SKELETON_ARGB = 0xCC000000;

    public record BoundaryMask(int width, int height, boolean[] boundaryPixels) {
        public boolean boundaryAt(int x, int y) {
            return boundaryPixels[y * width + x];
        }
    }

    private int[] pixels;
    private int sizeYp = 0;
    private int sizeXp = 0;

    public Image render(RasterDiagramRenderer.OwnershipGrid ownershipGrid) {
        BoundaryMask boundaryMask = extractBoundaryMask(ownershipGrid);
        ensureBuffers(boundaryMask.width(), boundaryMask.height());

        int pixelCount = boundaryMask.width() * boundaryMask.height();
        for (int i = 0; i < pixelCount; i++) {
            pixels[i] = boundaryMask.boundaryPixels()[i] ? SKELETON_ARGB : 0x00000000;
        }

        PixelFormat<IntBuffer> pixelFormat = PixelFormat.getIntArgbPreInstance();
        IntBuffer buffer = IntBuffer.wrap(pixels);
        PixelBuffer<IntBuffer> pixelBuffer = new PixelBuffer<>(boundaryMask.width(), boundaryMask.height(), buffer, pixelFormat);
        pixelBuffer.updateBuffer(pb -> null);
        return new WritableImage(pixelBuffer);
    }

    static BoundaryMask extractBoundaryMask(RasterDiagramRenderer.OwnershipGrid ownershipGrid) {
        int width = ownershipGrid.width();
        int height = ownershipGrid.height();
        boolean[] boundaryPixels = new boolean[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int owner = ownershipGrid.clusterIndexAt(x, y);
                boolean boundary =
                        (x > 0 && ownershipGrid.clusterIndexAt(x - 1, y) != owner) ||
                        (x + 1 < width && ownershipGrid.clusterIndexAt(x + 1, y) != owner) ||
                        (y > 0 && ownershipGrid.clusterIndexAt(x, y - 1) != owner) ||
                        (y + 1 < height && ownershipGrid.clusterIndexAt(x, y + 1) != owner);
                boundaryPixels[y * width + x] = boundary;
            }
        }

        return new BoundaryMask(width, height, boundaryPixels);
    }

    private void ensureBuffers(int sizeX, int sizeY) {
        if ((sizeYp < sizeY) || (sizeXp < sizeX)) {
            sizeYp = sizeY;
            sizeXp = sizeX;
            pixels = new int[sizeY * sizeX];
        }
    }
}
