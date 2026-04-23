package cvdexplorer.io;

import cvdexplorer.metric.MetricMemberCompatibility;
import cvdexplorer.metric.MetricKind;
import cvdexplorer.model.CircleMember;
import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.LineMember;
import cvdexplorer.model.NeighborOrder;
import cvdexplorer.model.PointMember;
import cvdexplorer.model.SceneState;
import cvdexplorer.model.SegmentMember;
import cvdexplorer.model.SiteMemberKind;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.scene.paint.Color;
import xyz.marsavic.geometry.Vector;

import java.util.ArrayList;
import java.util.List;

/** Human-readable scene file: version, metric/site-member authoring, clusters and geometry. */
public final class SceneJsonCodec {
    public static final String CURRENT_VERSION = "1";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private SceneJsonCodec() {
    }

    public static String encode(SceneState state) {
        SceneFileV1 file = toDto(state);
        return GSON.toJson(file);
    }

    /** Parse JSON and replace scene content via {@link SceneState#applyLoadedScene}. */
    public static void applyJson(SceneState state, String json) throws SceneJsonException {
        SceneFileV1 dto;
        try {
            dto = GSON.fromJson(json, SceneFileV1.class);
        } catch (Exception e) {
            throw new SceneJsonException("Invalid JSON", e);
        }
        if (dto == null) {
            throw new SceneJsonException("Empty scene file");
        }
        applyDto(state, dto);
    }

    static SceneFileV1 toDto(SceneState state) {
        List<ClusterJson> clusters = new ArrayList<>();
        for (ClusterSite site : state.clusters()) {
            List<MemberJson> members = new ArrayList<>();
            for (ClusterMember member : site.members()) {
                if (member instanceof PointMember pm) {
                    Vector p = pm.position();
                    MemberJson mj = new MemberJson();
                    mj.kind = "POINT";
                    mj.x = p.x();
                    mj.y = p.y();
                    members.add(mj);
                } else if (member instanceof SegmentMember sm) {
                    MemberJson mj = new MemberJson();
                    mj.kind = "LINE_SEGMENT";
                    mj.ax = sm.a().x();
                    mj.ay = sm.a().y();
                    mj.bx = sm.b().x();
                    mj.by = sm.b().y();
                    members.add(mj);
                } else if (member instanceof CircleMember cm) {
                    MemberJson mj = new MemberJson();
                    mj.kind = "CIRCLE";
                    mj.cx = cm.center().x();
                    mj.cy = cm.center().y();
                    mj.radius = cm.radius();
                    members.add(mj);
                } else if (member instanceof LineMember lm) {
                    MemberJson mj = new MemberJson();
                    mj.kind = "LINE";
                    mj.px = lm.a().x();
                    mj.py = lm.a().y();
                    mj.qx = lm.b().x();
                    mj.qy = lm.b().y();
                    members.add(mj);
                }
            }
            Color c = site.color();
            // JavaFX RGBA components are 0..1 in the file.
            ColorJson color = new ColorJson();
            color.r = c.getRed();
            color.g = c.getGreen();
            color.b = c.getBlue();
            color.opacity = c.getOpacity();
            ClusterJson cj = new ClusterJson();
            cj.name = site.name();
            cj.color = color;
            cj.members = members;
            clusters.add(cj);
        }
        SceneFileV1 root = new SceneFileV1();
        root.version = CURRENT_VERSION;
        root.metricKind = state.metricKind.name();
        root.neighborOrder = state.neighborOrder.name();
        root.siteMemberKind = state.siteMemberKind.name();
        root.nearestNeighborK = state.nearestNeighborK;
        root.clusters = clusters;
        return root;
    }

    /**
     * Parses {@code metricKind} from JSON. {@code AVERAGE_DISTANCE} is accepted as a legacy alias for {@link MetricKind#MEAN_DISTANCE}.
     */
    static MetricKind parseMetricKind(String name) throws SceneJsonException {
        if (name == null || name.isEmpty()) {
            throw new SceneJsonException("metricKind is required");
        }
        if ("AVERAGE_DISTANCE".equals(name)) {
            return MetricKind.MEAN_DISTANCE;
        }
        try {
            return MetricKind.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new SceneJsonException("Unknown metricKind: " + name);
        }
    }

    static NeighborOrder parseNeighborOrder(String name) throws SceneJsonException {
        if (name == null || name.isEmpty()) {
            return NeighborOrder.NEAREST;
        }
        try {
            return NeighborOrder.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new SceneJsonException("Unknown neighborOrder: " + name);
        }
    }

