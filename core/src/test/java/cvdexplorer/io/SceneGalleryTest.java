package cvdexplorer.io;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SceneGalleryTest {

    @Test
    void entriesLoadDefaultSceneWithReadableName() throws Exception {
        List<SceneGallery.Entry> entries = SceneGallery.entries();
        assertFalse(entries.isEmpty());
        SceneGallery.Entry first = entries.get(0);
        assertEquals("default_cvd.json", first.fileName());
        assertEquals("Default cluster Voronoi", first.name());
        assertEquals("Default cluster Voronoi", SceneGallery.displayName("default_cvd.json"));
    }

    @Test
    void loadSceneParsesGalleryDefault() throws Exception {
        var snapshot = SceneGallery.loadScene("default_cvd.json");
        assertEquals("Default cluster Voronoi", snapshot.name());
        assertTrue(snapshot.clusters().size() >= 1);
    }
}
