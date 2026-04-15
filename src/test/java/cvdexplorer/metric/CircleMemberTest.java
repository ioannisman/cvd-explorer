package cvdexplorer.metric;

import cvdexplorer.model.CircleMember;
import org.junit.jupiter.api.Test;
import xyz.marsavic.geometry.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CircleMemberTest {

    @Test
    void distanceToCircumferenceIsZeroOnRingAndPositiveInsideAndOutside() {
        CircleMember circle = new CircleMember(Vector.xy(10, -5), Vector.xy(13, -5));

        assertEquals(0.0, circle.distanceTo(Vector.xy(10, -2)), 1e-9);
        assertEquals(1.0, circle.distanceTo(Vector.xy(12, -5)), 1e-9);
        assertEquals(2.0, circle.distanceTo(Vector.xy(15, -5)), 1e-9);
    }

    @Test
    void movingCenterPreservesRadiusAndRelativeHandleOffset() {
        CircleMember circle = new CircleMember(Vector.xy(1, 2), Vector.xy(4, 6));

        CircleMember moved = (CircleMember) circle.withHandle(0, Vector.xy(-3, 7));

        assertEquals(-3.0, moved.center().x(), 1e-9);
        assertEquals(7.0, moved.center().y(), 1e-9);
        assertEquals(0.0, moved.radiusHandle().x(), 1e-9);
        assertEquals(11.0, moved.radiusHandle().y(), 1e-9);
        assertEquals(circle.radius(), moved.radius(), 1e-9);
    }

    @Test
    void movingRadiusHandleChangesRadius() {
        CircleMember circle = new CircleMember(Vector.xy(0, 0), Vector.xy(2, 0));

        CircleMember resized = (CircleMember) circle.withHandle(1, Vector.xy(0, 5));

        assertEquals(5.0, resized.radius(), 1e-9);
    }
}
