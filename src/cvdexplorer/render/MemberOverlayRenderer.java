package cvdexplorer.render;

import cvdexplorer.HandleVisibility;
import cvdexplorer.model.CircleMember;
import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.SegmentMember;
import javafx.scene.paint.Color;
import xyz.marsavic.drawingfx.drawing.View;
import xyz.marsavic.geometry.Vector;

public final class MemberOverlayRenderer {
    private static final Color SNAP_INDICATOR_COLOR = Color.hsb(60, 0.9, 1.0, 0.8);
    /** Stroke for the selected member’s segment and its active handle (distinct from snap yellow and white edges). */
    private static final Color SELECTION_STROKE = Color.hsb(265, 0.88, 0.52);

    private MemberOverlayRenderer() {
    }

    public static void drawMember(
            View view,
            ClusterMember member,
            Color clusterColor,
            boolean activeCluster,
            boolean memberSelected,
            int selectedHandleIndex,
            double handleRadiusWorld,
            double lineWidthWorld
    ) {
        Color edge = activeCluster ? Color.WHITE : Color.gray(0.1, 0.75);
        double lw = (memberSelected ? 4.0 : activeCluster ? 2.5 : 1.5) * lineWidthWorld;

        if (member instanceof SegmentMember sm) {
            view.setLineWidth(lw);
            view.setStroke(memberSelected ? SELECTION_STROKE : edge);
            view.strokeLineSegment(sm.a(), sm.b());
        } else if (member instanceof CircleMember cm) {
            view.setLineWidth(lw);
            view.setStroke(memberSelected ? SELECTION_STROKE : edge);
            view.strokeCircleCentered(cm.center(), cm.radius());
        }

        view.setLineWidth(lw);
        for (int h = 0; h < member.handleCount(); h++) {
            if (!HandleVisibility.isVisible(member, h, memberSelected)) {
                continue;
            }
            boolean handleSelected = memberSelected && h == selectedHandleIndex;
            view.setFill(clusterColor);
            view.fillCircleCentered(member.getHandle(h), handleRadiusWorld);
            view.setStroke(handleSelected ? SELECTION_STROKE : edge);
            view.strokeCircleCentered(member.getHandle(h), handleRadiusWorld);
        }
    }

    public static void drawSnapIndicator(View view, Vector position, double handleRadiusWorld, double lineWidthWorld) {
        double outerRadius = handleRadiusWorld * 1.8;
        view.setLineWidth(2.5 * lineWidthWorld);
        view.setStroke(SNAP_INDICATOR_COLOR);
        view.strokeCircleCentered(position, outerRadius);
    }
}
