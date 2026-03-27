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
                "    Number of clusters, Active cluster, Points in active cluster, Distance shading",
                "",
                "Controls:",
                "    h               - Toggle help",
                "    m               - Cycle MINIMUM_DISTANCE / MAXIMUM_DISTANCE / SUM_OF_DISTANCES",
                "    s               - Toggle distance shading (gadget)",
                "    p               - Toggle cluster members",
                "    d               - Toggle raster diagram",
                "    g               - Toggle snap to grid",
                "    e               - Cycle active cluster (syncs 1-based gadget)",
                "    a               - Add a point to the active cluster (gadget)",
                "    x               - Remove the selected point",
                "    n               - Reset the demo scene",
                "    Mouse left      - Select or drag a cluster point; click empty space to clear",
                "    Ctrl            - Control the view:",
                "      + Mouse left      - Pan",
                "      + Mouse wheel     - Zoom",
                "      + Mouse right     - Reset"
        );
    }
}
