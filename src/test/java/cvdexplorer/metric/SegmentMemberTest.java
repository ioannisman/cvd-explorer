package cvdexplorer.metric;

import cvdexplorer.model.SegmentMember;
import org.junit.jupiter.api.Test;
import xyz.marsavic.geometry.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SegmentMemberTest {

    @Test
    void distanceToDegenerateSegmentIsPointDistance() {
        Vector p = Vector.xy(3, 4);
        SegmentMember seg = new SegmentMember(Vector.xy(0, 0), Vector.xy(0, 0));
        assertEquals(5.0, seg.distanceTo(p), 1e-9);
    }

    @Test
    void distanceToInteriorOfSegmentIsPerpendicular() {
        SegmentMember seg = new SegmentMember(Vector.xy(0, 0), Vector.xy(10, 0));
        assertEquals(8.0, seg.distanceTo(Vector.xy(6, 8)), 1e-9);
    }

    @Test
    void distanceClampsToEndpoints() {
        SegmentMember seg = new SegmentMember(Vector.xy(0, 0), Vector.xy(10, 0));
        assertEquals(Math.sqrt(17), seg.distanceTo(Vector.xy(-1, 4)), 1e-9);
        assertEquals(Math.sqrt(17), seg.distanceTo(Vector.xy(11, 4)), 1e-9);
    }
}
