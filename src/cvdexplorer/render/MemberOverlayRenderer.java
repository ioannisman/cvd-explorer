package cvdexplorer.render;

import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.SegmentMember;
import javafx.scene.paint.Color;
import xyz.marsavic.drawingfx.drawing.View;

public final class MemberOverlayRenderer {
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
            view.setStroke(memberSelected ? Color.BLACK : edge);
            view.strokeLineSegment(sm.a(), sm.b());
        }

        view.setLineWidth(lw);
        for (int h = 0; h < member.handleCount(); h++) {
            boolean handleSelected = memberSelected && h == selectedHandleIndex;
            view.setFill(clusterColor);
            view.fillCircleCentered(member.getHandle(h), handleRadiusWorld);
            view.setStroke(handleSelected ? Color.BLACK : edge);
            view.strokeCircleCentered(member.getHandle(h), handleRadiusWorld);
        }
    }
}
