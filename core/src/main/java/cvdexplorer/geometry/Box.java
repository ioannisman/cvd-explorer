package cvdexplorer.geometry;

/**
 * Axis-aligned rectangle defined by opposite corners {@code p} and {@code q}.
 * {@link #positive()} orients each axis so {@code p ≤ q}; {@link #d()} is the diagonal extent.
 */
public final class Box {
    private final double px;
    private final double py;
    private final double qx;
    private final double qy;

    private Box(double px, double py, double qx, double qy) {
        this.px = px;
        this.py = py;
        this.qx = qx;
        this.qy = qy;
    }

    public static Box pq(Vector p, Vector q) {
        return new Box(p.x(), p.y(), q.x(), q.y());
    }

    public Vector p() {
        return Vector.xy(px, py);
    }

    public Vector q() {
        return Vector.xy(qx, qy);
    }

    /** Diagonal extent {@code (qx - px, qy - py)}. */
    public Vector d() {
        return Vector.xy(qx - px, qy - py);
    }

    /** Orient each axis so the interval is non-decreasing ({@code p ≤ q} per axis). */
    public Box positive() {
        return pq(
                Vector.xy(Math.min(px, qx), Math.min(py, qy)),
                Vector.xy(Math.max(px, qx), Math.max(py, qy))
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Box other)) {
            return false;
        }
        return px == other.px && py == other.py && qx == other.qx && qy == other.qy;
    }

    @Override
    public int hashCode() {
        int h = Double.hashCode(px);
        h = 31 * h + Double.hashCode(py);
        h = 31 * h + Double.hashCode(qx);
        h = 31 * h + Double.hashCode(qy);
        return h;
    }

    @Override
    public String toString() {
        return "Box[" + p() + " → " + q() + "]";
    }
}
