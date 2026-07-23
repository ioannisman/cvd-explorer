package cvdexplorer.geometry;

/**
 * Immutable 2D affine transform
 * <pre>
 *   [ mex  mfx  tx ]
 *   [ mey  mfy  ty ]
 *   [ 0    0    1  ]
 * </pre>
 * Composition: {@code a.then(b) == b.applyAfter(a)}, i.e. {@code a.then(b)} applies {@code a} first.
 */
public final class Transformation {
    public static final Transformation IDENTITY = new Transformation(1, 0, 0, 0, 1, 0);

    private final double mex;
    private final double mfx;
    private final double tx;
    private final double mey;
    private final double mfy;
    private final double ty;

    public Transformation(double mex, double mfx, double tx, double mey, double mfy, double ty) {
        this.mex = mex;
        this.mfx = mfx;
        this.tx = tx;
        this.mey = mey;
        this.mfy = mfy;
        this.ty = ty;
    }

    public double mex() {
        return mex;
    }

    public double mfx() {
        return mfx;
    }

    public double tx() {
        return tx;
    }

    public double mey() {
        return mey;
    }

    public double mfy() {
        return mfy;
    }

    public double ty() {
        return ty;
    }

    public static Transformation scaling(double sx, double sy) {
        return new Transformation(sx, 0, 0, 0, sy, 0);
    }

    public static Transformation translation(Vector v) {
        return new Transformation(1, 0, v.x(), 0, 1, v.y());
    }

    /** Apply {@code this} after {@code t}: result(v) = this(t(v)). */
    public Transformation applyAfter(Transformation t) {
        return new Transformation(
                mex * t.mex + mfx * t.mey,
                mex * t.mfx + mfx * t.mfy,
                mex * t.tx + mfx * t.ty + tx,
                mey * t.mex + mfy * t.mey,
                mey * t.mfx + mfy * t.mfy,
                mey * t.tx + mfy * t.ty + ty
        );
    }

    /** Apply {@code t} after {@code this}: result(v) = t(this(v)). */
    public Transformation then(Transformation t) {
        return t.applyAfter(this);
    }

    public Vector applyTo(Vector v) {
        return Vector.xy(
                mex * v.x() + mfx * v.y() + tx,
                mey * v.x() + mfy * v.y() + ty
        );
    }

    public double determinant() {
        return mex * mfy - mey * mfx;
    }

    public Transformation inverse() {
        double det = determinant();
        return new Transformation(
                mfy / det,
                -mfx / det,
                (mfx * ty - mfy * tx) / det,
                -mey / det,
                mex / det,
                (mey * tx - mex * ty) / det
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Transformation other)) {
            return false;
        }
        return mex == other.mex
                && mfx == other.mfx
                && tx == other.tx
                && mey == other.mey
                && mfy == other.mfy
                && ty == other.ty;
    }

    @Override
    public int hashCode() {
        int h = Double.hashCode(mex);
        h = 31 * h + Double.hashCode(mfx);
        h = 31 * h + Double.hashCode(tx);
        h = 31 * h + Double.hashCode(mey);
        h = 31 * h + Double.hashCode(mfy);
        h = 31 * h + Double.hashCode(ty);
        return h;
    }

    @Override
    public String toString() {
        return "Transformation[[" + mex + ", " + mfx + ", " + tx + "], ["
                + mey + ", " + mfy + ", " + ty + "]]";
    }
}
