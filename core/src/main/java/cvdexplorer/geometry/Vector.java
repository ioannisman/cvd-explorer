package cvdexplorer.geometry;

/**
 * Immutable 2D vector. Angle arguments to {@link #polar(double, double)} are in
 * <em>turns</em> (1 turn = {@code 2π} radians).
 */
public final class Vector {
    public static final Vector ZERO = xy(0.0, 0.0);

    private static final double TAU = 2.0 * Math.PI;

    private final double x;
    private final double y;

    private Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Vector xy(double x, double y) {
        return new Vector(x, y);
    }

    /**
     * Polar construction with angle in turns: {@code (r·cos(φ·2π), r·sin(φ·2π))}.
     */
    public static Vector polar(double r, double angleTurns) {
        return xy(r * Math.cos(angleTurns * TAU), r * Math.sin(angleTurns * TAU));
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public int xInt() {
        return (int) x;
    }

    public int yInt() {
        return (int) y;
    }

    public Vector add(Vector o) {
        return xy(x + o.x, y + o.y);
    }

    public Vector sub(Vector o) {
        return xy(x - o.x, y - o.y);
    }

    public Vector mul(double k) {
        return xy(x * k, y * k);
    }

    public Vector mul(Vector o) {
        return xy(x * o.x, y * o.y);
    }

    public Vector div(Vector o) {
        return xy(x / o.x, y / o.y);
    }

    public Vector abs() {
        return xy(Math.abs(x), Math.abs(y));
    }

    public Vector round() {
        return xy(Math.round(x), Math.round(y));
    }

    /** Snap to a grid with cell size {@code d}: {@code round(this / d) * d}. */
    public Vector round(Vector d) {
        return div(d).round().mul(d);
    }

    public double lengthSquared() {
        return x * x + y * y;
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public double distanceTo(Vector o) {
        return sub(o).length();
    }

    public double dot(Vector o) {
        return x * o.x + y * o.y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Vector other)) {
            return false;
        }
        return x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(x) * 31 + Double.hashCode(y);
    }

    @Override
    public String toString() {
        return "Vector(" + x + ", " + y + ")";
    }
}
