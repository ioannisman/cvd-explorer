package cvdexplorer.io;

import com.google.gson.Gson;
import cvdexplorer.model.SceneSnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Curated scenes from classpath {@code scenes/gallery/} (see repo {@code scenes/gallery/}).
 * Dropdown labels use each file's top-level {@code name} field.
 */
public final class SceneGallery {
    private static final String ROOT = "scenes/gallery/";
    private static final Gson GSON = new Gson();

    private static List<Entry> cachedEntries;
    private static Map<String, String> cachedNames;

    private SceneGallery() {
    }

    public record Entry(String fileName, String name) {
    }

    /** Gallery entries in manifest order. */
    public static synchronized List<Entry> entries() throws SceneJsonException, IOException {
        ensureLoaded();
        return List.copyOf(cachedEntries);
    }

    /** Display name for a gallery file, or the filename if unknown. */
    public static synchronized String displayName(String fileName) {
        try {
            ensureLoaded();
        } catch (SceneJsonException | IOException e) {
            return fileName;
        }
        return cachedNames.getOrDefault(fileName, fileName);
    }

    public static SceneSnapshot loadScene(String fileName) throws SceneJsonException, IOException {
        if (fileName == null || fileName.isBlank()) {
            throw new SceneJsonException("Gallery file name is required");
        }
        String json = readResource(ROOT + fileName);
        return SceneJsonCodec.parse(json);
    }

    private static void ensureLoaded() throws SceneJsonException, IOException {
        if (cachedEntries != null) {
            return;
        }
        String manifestJson = readResource(ROOT + "manifest.json");
        ManifestDto manifest = GSON.fromJson(manifestJson, ManifestDto.class);
        if (manifest == null || manifest.files == null || manifest.files.isEmpty()) {
            throw new SceneJsonException("Gallery manifest must list at least one file");
        }
        List<Entry> entries = new ArrayList<>();
        Map<String, String> names = new LinkedHashMap<>();
        for (String fileName : manifest.files) {
            if (fileName == null || fileName.isBlank()) {
                throw new SceneJsonException("Gallery manifest contains an empty file name");
            }
            SceneSnapshot snapshot = loadScene(fileName.trim());
            String name = snapshot.name();
            if (name == null || name.isBlank()) {
                throw new SceneJsonException("Gallery scene missing top-level name: " + fileName);
            }
            String trimmed = name.trim();
            entries.add(new Entry(fileName.trim(), trimmed));
            names.put(fileName.trim(), trimmed);
        }
        cachedEntries = List.copyOf(entries);
        cachedNames = Map.copyOf(names);
    }

    private static String readResource(String path) throws IOException {
        ClassLoader cl = SceneGallery.class.getClassLoader();
        try (InputStream in = cl.getResourceAsStream(path)) {
            if (in == null) {
                throw new IOException("Missing gallery resource: " + path);
            }
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                StringBuilder sb = new StringBuilder();
                char[] buf = new char[4096];
                int n;
                while ((n = reader.read(buf)) >= 0) {
                    sb.append(buf, 0, n);
                }
                return sb.toString();
            }
        }
    }

    static final class ManifestDto {
        List<String> files;
    }
}