    static void applyDto(SceneState state, SceneFileV1 dto) throws SceneJsonException {
        if (dto.version == null || !dto.version.equals(CURRENT_VERSION)) {
            throw new SceneJsonException("Unsupported or missing version (expected " + CURRENT_VERSION + ")");
        }
        if (dto.clusters == null || dto.clusters.isEmpty()) {
            throw new SceneJsonException("Scene must contain at least one cluster");
        }
        if (dto.clusters.size() > SceneState.MAX_CLUSTERS) {
            throw new SceneJsonException("Too many clusters (max " + SceneState.MAX_CLUSTERS + ")");
        }

        MetricKind metricKind = parseMetricKind(dto.metricKind);
        NeighborOrder neighborOrder = parseNeighborOrder(dto.neighborOrder);
        SiteMemberKind siteMemberKind;
        try {
            siteMemberKind = SiteMemberKind.valueOf(dto.siteMemberKind);
        } catch (Exception e) {
            throw new SceneJsonException("Unknown siteMemberKind: " + dto.siteMemberKind);
        }

        List<ClusterSite> loaded = new ArrayList<>();
        for (ClusterJson cj : dto.clusters) {
            if (cj.name == null) {
                throw new SceneJsonException("Cluster name is required");
            }
            if (cj.color == null) {
                throw new SceneJsonException("Cluster color is required for " + cj.name);
            }
            Color color = new Color(
                    cj.color.r,
                    cj.color.g,
                    cj.color.b,
                    cj.color.opacity
            );
            if (cj.members == null || cj.members.isEmpty()) {
                throw new SceneJsonException("Cluster must have at least one member: " + cj.name);
            }
            List<ClusterMember> members = parseMembers(cj.members, cj.name);
            loaded.add(new ClusterSite(cj.name, color, members));
        }

        String invalidMetricMessage = MetricMemberCompatibility.invalidMetricMessage(metricKind, loaded).orElse(null);
        if (invalidMetricMessage != null) {
            throw new SceneJsonException(invalidMetricMessage);
        }

        int loadedNearestNeighborK = 1;
        if (dto.nearestNeighborK != null) {
            if (dto.nearestNeighborK < 1 || dto.nearestNeighborK > SceneState.MAX_MEMBERS_PER_CLUSTER) {
                throw new SceneJsonException(
                        "nearestNeighborK must be between 1 and " + SceneState.MAX_MEMBERS_PER_CLUSTER
                );
            }
            loadedNearestNeighborK = dto.nearestNeighborK;
        }

        // View toggles unchanged; gadget counts follow the new cluster list.
        state.applyLoadedScene(metricKind, neighborOrder, siteMemberKind, loaded, loadedNearestNeighborK);
    }

    private static List<ClusterMember> parseMembers(List<MemberJson> members, String clusterName) throws SceneJsonException {
        List<ClusterMember> out = new ArrayList<>();
        for (MemberJson mj : members) {
            if (mj.kind == null) {
                throw new SceneJsonException("Member kind is required in cluster " + clusterName);
            }
            switch (mj.kind) {
                case "POINT" -> {
                    if (mj.x == null || mj.y == null) {
                        throw new SceneJsonException("POINT member requires x and y in cluster " + clusterName);
                    }
                    out.add(new PointMember(Vector.xy(mj.x, mj.y)));
                }
                case "LINE_SEGMENT" -> {
                    if (mj.ax == null || mj.ay == null || mj.bx == null || mj.by == null) {
                        throw new SceneJsonException("LINE_SEGMENT member requires ax, ay, bx, by in cluster " + clusterName);
                    }
                    out.add(new SegmentMember(Vector.xy(mj.ax, mj.ay), Vector.xy(mj.bx, mj.by)));
                }
                case "CIRCLE" -> {
                    if (mj.cx == null || mj.cy == null || mj.radius == null) {
                        throw new SceneJsonException("CIRCLE member requires cx, cy, radius in cluster " + clusterName);
                    }
                    if (mj.radius < 0.0) {
                        throw new SceneJsonException("CIRCLE member radius must be non-negative in cluster " + clusterName);
                    }
                    Vector center = Vector.xy(mj.cx, mj.cy);
                    Vector radiusHandle = center.add(Vector.xy(mj.radius, 0.0));
                    out.add(new CircleMember(center, radiusHandle));
                }
                case "LINE" -> {
                    if (mj.px == null || mj.py == null || mj.qx == null || mj.qy == null) {
                        throw new SceneJsonException("LINE member requires px, py, qx, qy in cluster " + clusterName);
                    }
                    out.add(new LineMember(Vector.xy(mj.px, mj.py), Vector.xy(mj.qx, mj.qy)));
                }
                default -> throw new SceneJsonException("Unknown member kind: " + mj.kind + " in cluster " + clusterName);
            }
        }
        if (out.size() > SceneState.MAX_MEMBERS_PER_CLUSTER) {
            throw new SceneJsonException("Too many members in cluster " + clusterName);
        }
        return out;
    }

    /** Gson DTO; field names match JSON keys. */
    static final class SceneFileV1 {
        String version;
        String metricKind;
        String neighborOrder;
        String siteMemberKind;
        Integer nearestNeighborK;
        List<ClusterJson> clusters;
    }

    static final class ClusterJson {
        String name;
        ColorJson color;
        List<MemberJson> members;
    }

    static final class ColorJson {
        double r;
        double g;
        double b;
        double opacity;
    }

    static final class MemberJson {
        String kind;
        Double x;
        Double y;
        Double ax;
        Double ay;
        Double bx;
        Double by;
        Double cx;
        Double cy;
        Double radius;
        Double px;
        Double py;
        Double qx;
        Double qy;
    }
}
