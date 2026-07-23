package cvdexplorer.render;

import cvdexplorer.core.DiagramRasterizer;
import cvdexplorer.geometry.Box;
import cvdexplorer.geometry.Transformation;
import cvdexplorer.geometry.Vector;
import javafx.scene.image.Image;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

import java.nio.IntBuffer;

/** Builds a JavaFX {@link Image} from {@link DiagramRasterizer} buffers. */
public class RasterDiagramRenderer {
    public record RenderResult(Image image, DiagramRasterizer.OwnershipGrid ownershipGrid) {
    }

    @FunctionalInterface
    public interface Classifier {
        DiagramRasterizer.Classification classify(Vector point);
    }

    @FunctionalInterface
    public interface Colorizer {
        int color(DiagramRasterizer.Classification classification);
    }

    private final DiagramRasterizer rasterizer = new DiagramRasterizer();

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
        DiagramRasterizer.RasterResult result = rasterizer.renderParallel(
                tFromPixels,
                bImage,
                classifier::classify,
                colorizer == null ? null : colorizer::color,
                resolutionScale
        );
        if (result == null) {
            return null;
        }

        Image image = null;
        if (result.argbPixels() != null) {
            PixelFormat<IntBuffer> pixelFormat = PixelFormat.getIntArgbPreInstance();
            IntBuffer buffer = IntBuffer.wrap(result.argbPixels());
            PixelBuffer<IntBuffer> pixelBuffer = new PixelBuffer<>(
                    result.width(),
                    result.height(),
                    buffer,
                    pixelFormat
            );
            pixelBuffer.updateBuffer(pb -> null);
            image = new WritableImage(pixelBuffer);
        }

        return new RenderResult(image, result.ownershipGrid());
    }
}
