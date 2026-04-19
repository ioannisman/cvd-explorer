package cvdexplorer.metric;

import cvdexplorer.model.LineMember;
import org.junit.jupiter.api.Test;
import xyz.marsavic.geometry.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LineMemberTest {

    @Test
    void distanceToDegenerateLineIsPointDistance() {
        Vector p = Vector.xy(3, 4);
        LineMember line = new LineMember(Vector.xy(0, 0), Vector.xy(0, 0));

        assertEquals(5.0, line.distanceTo(p), 1e-9);
    }

    @Test
    void distanceUsesInfiniteLineInsteadOfClampingToHandles() {
        LineMember line = new LineMember(Vector.xy(0, 0), Vector.xy(10, 0));

        assertEquals(4.0, line.distanceTo(Vector.xy(-1, 4)), 1e-9);
        assertEquals(4.0, line.distanceTo(Vector.xy(11, 4)), 1e-9);
    }
}
