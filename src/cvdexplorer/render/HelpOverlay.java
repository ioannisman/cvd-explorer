package cvdexplorer.render;

import xyz.marsavic.drawingfx.drawing.DrawingUtils;
import xyz.marsavic.drawingfx.drawing.View;

public final class HelpOverlay {
    private HelpOverlay() {
    }

    public static void draw(View view) {
        DrawingUtils.drawInfoText(
                view,
                "Cluster Voronoi Explorer",
                "",
                "Gadgets:",
                "    Clusters, Active cluster, Members count, New member type (POINT / LINE_SEGMENT), Metric, Shading",
                "",
                "Controls:",
                "    h               - Toggle help",
                "    m               - Cycle MINIMUM_DISTANCE / MAXIMUM_DISTANCE / SUM_OF_DISTANCES",
                "    s               - Toggle distance shading (gadget)",
                "    p               - Toggle cluster members",
                "    d               - Toggle raster diagram",
                "    k               - Toggle skeleton overlay",
                "    g               - Toggle snap to grid",
                "    e               - Cycle active cluster",
                "    a               - Add member (type from gadget) at pointer",
                "    x               - Remove selected member",
                "    n               - Reset the demo scene",
                "    Mouse left      - Select/drag a handle; empty space clears highlight",
                "    Ctrl            - Control the view:",
                "      + Mouse left      - Pan",
                "      + Mouse wheel     - Zoom",
                "      + Mouse right     - Reset"
        );
    }
}
