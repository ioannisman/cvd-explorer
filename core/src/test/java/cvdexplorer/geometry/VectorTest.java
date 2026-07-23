package cvdexplorer.geometry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VectorTest {
    @Test
    void xyAndAccessors() {
        Vector v = Vector.xy(3.5, -2.25);
        assertEquals(3.5, v.x(), 0.0);
        assertEquals(-2.25, v.y(), 0.0);
        assertEquals(3, v.xInt());
        assertEquals(-2, v.yInt());
    }

    @Test
    void polarUsesTurns() {
        Vector unitX = Vector.polar(1.0, 0.0);
        assertEquals(1.0, unitX.x(), 1e-12);
        assertEquals(0.0, unitX.y(), 1e-12);

        Vector unitY = Vector.polar(1.0, 0.25);
        assertEquals(0.0, unitY.x(), 1e-12);
        assertEquals(1.0, unitY.y(), 1e-12);
    }

    @Test
    void arithmeticAndMetrics() {
        Vector a = Vector.xy(3, 4);
        Vector b = Vector.xy(1, -1);
        assertEquals(Vector.xy(4, 3), a.add(b));
        assertEquals(Vector.xy(2, 5), a.sub(b));
        assertEquals(Vector.xy(6, 8), a.mul(2));
        assertEquals(-1.0, a.dot(b), 1e-12);
        assertEquals(25.0, a.lengthSquared(), 1e-12);
        assertEquals(5.0, a.distanceTo(Vector.ZERO), 1e-12);
        assertEquals(Vector.xy(3, 4), Vector.xy(-3, -4).abs());
    }

    @Test
    void roundToGrid() {
        Vector grid = Vector.xy(16, 16);
        assertEquals(Vector.xy(32, -16), Vector.xy(24, -10).round(grid));
    }

    @Test
    void exactEquality() {
        assertTrue(Vector.xy(1.0, 2.0).equals(Vector.xy(1.0, 2.0)));
        assertFalse(Vector.xy(1.0, 2.0).equals(Vector.xy(1.0, 2.0000000001)));
    }
}
