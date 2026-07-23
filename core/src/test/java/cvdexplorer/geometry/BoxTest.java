package cvdexplorer.geometry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BoxTest {
    @Test
    void pqAndDiagonal() {
        Box box = Box.pq(Vector.xy(2, 5), Vector.xy(10, 1));
        assertEquals(Vector.xy(2, 5), box.p());
        assertEquals(Vector.xy(10, 1), box.q());
        assertEquals(Vector.xy(8, -4), box.d());
    }

    @Test
    void positiveOrientsAxes() {
        Box flipped = Box.pq(Vector.xy(10, 1), Vector.xy(2, 5)).positive();
        assertEquals(Vector.xy(2, 1), flipped.p());
        assertEquals(Vector.xy(10, 5), flipped.q());
        assertEquals(Vector.xy(8, 4), flipped.d());
    }

    @Test
    void zeroExtentStaysZeroAfterPositive() {
        Box zero = Box.pq(Vector.ZERO, Vector.ZERO).positive();
        assertEquals(Vector.ZERO, zero.d());
        assertEquals(0, zero.d().abs().xInt());
        assertEquals(0, zero.d().abs().yInt());
    }
}
