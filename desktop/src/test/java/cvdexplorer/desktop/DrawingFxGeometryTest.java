package cvdexplorer.desktop;

import cvdexplorer.geometry.Box;
import cvdexplorer.geometry.Transformation;
import cvdexplorer.geometry.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DrawingFxGeometryTest {
    @Test
    void vectorRoundTrip() {
        Vector core = Vector.xy(12.5, -3.25);
        assertEquals(core, DrawingFxGeometry.fromDrawingFx(DrawingFxGeometry.toDrawingFx(core)));
    }

    @Test
    void boxRoundTrip() {
        Box core = Box.pq(Vector.xy(0, 10), Vector.xy(40, -2)).positive();
        Box back = DrawingFxGeometry.fromDrawingFx(DrawingFxGeometry.toDrawingFx(core));
        assertEquals(core.p(), back.p());
        assertEquals(core.q(), back.q());
        assertEquals(core.d(), back.d());
    }

    @Test
    void transformationRoundTripAndApply() {
        Transformation core = Transformation.scaling(2, -0.5)
                .then(Transformation.translation(Vector.xy(3, 7)));
        Transformation back = DrawingFxGeometry.fromDrawingFx(DrawingFxGeometry.toDrawingFx(core));
        Vector p = Vector.xy(11, -4);
        assertEquals(core.applyTo(p), back.applyTo(p));

        xyz.marsavic.geometry.Vector fxP = xyz.marsavic.geometry.Vector.xy(11, -4);
        xyz.marsavic.geometry.Vector fxOut = DrawingFxGeometry.toDrawingFx(core).applyTo(fxP);
        assertEquals(core.applyTo(p).x(), fxOut.x(), 1e-12);
        assertEquals(core.applyTo(p).y(), fxOut.y(), 1e-12);
    }
}
