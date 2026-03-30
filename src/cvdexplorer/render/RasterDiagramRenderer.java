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
    public record Classification(int clusterIndex, double score) {
    }

    public record OwnershipGrid(int width, int height, int[] clusterIndices) {
        public int clusterIndexAt(int x, int y) {
            return clusterIndices[y * width + x];
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
    private int sizeYp = 0;
    private int sizeXp = 0;

    public RenderResult render(
            Transformation tFromPixels,
            Box bImage,
            Classifier classifier,
            Colorizer colorizer
    ) {
        Vector diag = bImage.d().abs();
        int sizeX = diag.xInt();
        int sizeY = diag.yInt();

        if (sizeX == 0 || sizeY == 0) {
            return null;
        }

        ensureBuffers(sizeX, sizeY);
        IntStream.range(0, sizeY).parallel().forEach(y -> {
            for (int x = 0; x < sizeX; x++) {
                Vector pixelCenter = Vector.xy(x + 0.5, y + 0.5);
                Vector point = tFromPixels.applyTo(pixelCenter);
                Classification classification = classifier.classify(point);
                int index = y * sizeX + x;
                clusterIndices[index] = classification.clusterIndex();
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

        return new RenderResult(image, new OwnershipGrid(sizeX, sizeY, clusterIndices));
    }

    private void ensureBuffers(int sizeX, int sizeY) {
        if ((sizeYp < sizeY) || (sizeXp < sizeX)) {
            sizeYp = sizeY;
            sizeXp = sizeX;
            pixels = new int[sizeY * sizeX];
            clusterIndices = new int[sizeY * sizeX];
        }
    }
}
