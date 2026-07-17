package cvdexplorer.model;

/** Color with components in {@code [0, 1]} (same layout as scene JSON). */
public final class Rgba {
    public static final Rgba RED = rgb(1.0, 0.0, 0.0);
    public static final Rgba GREEN = rgb(0.0, 1.0, 0.0);
    public static final Rgba BLUE = rgb(0.0, 0.0, 1.0);
    public static final Rgba BLACK = rgb(0.0, 0.0, 0.0);
    public static final Rgba WHITE = rgb(1.0, 1.0, 1.0);
    public static final Rgba GRAY = gray(0.5);
    public static final Rgba ORANGE = rgb(1.0, 0.647, 0.0);
    public static final Rgba CYAN = rgb(0.0, 1.0, 1.0);

    private final double r;
    private final double g;
    private final double b;
    private final double a;

    public Rgba(double r, double g, double b, double a) {
        this.r = clamp01(r);
        this.g = clamp01(g);
        this.b = clamp01(b);
        this.a = clamp01(a);
    }

    public static Rgba rgb(double r, double g, double b) {
        return new Rgba(r, g, b, 1.0);
    }

    public static Rgba gray(double value) {
        return gray(value, 1.0);
    }

    public static Rgba gray(double value, double opacity) {
        double v = clamp01(value);
        return new Rgba(v, v, v, opacity);
    }

    /** Hue in degrees; saturation, brightness, and opacity in {@code [0, 1]}. */
    public static Rgba hsb(double hue, double saturation, double brightness) {
        return hsb(hue, saturation, brightness, 1.0);
    }

    public static Rgba hsb(double hue, double saturation, double brightness, double opacity) {
        double h = ((hue % 360.0) + 360.0) % 360.0;
        double s = clamp01(saturation);
        double v = clamp01(brightness);
        double c = v * s;
        double x = c * (1.0 - Math.abs((h / 60.0) % 2.0 - 1.0));
        double m = v - c;
        double rp;
        double gp;
        double bp;
        if (h < 60.0) {
            rp = c;
            gp = x;
            bp = 0.0;
        } else if (h < 120.0) {
            rp = x;
            gp = c;
            bp = 0.0;
        } else if (h < 180.0) {
            rp = 0.0;
            gp = c;
            bp = x;
        } else if (h < 240.0) {
            rp = 0.0;
            gp = x;
            bp = c;
        } else if (h < 300.0) {
            rp = x;
            gp = 0.0;
            bp = c;
        } else {
            rp = c;
            gp = 0.0;
            bp = x;
        }
        return new Rgba(rp + m, gp + m, bp + m, opacity);
    }

    public double r() {
        return r;
    }

    public double g() {
        return g;
    }

    public double b() {
        return b;
    }

    public double a() {
        return a;
    }

    /** Scales RGB by {@code brightnessFactor} (clamped to {@code [0, 1]}); alpha unchanged. */
    public Rgba withBrightnessFactor(double brightnessFactor) {
        return new Rgba(
                clamp01(r * brightnessFactor),
                clamp01(g * brightnessFactor),
                clamp01(b * brightnessFactor),
                a
        );
    }

    public int toArgb() {
        int ai = (int) Math.round(a * 255.0);
        int ri = (int) Math.round(r * 255.0);
        int gi = (int) Math.round(g * 255.0);
        int bi = (int) Math.round(b * 255.0);
        return (ai << 24) | (ri << 16) | (gi << 8) | bi;
    }

    private static double clamp01(double v) {
        if (v < 0.0) {
            return 0.0;
        }
        if (v > 1.0) {
            return 1.0;
        }
        return v;
    }
}
