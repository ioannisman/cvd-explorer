package cvdexplorer.metric;

import cvdexplorer.model.EllipseMember;
import org.junit.jupiter.api.Test;
import xyz.marsavic.geometry.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EllipseMemberTest {

    @Test
    void controlPointIsNearZeroDistance() {
        EllipseMember ellipse = new EllipseMember(
                Vector.xy(-10, 0),
                Vector.xy(10, 0),
                Vector.xy(0, 12)
        );

        assertTrue(ellipse.distanceTo(ellipse.controlHandle()) < 1e-6);
    }

    @Test
    void centerDistanceMatchesSemiMinorAxis() {
        double b = Math.sqrt(125);
        EllipseMember ellipse = new EllipseMember(
                Vector.xy(-10, 0),
                Vector.xy(10, 0),
                Vector.xy(0, b)
        );

        assertEquals(b, ellipse.semiMinor(), 1e-9);
        assertEquals(b, ellipse.distanceTo(Vector.xy(0, 0)), 1e-6);
    }

    @Test
    void exteriorPointAlongMajorAxis() {
        double b = Math.sqrt(125);
        EllipseMember ellipse = new EllipseMember(
                Vector.xy(-10, 0),
                Vector.xy(10, 0),
                Vector.xy(0, b)
        );
        // a = 15; point at (20,0) is 5 outside the vertex at (15,0)
        assertEquals(5.0, ellipse.distanceTo(Vector.xy(20, 0)), 1e-6);
    }

    @Test
    void interiorPointPrefersBoundaryOverFarExteriorSite() {
        EllipseMember ellipse = new EllipseMember(
                Vector.xy(-20, 0),
                Vector.xy(20, 0),
                Vector.xy(0, 30)
        );
        Vector interior = Vector.xy(0, 0);
        Vector exteriorSite = Vector.xy(100, 0);

        assertTrue(ellipse.distanceTo(interior) < interior.distanceTo(exteriorSite));
    }

    @Test
    void degenerateCollapsesToSegmentDistance() {
        EllipseMember degenerate = new EllipseMember(
                Vector.xy(-5, 0),
                Vector.xy(5, 0),
                Vector.xy(0, 0)
        );
        assertTrue(degenerate.isDegenerate());
        assertEquals(3.0, degenerate.distanceTo(Vector.xy(0, 3)), 1e-9);
    }

    @Test
    void movingFocusKeepsControl() {
        EllipseMember ellipse = new EllipseMember(
                Vector.xy(-10, 0),
                Vector.xy(10, 0),
                Vector.xy(0, 12)
        );
        EllipseMember moved = (EllipseMember) ellipse.withHandle(0, Vector.xy(-20, 0));
        assertEquals(-20.0, moved.focusA().x(), 1e-9);
        assertEquals(10.0, moved.focusB().x(), 1e-9);
        assertEquals(0.0, moved.controlHandle().x(), 1e-9);
        assertEquals(12.0, moved.controlHandle().y(), 1e-9);
    }

    @Test
    void randomPointsMatchPolylineOracle() {
        EllipseMember ellipse = new EllipseMember(
                Vector.xy(-18, 4),
                Vector.xy(14, -6),
                Vector.xy(2, 28)
        );
        List<Vector> poly = ellipse.boundaryPolyline();
        assertTrue(poly.size() >= 3);

        double[][] samples = {
                {0, 0},
                {30, 0},
                {-30, 5},
                {0, 40},
                {5, -20},
                {ellipse.center().x(), ellipse.center().y()}
        };
        for (double[] xy : samples) {
            Vector p = Vector.xy(xy[0], xy[1]);
            double analytic = ellipse.distanceTo(p);
            double oracle = distanceToClosedPolyline(p, poly);
            assertEquals(oracle, analytic, 0.08, "at " + p);
        }
    }

    private static double distanceToClosedPolyline(Vector point, List<Vector> poly) {
        double min = Double.POSITIVE_INFINITY;
        int n = poly.size();
        for (int i = 0; i < n; i++) {
            Vector a = poly.get(i);
            Vector b = poly.get((i + 1) % n);
            double apx = point.x() - a.x();
            double apy = point.y() - a.y();
            double abx = b.x() - a.x();
            double aby = b.y() - a.y();
            double ab2 = abx * abx + aby * aby;
            double t = ab2 <= 0 ? 0 : Math.max(0, Math.min(1, (apx * abx + apy * aby) / ab2));
            double cx = a.x() + t * abx;
            double cy = a.y() + t * aby;
            double dx = point.x() - cx;
            double dy = point.y() - cy;
            min = Math.min(min, Math.sqrt(dx * dx + dy * dy));
        }
        return min;
    }
}
