package cvdexplorer.web;

import cvdexplorer.io.SceneFileFormat;
import cvdexplorer.io.SceneFileFormat.ClusterJson;
import cvdexplorer.io.SceneFileFormat.ColorJson;
import cvdexplorer.io.SceneFileFormat.MemberJson;
import cvdexplorer.io.SceneFileFormat.SceneFileV1;
import cvdexplorer.io.SceneJsonException;
import cvdexplorer.model.SceneSnapshot;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSArray;
import org.teavm.jso.core.JSString;

import java.util.ArrayList;
import java.util.List;

/**
 * TeaVM-safe scene JSON load: browser {@code JSON.parse}, then map into {@link SceneFileFormat} DTOs.
 * Avoids Gson, which pulls unsupported JDK classes under TeaVM.
 */
final class SceneJsonJs {
    private SceneJsonJs() {
    }

    static SceneSnapshot parse(String json) throws SceneJsonException {
        JSObject root;
        try {
            root = parseObject(json);
        } catch (Throwable t) {
            throw new SceneJsonException("Invalid JSON", t);
        }
        if (root == null) {
            throw new SceneJsonException("Empty scene file");
        }
        return SceneFileFormat.fromDto(toDto(root));
    }

    private static SceneFileV1 toDto(JSObject root) throws SceneJsonException {
        SceneFileV1 dto = new SceneFileV1();
        dto.version = str(root, "version");
        dto.name = str(root, "name");
        dto.metricKind = str(root, "metricKind");
        dto.neighborOrder = str(root, "neighborOrder");
        dto.siteMemberKind = str(root, "siteMemberKind");
        if (has(root, "nearestNeighborK")) {
            dto.nearestNeighborK = (int) num(root, "nearestNeighborK");
        }
        JSArray<?> clusters = array(root, "clusters");
        if (clusters == null) {
            throw new SceneJsonException("Scene must contain at least one cluster");
        }
        dto.clusters = new ArrayList<>();
        for (int i = 0; i < clusters.getLength(); i++) {
            dto.clusters.add(toCluster((JSObject) clusters.get(i)));
        }
        return dto;
    }

    private static ClusterJson toCluster(JSObject cluster) throws SceneJsonException {
        if (cluster == null) {
            throw new SceneJsonException("Cluster entry is null");
        }
        ClusterJson cj = new ClusterJson();
        cj.name = str(cluster, "name");
        JSObject color = obj(cluster, "color");
        if (color != null) {
            ColorJson c = new ColorJson();
            c.r = num(color, "r");
            c.g = num(color, "g");
            c.b = num(color, "b");
            c.opacity = num(color, "opacity");
            cj.color = c;
        }
        JSArray<?> members = array(cluster, "members");
        if (members == null) {
            throw new SceneJsonException("Cluster must have at least one member: " + cj.name);
        }
        List<MemberJson> list = new ArrayList<>();
        for (int i = 0; i < members.getLength(); i++) {
            list.add(toMember((JSObject) members.get(i)));
        }
        cj.members = list;
        return cj;
    }

    private static MemberJson toMember(JSObject member) {
        MemberJson mj = new MemberJson();
        if (member == null) {
            return mj;
        }
        mj.kind = str(member, "kind");
        mj.x = optNum(member, "x");
        mj.y = optNum(member, "y");
        mj.ax = optNum(member, "ax");
        mj.ay = optNum(member, "ay");
        mj.bx = optNum(member, "bx");
        mj.by = optNum(member, "by");
        mj.cx = optNum(member, "cx");
        mj.cy = optNum(member, "cy");
        mj.radius = optNum(member, "radius");
        mj.px = optNum(member, "px");
        mj.py = optNum(member, "py");
        mj.qx = optNum(member, "qx");
        mj.qy = optNum(member, "qy");
        mj.hx = optNum(member, "hx");
        mj.hy = optNum(member, "hy");
        return mj;
    }

    @JSBody(params = { "json" }, script = "return JSON.parse(json);")
    private static native JSObject parseObject(String json);

    @JSBody(params = { "obj", "key" }, script = "return obj != null && Object.prototype.hasOwnProperty.call(obj, key);")
    private static native boolean has(JSObject obj, String key);

    @JSBody(params = { "obj", "key" }, script = "var v = obj[key]; return v == null ? null : v;")
    private static native JSObject obj(JSObject obj, String key);

    @JSBody(params = { "obj", "key" }, script = "var v = obj[key]; return Array.isArray(v) ? v : null;")
    private static native JSArray<?> array(JSObject obj, String key);

    @JSBody(params = { "obj", "key" }, script = ""
            + "var v = obj[key];"
            + "if (v == null) return null;"
            + "return (typeof v === 'string') ? v : String(v);")
    private static native JSString strRaw(JSObject obj, String key);

    @JSBody(params = { "obj", "key" }, script = ""
            + "var v = obj[key];"
            + "if (v == null || typeof v !== 'number') return NaN;"
            + "return v;")
    private static native double numRaw(JSObject obj, String key);

    private static String str(JSObject obj, String key) {
        JSString v = strRaw(obj, key);
        return v == null ? null : v.stringValue();
    }

    private static double num(JSObject obj, String key) {
        return numRaw(obj, key);
    }

    private static Double optNum(JSObject obj, String key) {
        if (!has(obj, key)) {
            return null;
        }
        double v = numRaw(obj, key);
        if (Double.isNaN(v)) {
            return null;
        }
        return v;
    }
}
