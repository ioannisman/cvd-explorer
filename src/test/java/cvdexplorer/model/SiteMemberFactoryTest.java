package cvdexplorer.model;

import org.junit.jupiter.api.Test;
import xyz.marsavic.geometry.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SiteMemberFactoryTest {

    private static final Vector HINT = Vector.xy(100, -50);

    @Test
    void pointMemberUsesHintAsPosition() {
        ClusterMember m = SiteMemberFactory.createDefault(SiteMemberKind.POINT, 0, 0, HINT);
        PointMember p = assertInstanceOf(PointMember.class, m);
        assertEquals(HINT.x(), p.position().x(), 1e-9);
        assertEquals(HINT.y(), p.position().y(), 1e-9);
    }

    @Test
    void lineSegmentMemberIsCenteredOnHint() {
        ClusterMember m = SiteMemberFactory.createDefault(SiteMemberKind.LINE_SEGMENT, 1, 2, HINT);
        SegmentMember s = assertInstanceOf(SegmentMember.class, m);
        Vector mid = s.a().add(s.b()).mul(0.5);
        assertEquals(HINT.x(), mid.x(), 1e-9);
        assertEquals(HINT.y(), mid.y(), 1e-9);
        assertTrue(s.a().distanceTo(s.b()) > 1e-6);
    }

    @Test
    void circleMemberHasCenterAtHintAndPositiveRadius() {
        ClusterMember m = SiteMemberFactory.createDefault(SiteMemberKind.CIRCLE, 2, 1, HINT);
        CircleMember c = assertInstanceOf(CircleMember.class, m);
        assertEquals(HINT.x(), c.center().x(), 1e-9);
        assertEquals(HINT.y(), c.center().y(), 1e-9);
        assertTrue(c.radius() > 1e-6);
    }

    @Test
    void lineMemberIsCenteredOnHint() {
        ClusterMember m = SiteMemberFactory.createDefault(SiteMemberKind.LINE, 0, 3, HINT);
        LineMember l = assertInstanceOf(LineMember.class, m);
        Vector mid = l.a().add(l.b()).mul(0.5);
        assertEquals(HINT.x(), mid.x(), 1e-9);
        assertEquals(HINT.y(), mid.y(), 1e-9);
        assertTrue(l.a().distanceTo(l.b()) > 1e-6);
    }
}
