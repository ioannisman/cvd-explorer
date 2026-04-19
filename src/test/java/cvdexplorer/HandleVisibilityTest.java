package cvdexplorer;

import cvdexplorer.model.CircleMember;
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
    void nonCircleHandlesRemainVisible() {
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
