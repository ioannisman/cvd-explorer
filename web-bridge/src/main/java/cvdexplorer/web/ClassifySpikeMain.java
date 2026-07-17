package cvdexplorer.web;

import cvdexplorer.core.DiagramRasterizer;
import cvdexplorer.core.ScenePreparation;
import cvdexplorer.core.ScenePreparation.PreparedScene;
import cvdexplorer.metric.MetricKind;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.EllipseMember;
import cvdexplorer.model.NeighborOrder;
import cvdexplorer.model.PointMember;
import cvdexplorer.model.Rgba;
import cvdexplorer.model.SceneSnapshot;
import cvdexplorer.render.ClusterColorizer;
import org.teavm.jso.JSBody;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import xyz.marsavic.geometry.Box;
import xyz.marsavic.geometry.Transformation;
import xyz.marsavic.geometry.Vector;

import java.util.List;

/**
 * TeaVM spike entry: rasterize a tiny hardcoded scene and report to the page.
 */
public final class ClassifySpikeMain {
    private ClassifySpikeMain() {
    }

    public static void main(String[] args) {
        try {
            StringBuilder log = new StringBuilder();
            log.append("TeaVM spike starting…\n");

            SceneSnapshot hardcoded = hardcodedScene();
            RasterStats hardStats = rasterize(hardcoded, 64, 64);
            log.append("hardcoded: ").append(hardStats).append('\n');

            // JSON path deferred until Gson/TeaVM strategy is chosen; geometry+raster only for this step.
            log.append("OK: mars-bits geometry + sequential DiagramRasterizer\n");
            report(log.toString(), true);
        } catch (Throwable t) {
            String msg = "FAIL: " + t.getClass().getName() + ": " + t.getMessage();
            report(msg, false);
            throw new RuntimeException(t);
        }
    }

    private static SceneSnapshot hardcodedScene() {
        SceneSnapshot snapshot = new SceneSnapshot();
        snapshot.setMetricKind(MetricKind.MINIMUM_DISTANCE);
        snapshot.setNeighborOrder(NeighborOrder.NEAREST);
        snapshot.setClusters(List.of(
                new ClusterSite(
                        "Ellipse",
                        Rgba.hsb(30, 0.75, 1.0),
                        List.of(new EllipseMember(
                                Vector.xy(-40, 0),
                                Vector.xy(40, 0),
                                Vector.xy(0, 30)
                        ))
                ),
                new ClusterSite(
                        "Point",
                        Rgba.hsb(210, 0.75, 0.95),
                        List.of(new PointMember(Vector.xy(80, 40)))
                )
        ));
        return snapshot;
    }

    private static RasterStats rasterize(SceneSnapshot snapshot, int width, int height) {
        PreparedScene prepared = ScenePreparation.prepare(
                snapshot.clusters(),
                snapshot.metricKind(),
                snapshot.neighborOrder(),
                snapshot.nearestNeighborK()
        );
        ClusterColorizer colorizer = new ClusterColorizer(
                prepared.clusters(),
                Rgba.gray(0.92),
                false
        );
        DiagramRasterizer rasterizer = new DiagramRasterizer();
        Box imageBox = Box.pq(Vector.ZERO, Vector.xy(width, height)).positive();
        // Identity world≈pixel for the spike; only need a non-empty raster.
        DiagramRasterizer.RasterResult result = rasterizer.render(
                Transformation.IDENTITY,
                imageBox,
                point -> {
                    var ownership = prepared.ownershipSelector().selectOwner(
                            point,
                            prepared.clusters(),
                            prepared.metric()
                    );
                    return new DiagramRasterizer.Classification(
                            ownership.clusterIndex(),
                            ownership.score(),
                            ownership.memberIndex()
                    );
                },
                colorizer::color,
                1.0
        );
        if (result == null || result.argbPixels() == null) {
            throw new IllegalStateException("rasterizer returned null");
        }
        long checksum = 0;
        for (int pixel : result.argbPixels()) {
            checksum = (checksum * 31) + (pixel & 0xffffffffL);
        }
        int owners = 0;
        for (int c : result.ownershipGrid().clusterIndices()) {
            if (c >= 0) {
                owners++;
            }
        }
        return new RasterStats(result.width(), result.height(), owners, checksum);
    }

    private static void report(String text, boolean ok) {
        consoleLog(text);
        HTMLDocument doc = Window.current().getDocument();
        HTMLElement pre = doc.createElement("pre");
        pre.setAttribute("id", "spike-result");
        pre.setAttribute("data-ok", ok ? "true" : "false");
        pre.appendChild(doc.createTextNode(text));
        doc.getBody().appendChild(pre);
    }

    @JSBody(params = { "message" }, script = "console.log(message);")
    private static native void consoleLog(String message);

    private record RasterStats(int width, int height, int ownedPixels, long checksum) {
        @Override
        public String toString() {
            return width + "x" + height + " owned=" + ownedPixels + " checksum=" + checksum;
        }
    }
}
