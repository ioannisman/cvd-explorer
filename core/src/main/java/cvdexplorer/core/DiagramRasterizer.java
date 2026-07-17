package cvdexplorer.core;

import xyz.marsavic.geometry.Box;
import xyz.marsavic.geometry.Transformation;
import xyz.marsavic.geometry.Vector;

import java.util.stream.IntStream;

/** Pixel ownership grid and optional ARGB fill for a diagram raster. */
public final class DiagramRasterizer {
    public record Classification(int clusterIndex, double score, int memberIndex) {
    }

    public record OwnershipGrid(int width, int height, int[] clusterIndices, int[] memberIndices) {
        public int clusterIndexAt(int x, int y) {
            return clusterIndices[y * width + x];
        }

        public int memberIndexAt(int x, int y) {
            return memberIndices[y * width + x];
        }
    }

    public record RasterResult(int width, int height, int[] argbPixels, OwnershipGrid ownershipGrid) {
    }

    @FunctionalInterface
    public interface Classifier {
        Classification classify(Vector point);
    }

    @FunctionalInterface
    public interface Colorizer {
        int color(Classification classification);
    }

    private int[] pixels;
    private int[] clusterIndices;
    private int[] memberIndices;
    private int sizeYp = 0;
    private int sizeXp = 0;

    public RasterResult render(
            Transformation tFromPixels,
            Box bImage,
            Classifier classifier,
            Colorizer colorizer
    ) {
        return render(tFromPixels, bImage, classifier, colorizer, 1.0);
    }

    /**
     * Renders at {@code resolutionScale} of the image extent (clamped to (0, 1]).
     * Samples are taken in full pixel-space so {@code tFromPixels} stays correct.
     */
    public RasterResult render(
            Transformation tFromPixels,
            Box bImage,
            Classifier classifier,
            Colorizer colorizer,
            double resolutionScale
    ) {
        Vector diag = bImage.d().abs();
        int fullW = diag.xInt();
        int fullH = diag.yInt();

        if (fullW == 0 || fullH == 0) {
            return null;
        }

        double scale = Math.min(1.0, Math.max(Double.MIN_VALUE, resolutionScale));
        int sizeX = Math.max(1, (int) Math.round(fullW * scale));
        int sizeY = Math.max(1, (int) Math.round(fullH * scale));
        double sx = fullW / (double) sizeX;
        double sy = fullH / (double) sizeY;

        ensureBuffers(sizeX, sizeY);
        IntStream.range(0, sizeY).parallel().forEach(y -> {
            for (int x = 0; x < sizeX; x++) {
                Vector pixelCenter = Vector.xy((x + 0.5) * sx, (y + 0.5) * sy);
                Vector point = tFromPixels.applyTo(pixelCenter);
                Classification classification = classifier.classify(point);
                int index = y * sizeX + x;
                clusterIndices[index] = classification.clusterIndex();
                memberIndices[index] = classification.memberIndex();
                if (colorizer != null) {
                    pixels[index] = colorizer.color(classification);
                }
            }
        });

        OwnershipGrid grid = new OwnershipGrid(sizeX, sizeY, clusterIndices, memberIndices);
        return new RasterResult(sizeX, sizeY, colorizer != null ? pixels : null, grid);
    }

    private void ensureBuffers(int sizeX, int sizeY) {
        if ((sizeYp < sizeY) || (sizeXp < sizeX)) {
            sizeYp = sizeY;
            sizeXp = sizeX;
            pixels = new int[sizeY * sizeX];
            clusterIndices = new int[sizeY * sizeX];
            memberIndices = new int[sizeY * sizeX];
        }
    }
}
