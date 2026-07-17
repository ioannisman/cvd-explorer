package cvdexplorer.desktop;

import cvdexplorer.model.Rgba;
import javafx.scene.paint.Color;

/** Converts {@link Rgba} to {@link Color}. */
public final class FxColors {
    private FxColors() {
    }

    public static Color toFx(Rgba rgba) {
        return new Color(rgba.r(), rgba.g(), rgba.b(), rgba.a());
    }
}
