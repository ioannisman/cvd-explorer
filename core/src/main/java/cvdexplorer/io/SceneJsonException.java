package cvdexplorer.io;

/** Invalid file content, version, or schema (parse errors wrap the cause). */
public final class SceneJsonException extends Exception {
    public SceneJsonException(String message) {
        super(message);
    }

    public SceneJsonException(String message, Throwable cause) {
        super(message, cause);
    }
}
