package cvdexplorer.model;

/**
 * Human-readable cluster labels derived from hue (for newly created clusters).
 * Demo scenes may still use bespoke names like Amber / Azure.
 */
public final class ClusterNaming {
    private static final String[] NAMES = {
            "Red", "Orange", "Amber", "Yellow", "Lime", "Green", "Teal",
            "Cyan", "Azure", "Blue", "Indigo", "Violet", "Magenta", "Rose"
    };
    /** Approximate hue centers (degrees) matching {@link #NAMES}. */
    private static final double[] HUE_CENTERS = {
            0, 20, 35, 55, 100, 130, 160,
            185, 210, 235, 255, 275, 300, 330
    };

    private ClusterNaming() {
    }

    /** Maps a hue in degrees to the nearest named color on the wheel. */
    public static String fromHue(double hueDegrees) {
        double hue = normalizeHue(hueDegrees);
        int best = 0;
        double bestDist = Double.POSITIVE_INFINITY;
        for (int i = 0; i < HUE_CENTERS.length; i++) {
            double dist = hueDistance(hue, HUE_CENTERS[i]);
            if (dist < bestDist) {
                bestDist = dist;
                best = i;
            }
        }
        return NAMES[best];
    }

    /**
     * Name for a new cluster at {@code index}, using the same golden-angle hue
     * as {@code Rgba.hsb} defaults in scene authoring.
     */
    public static String forNewCluster(int index) {
        double hue = (360 * index * 0.618033988749895) % 360;
        return fromHue(hue);
    }

    private static double normalizeHue(double hueDegrees) {
        double hue = hueDegrees % 360.0;
        if (hue < 0.0) {
            hue += 360.0;
        }
        return hue;
    }

    private static double hueDistance(double a, double b) {
        double d = Math.abs(a - b) % 360.0;
        return d > 180.0 ? 360.0 - d : d;
    }
}
