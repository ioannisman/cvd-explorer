package cvdexplorer.render;

import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.PointMember;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import xyz.marsavic.geometry.Box;
import xyz.marsavic.geometry.Transformation;
import xyz.marsavic.geometry.Vector;

import java.nio.IntBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RasterDiagramRendererTest {

    @BeforeAll
    static void startJavaFxRuntime() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Toolkit already initialized
        }
    }

    @Test
    void renderReturnsNullWhenImageExtentIsZero() {
        RasterDiagramRenderer renderer = new RasterDiagramRenderer();
        Box emptyExtent = Box.pq(Vector.xy(0, 0), Vector.xy(0, 0)).positive();

        RasterDiagramRenderer.RenderResult result = renderer.render(
                Transformation.IDENTITY.inverse(),
                emptyExtent,
                p -> new RasterDiagramRenderer.Classification(0, 0.0),
                c -> 0xff000000
        );

        assertNull(result);
    }

    @Test
    void renderWithNullColorizerFillsOwnershipGridButImageIsNull() {
        RasterDiagramRenderer renderer = new RasterDiagramRenderer();
        Box box = Box.pq(Vector.xy(0, 0), Vector.xy(2, 2)).positive();

        RasterDiagramRenderer.RenderResult result = renderer.render(
                Transformation.IDENTITY.inverse(),
                box,
                p -> new RasterDiagramRenderer.Classification((int) Math.floor(p.x()) % 2, 0.0),
                null
        );

        assertNotNull(result);
        assertNull(result.image());
        RasterDiagramRenderer.OwnershipGrid grid = result.ownershipGrid();
        assertEquals(2, grid.width());
        assertEquals(2, grid.height());
        assertEquals(0, grid.clusterIndexAt(0, 0));
        assertEquals(1, grid.clusterIndexAt(1, 0));
    }

    @Test
    void renderWithClusterColorizerWritesArgbPixels() throws Exception {
        RasterDiagramRenderer renderer = new RasterDiagramRenderer();
        Box box = Box.pq(Vector.xy(0, 0), Vector.xy(2, 2)).positive();
        Color background = Color.gray(0.92);
        List<ClusterSite> clusters = List.of(
                new ClusterSite("A", Color.RED, List.of(new PointMember(Vector.xy(0, 0)))),
                new ClusterSite("B", Color.BLUE, List.of(new PointMember(Vector.xy(1, 0))))
        );
        ClusterColorizer clusterColorizer = new ClusterColorizer(clusters, background, false);

        CompletableFuture<Void> done = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                RasterDiagramRenderer.RenderResult result = renderer.render(
                        Transformation.IDENTITY.inverse(),
                        box,
                        p -> new RasterDiagramRenderer.Classification(p.x() < 1.0 ? 0 : 1, 0.0),
                        clusterColorizer::color
                );
                assertNotNull(result);
                Image image = result.image();
                assertNotNull(image);
                int w = (int) image.getWidth();
                int h = (int) image.getHeight();
                assertEquals(2, w);
                assertEquals(2, h);
                int[] row = new int[w];
                IntBuffer buf = IntBuffer.wrap(row);
                image.getPixelReader().getPixels(0, 0, w, 1, PixelFormat.getIntArgbInstance(), buf, w);
                int left = row[0];
                int right = row[1];
                assertEquals(left, clusterColorizer.color(new RasterDiagramRenderer.Classification(0, 0.0)));
                assertEquals(right, clusterColorizer.color(new RasterDiagramRenderer.Classification(1, 0.0)));
                done.complete(null);
            } catch (Throwable t) {
                done.completeExceptionally(t);
            }
        });
        done.get(10, TimeUnit.SECONDS);
    }

    @Test
    void clusterColorizerUsesBackgroundForNegativeClusterIndex() {
        Color background = Color.color(0.1, 0.2, 0.3, 0.5);
        ClusterColorizer colorizer = new ClusterColorizer(
                List.of(new ClusterSite("A", Color.RED, List.of(new PointMember(Vector.xy(0, 0))))),
                background,
                false
        );
        int argb = colorizer.color(new RasterDiagramRenderer.Classification(-1, 42.0));
        int expectedA = (int) Math.round(background.getOpacity() * 255.0);
        int expectedR = (int) Math.round(background.getRed() * 255.0);
        int expectedG = (int) Math.round(background.getGreen() * 255.0);
        int expectedB = (int) Math.round(background.getBlue() * 255.0);
        int expected = (expectedA << 24) | (expectedR << 16) | (expectedG << 8) | expectedB;
        assertEquals(expected, argb);
    }
}
