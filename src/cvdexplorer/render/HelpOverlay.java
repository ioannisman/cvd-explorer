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
                "Controls:",
                "    h               - Toggle help",
                "    m               - Cycle nearest / farthest metric",
                "    p               - Toggle cluster members",
                "    d               - Toggle raster diagram",
                "    g               - Toggle snap to grid",
                "    e               - Cycle active cluster",
                "    a               - Add a point to the active cluster",
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
