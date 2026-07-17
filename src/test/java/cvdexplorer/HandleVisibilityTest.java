package cvdexplorer;

import cvdexplorer.model.CircleMember;
import cvdexplorer.model.EllipseMember;
import cvdexplorer.model.LineMember;
import cvdexplorer.model.PointMember;
import org.junit.jupiter.api.Test;
import xyz.marsavic.geometry.Vector;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HandleVisibilityTest {

    @Test
    void circleRadiusHandleIsHiddenUntilSelected() {
        CircleMember circle = new CircleMember(Vector.xy(0, 0), Vector.xy(3, 0));

        assertTrue(HandleVisibility.isVisible(circle, 0, false));
        assertFalse(HandleVisibility.isVisible(circle, 1, false));
        assertTrue(HandleVisibility.isVisible(circle, 1, true));
    }

    @Test
    void ellipseSecondaryHandlesAreHiddenUntilSelected() {
        EllipseMember ellipse = new EllipseMember(
                Vector.xy(-5, 0),
                Vector.xy(5, 0),
                Vector.xy(0, 8)
        );

        assertTrue(HandleVisibility.isVisible(ellipse, 0, false));
        assertFalse(HandleVisibility.isVisible(ellipse, 1, false));
        assertFalse(HandleVisibility.isVisible(ellipse, 2, false));
        assertTrue(HandleVisibility.isVisible(ellipse, 1, true));
        assertTrue(HandleVisibility.isVisible(ellipse, 2, true));
    }

    @Test
    void nonEllipseHandlesRemainVisible() {
        PointMember point = new PointMember(Vector.xy(1, 2));

        assertTrue(HandleVisibility.isVisible(point, 0, false));
    }

    @Test
    void lineSecondHandleIsHiddenUntilSelected() {
        LineMember line = new LineMember(Vector.xy(0, 0), Vector.xy(3, 0));

        assertTrue(HandleVisibility.isVisible(line, 0, false));
        assertFalse(HandleVisibility.isVisible(line, 1, false));
        assertTrue(HandleVisibility.isVisible(line, 1, true));
    }
}
