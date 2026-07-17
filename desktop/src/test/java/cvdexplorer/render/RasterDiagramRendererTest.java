package cvdexplorer.render;

import cvdexplorer.core.DiagramRasterizer;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.PointMember;
import cvdexplorer.model.Rgba;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
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
                p -> new DiagramRasterizer.Classification(0, 0.0, 0),
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
                p -> new DiagramRasterizer.Classification((int) Math.floor(p.x()) % 2, 0.0, 0),
                null
        );

        assertNotNull(result);
        assertNull(result.image());
        DiagramRasterizer.OwnershipGrid grid = result.ownershipGrid();
        assertEquals(2, grid.width());
        assertEquals(2, grid.height());
        assertEquals(0, grid.clusterIndexAt(0, 0));
        assertEquals(1, grid.clusterIndexAt(1, 0));
    }

    @Test
    void renderWithClusterColorizerWritesArgbPixels() throws Exception {
        RasterDiagramRenderer renderer = new RasterDiagramRenderer();
        Box box = Box.pq(Vector.xy(0, 0), Vector.xy(2, 2)).positive();
        Rgba background = Rgba.gray(0.92);
        List<ClusterSite> clusters = List.of(
                new ClusterSite("A", Rgba.RED, List.of(new PointMember(Vector.xy(0, 0)))),
                new ClusterSite("B", Rgba.BLUE, List.of(new PointMember(Vector.xy(1, 0))))
        );
        ClusterColorizer clusterColorizer = new ClusterColorizer(clusters, background, false);

        CompletableFuture<Void> done = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                RasterDiagramRenderer.RenderResult result = renderer.render(
                        Transformation.IDENTITY.inverse(),
                        box,
                        p -> new DiagramRasterizer.Classification(p.x() < 1.0 ? 0 : 1, 0.0, 0),
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
                assertEquals(left, clusterColorizer.color(new DiagramRasterizer.Classification(0, 0.0, 0)));
                assertEquals(right, clusterColorizer.color(new DiagramRasterizer.Classification(1, 0.0, 0)));
                done.complete(null);
            } catch (Throwable t) {
                done.completeExceptionally(t);
            }
        });
        done.get(10, TimeUnit.SECONDS);
    }

    @Test
    void renderWithHalfResolutionProducesSmallerGridAndImage() throws Exception {
        RasterDiagramRenderer renderer = new RasterDiagramRenderer();
        Box box = Box.pq(Vector.xy(0, 0), Vector.xy(4, 4)).positive();

        CompletableFuture<Void> done = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                RasterDiagramRenderer.RenderResult result = renderer.render(
                        Transformation.IDENTITY.inverse(),
                        box,
                        p -> new DiagramRasterizer.Classification(0, 0.0, 0),
                        c -> 0xff000000,
                        0.5
                );
                assertNotNull(result);
                DiagramRasterizer.OwnershipGrid grid = result.ownershipGrid();
                assertEquals(2, grid.width());
                assertEquals(2, grid.height());
                Image image = result.image();
                assertNotNull(image);
                assertEquals(2, (int) image.getWidth());
                assertEquals(2, (int) image.getHeight());
                done.complete(null);
            } catch (Throwable t) {
                done.completeExceptionally(t);
            }
        });
        done.get(10, TimeUnit.SECONDS);
    }

    @Test
    void renderWithFullResolutionScaleMatchesDefaultRenderSize() {
        RasterDiagramRenderer renderer = new RasterDiagramRenderer();
        Box box = Box.pq(Vector.xy(0, 0), Vector.xy(4, 4)).positive();

        RasterDiagramRenderer.RenderResult scaled = renderer.render(
                Transformation.IDENTITY.inverse(),
                box,
                p -> new DiagramRasterizer.Classification(0, 0.0, 0),
                null,
                1.0
        );
        RasterDiagramRenderer.RenderResult defaults = renderer.render(
                Transformation.IDENTITY.inverse(),
                box,
                p -> new DiagramRasterizer.Classification(0, 0.0, 0),
                null
        );

        assertNotNull(scaled);
        assertNotNull(defaults);
        assertEquals(defaults.ownershipGrid().width(), scaled.ownershipGrid().width());
        assertEquals(defaults.ownershipGrid().height(), scaled.ownershipGrid().height());
        assertEquals(4, scaled.ownershipGrid().width());
        assertEquals(4, scaled.ownershipGrid().height());
    }

    @Test
    void clusterColorizerUsesBackgroundForNegativeClusterIndex() {
        Rgba background = new Rgba(0.1, 0.2, 0.3, 0.5);
        ClusterColorizer colorizer = new ClusterColorizer(
                List.of(new ClusterSite("A", Rgba.RED, List.of(new PointMember(Vector.xy(0, 0))))),
                background,
                false
        );
        int argb = colorizer.color(new DiagramRasterizer.Classification(-1, 42.0, -1));
        assertEquals(background.toArgb(), argb);
    }
}
