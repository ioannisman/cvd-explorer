package cvdexplorer.model;

import cvdexplorer.geometry.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Classic 2-ellipse (ellipse with two foci): boundary is {@code d(p,f₁)+d(p,f₂)=c}.
 * Distance is Euclidean distance to that boundary (analytic Newton). {@code c} comes from the control handle.
 */
public final class EllipseMember implements ClusterMember {
    private static final int DRAW_SAMPLES = 96;
    private static final double DEGENERATE_EPS = 1e-9;

    private final Vector focusA;
    private final Vector focusB;
    private final Vector controlHandle;

    /** Cached frame for fast per-pixel distance / drawing. */
    private final boolean degenerate;
    private final double c;
    private final double a;
    private final double b;
    private final double centerX;
    private final double centerY;
    private final double cos;
    private final double sin;

    public EllipseMember(Vector focusA, Vector focusB, Vector controlHandle) {
        this.focusA = focusA;
        this.focusB = focusB;
        this.controlHandle = controlHandle;

        this.c = focusA.distanceTo(controlHandle) + focusB.distanceTo(controlHandle);
        double focusDist = focusA.distanceTo(focusB);
        this.degenerate = c <= focusDist + DEGENERATE_EPS;
        this.a = 0.5 * c;
        double focusHalf = 0.5 * focusDist;
        double bSq = a * a - focusHalf * focusHalf;
        this.b = (!degenerate && bSq > 0.0) ? Math.sqrt(bSq) : Double.NaN;
        this.centerX = 0.5 * (focusA.x() + focusB.x());
        this.centerY = 0.5 * (focusA.y() + focusB.y());
        double angle = Math.atan2(focusB.y() - focusA.y(), focusB.x() - focusA.x());
        this.cos = Math.cos(angle);
        this.sin = Math.sin(angle);
    }

    public Vector focusA() {
        return focusA;
    }

    public Vector focusB() {
        return focusB;
    }

    public Vector controlHandle() {
        return controlHandle;
    }

    /** Level-set constant {@code d(h,f₁)+d(h,f₂)}. */
    public double c() {
        return c;
    }

    public double focusDistance() {
        return focusA.distanceTo(focusB);
    }

    public boolean isDegenerate() {
        return degenerate;
    }

    /** Semi-major axis; NaN if degenerate. */
    public double semiMajor() {
        return degenerate ? Double.NaN : a;
    }

    /** Semi-minor axis; NaN if degenerate. */
    public double semiMinor() {
        return degenerate ? Double.NaN : b;
    }

    public Vector center() {
        return Vector.xy(centerX, centerY);
    }

    /** Angle of the major axis (focusA → focusB). */
    public double majorAxisAngle() {
        return Math.atan2(focusB.y() - focusA.y(), focusB.x() - focusA.x());
    }

    /** Parametric samples of the boundary for overlay drawing (empty if degenerate). */
    public List<Vector> boundaryPolyline() {
        if (degenerate) {
            return List.of();
        }
        List<Vector> points = new ArrayList<>(DRAW_SAMPLES);
        for (int i = 0; i < DRAW_SAMPLES; i++) {
            double theta = 2 * Math.PI * i / DRAW_SAMPLES;
            double lx = a * Math.cos(theta);
            double ly = b * Math.sin(theta);
            points.add(Vector.xy(
                    centerX + lx * cos - ly * sin,
                    centerY + lx * sin + ly * cos
            ));
        }
        return points;
    }

    @Override
    public double distanceTo(Vector point) {
        if (degenerate) {
            return distanceToSegment(point, focusA, focusB);
        }
        double dx = point.x() - centerX;
        double dy = point.y() - centerY;
        double localX = dx * cos + dy * sin;
        double localY = -dx * sin + dy * cos;
        return EllipseDistance.distanceAxisAligned(localX, localY, a, b);
    }

    private static double distanceToSegment(Vector point, Vector a, Vector b) {
        double apx = point.x() - a.x();
        double apy = point.y() - a.y();
        double abx = b.x() - a.x();
        double aby = b.y() - a.y();
        double ab2 = abx * abx + aby * aby;
        if (ab2 <= 0) {
            return point.distanceTo(a);
        }
        double t = (apx * abx + apy * aby) / ab2;
        t = Math.max(0.0, Math.min(1.0, t));
        double cx = a.x() + t * abx;
        double cy = a.y() + t * aby;
        double dx = point.x() - cx;
        double dy = point.y() - cy;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public int handleCount() {
        return 3;
    }

    @Override
    public Vector getHandle(int index) {
        return switch (index) {
            case 0 -> focusA;
            case 1 -> focusB;
            case 2 -> controlHandle;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    @Override
    public ClusterMember withHandle(int index, Vector v) {
        return switch (index) {
            case 0 -> new EllipseMember(v, focusB, controlHandle);
            case 1 -> new EllipseMember(focusA, v, controlHandle);
            case 2 -> new EllipseMember(focusA, focusB, v);
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    @Override
    public Vector placementCentroid() {
        return center();
    }

    @Override
    public Vector anchor() {
        return focusA;
    }
}
