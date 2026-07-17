package cvdexplorer.render;

import javafx.scene.image.Image;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import xyz.marsavic.geometry.Box;
import xyz.marsavic.geometry.Transformation;
import xyz.marsavic.geometry.Vector;

import java.nio.IntBuffer;
import java.util.stream.IntStream;

public class RasterDiagramRenderer {
    /**
     * Result of classifying a pixel against all clusters in the scene.
     * @param clusterIndex The index of the winning cluster.
     * @param score The distance or score of the winning cluster.
     * @param memberIndex The index of the specific member within the winning cluster that realized the score (or -1 if aggregate).
     */
    public record Classification(int clusterIndex, double score, int memberIndex) {
    }

    /**
     * Stores the rasterized classification results for the entire image grid.
     */
    public record OwnershipGrid(int width, int height, int[] clusterIndices, int[] memberIndices) {
        public int clusterIndexAt(int x, int y) {
            return clusterIndices[y * width + x];
        }

        public int memberIndexAt(int x, int y) {
            return memberIndices[y * width + x];
        }
    }

    public record RenderResult(Image image, OwnershipGrid ownershipGrid) {
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

    public RenderResult render(
            Transformation tFromPixels,
            Box bImage,
            Classifier classifier,
            Colorizer colorizer
    ) {
        return render(tFromPixels, bImage, classifier, colorizer, 1.0);
    }

    /**
     * Renders the diagram at {@code resolutionScale} of the image extent (clamped to (0, 1]).
     * Samples are taken in full pixel-space so {@code tFromPixels} stays correct; the returned
     * image/grid are smaller and intended to be stretched to {@code bImage} when drawn.
     */
    public RenderResult render(
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

        Image image = null;
        if (colorizer != null) {
            PixelFormat<IntBuffer> pixelFormat = PixelFormat.getIntArgbPreInstance();
            IntBuffer buffer = IntBuffer.wrap(pixels);
            PixelBuffer<IntBuffer> pixelBuffer = new PixelBuffer<>(sizeX, sizeY, buffer, pixelFormat);
            pixelBuffer.updateBuffer(pb -> null);
            image = new WritableImage(pixelBuffer);
        }

        return new RenderResult(image, new OwnershipGrid(sizeX, sizeY, clusterIndices, memberIndices));
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
