package cvdexplorer.model;

/**
 * Euclidean distance from a point to an axis-aligned ellipse {@code x²/a² + y²/b² = 1}
 * in the first quadrant, after David Eberly's Geometric Tools formulation.
 * The scalar root is found with Newton iteration (bisection fallback).
 */
final class EllipseDistance {
    private static final int MAX_NEWTON = 12;
    private static final int MAX_BISECTION = 32;
    private static final double EPS = 1e-12;
    private static final double TOL = 1e-12;

    private EllipseDistance() {
    }

    /**
     * Unsigned Euclidean distance from local coordinates (ellipse-centered, axis-aligned)
     * to the ellipse with semi-axes {@code a}, {@code b} (both positive; order free).
     */
    static double distanceAxisAligned(double localX, double localY, double a, double b) {
        double u = Math.abs(localX);
        double v = Math.abs(localY);
        if (a < b) {
            double tmp = a;
            a = b;
            b = tmp;
            tmp = u;
            u = v;
            v = tmp;
        }
        if (b <= EPS) {
            double x = Math.max(-a, Math.min(a, localX));
            double dx = localX - x;
            double dy = localY;
            return Math.sqrt(dx * dx + dy * dy);
        }
        return distanceFirstQuadrant(u, v, a, b);
    }

    private static double distanceFirstQuadrant(double u, double v, double a, double b) {
        if (v > EPS) {
            if (u > EPS) {
                double g = (u * u) / (a * a) + (v * v) / (b * b) - 1.0;
                if (Math.abs(g) <= EPS) {
                    return 0.0;
                }
                double r0 = (a / b) * (a / b);
                double sbar = findRoot(r0, u / a, v / b, g);
                double x0 = r0 * u / (sbar + r0);
                double x1 = v / (sbar + 1.0);
                double dx = x0 - u;
                double dy = x1 - v;
                return Math.sqrt(dx * dx + dy * dy);
            }
            return Math.abs(v - b);
        }
        if (u < a) {
            double denom = a * a - b * b;
            if (denom > EPS) {
                double x0 = a * u / denom;
                if (x0 < 1.0) {
                    double x1 = b * Math.sqrt(Math.max(0.0, 1.0 - x0 * x0));
                    double dx = a * x0 - u;
                    return Math.sqrt(dx * dx + x1 * x1);
                }
            }
        }
        return Math.abs(u - a);
    }

    /**
     * Solves {@code F(s) = (n0/(s+r0))² + (z1/(s+1))² - 1 = 0} with Newton,
     * falling back to bisection if a step leaves the safe bracket.
     */
    private static double findRoot(double r0, double z0, double z1, double g) {
        double n0 = r0 * z0;
        double s0 = z1 - 1.0;
        double s1 = g < 0.0 ? 0.0 : Math.sqrt(n0 * n0 + z1 * z1) - 1.0;
        // Ensure s0 <= s1 for bracketing (interior vs exterior initial guesses).
        if (s0 > s1) {
            double tmp = s0;
            s0 = s1;
            s1 = tmp;
        }

        double s = 0.5 * (s0 + s1);
        for (int i = 0; i < MAX_NEWTON; i++) {
            double spR0 = s + r0;
            double sp1 = s + 1.0;
            if (Math.abs(spR0) < EPS || Math.abs(sp1) < EPS) {
                break;
            }
            double ratio0 = n0 / spR0;
            double ratio1 = z1 / sp1;
            double f = ratio0 * ratio0 + ratio1 * ratio1 - 1.0;
            if (Math.abs(f) <= TOL) {
                return s;
            }
            // F'(s) = -2 n0²/(s+r0)³ - 2 z1²/(s+1)³
            double df = -2.0 * (n0 * n0) / (spR0 * spR0 * spR0)
                    - 2.0 * (z1 * z1) / (sp1 * sp1 * sp1);
            if (Math.abs(df) < EPS) {
                break;
            }
            double sNext = s - f / df;
            if (sNext < s0 || sNext > s1 || Double.isNaN(sNext)) {
                break;
            }
            if (Math.abs(sNext - s) <= TOL * (1.0 + Math.abs(sNext))) {
                return sNext;
            }
            s = sNext;
        }

        return bisect(n0, z1, r0, s0, s1);
    }

    private static double bisect(double n0, double z1, double r0, double s0, double s1) {
        double s = 0.5 * (s0 + s1);
        for (int i = 0; i < MAX_BISECTION; i++) {
            s = 0.5 * (s0 + s1);
            if (s == s0 || s == s1) {
                break;
            }
            double ratio0 = n0 / (s + r0);
            double ratio1 = z1 / (s + 1.0);
            double g = ratio0 * ratio0 + ratio1 * ratio1 - 1.0;
            if (g > 0.0) {
                s0 = s;
            } else if (g < 0.0) {
                s1 = s;
            } else {
                break;
            }
        }
        return s;
    }
}
