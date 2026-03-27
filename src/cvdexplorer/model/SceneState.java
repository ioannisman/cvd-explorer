package cvdexplorer.model;

import cvdexplorer.metric.MetricKind;
import javafx.scene.paint.Color;
import xyz.marsavic.drawingfx.gadgets.annotations.GadgetBoolean;
import xyz.marsavic.drawingfx.gadgets.annotations.GadgetEnum;
import xyz.marsavic.drawingfx.gadgets.annotations.Properties;
import xyz.marsavic.geometry.Vector;

import java.util.ArrayList;
import java.util.List;

public final class SceneState {
    @GadgetEnum(enumClass = MetricKind.class)
    @Properties(name = "Metric (m)")
    public MetricKind metricKind = MetricKind.NEAREST;

    @GadgetBoolean
    @Properties(name = "Show diagram (d)")
    public boolean showDiagram = true;

    @GadgetBoolean
    @Properties(name = "Show members (p)")
    public boolean showMembers = true;

    @GadgetBoolean
    @Properties(name = "Show help (h)")
    public boolean showHelp = true;

    @GadgetBoolean
    @Properties(name = "Snap to grid (g)")
    public boolean snapToGrid = false;

    private final List<ClusterSite> clusters = new ArrayList<>();
    private final Color backgroundColor = Color.gray(0.92);

    public static SceneState demo() {
        SceneState state = new SceneState();
        state.clusters.add(new ClusterSite(
                "Amber",
                Color.hsb(30, 0.75, 1.0),
                List.of(
                        new PointMember(Vector.xy(-260, -100)),
                        new PointMember(Vector.xy(-180, -220)),
                        new PointMember(Vector.xy(-120, -80))
                )
        ));
        state.clusters.add(new ClusterSite(
                "Azure",
                Color.hsb(210, 0.75, 0.95),
                List.of(
                        new PointMember(Vector.xy(180, -170)),
                        new PointMember(Vector.xy(260, -40)),
                        new PointMember(Vector.xy(110, 20)),
                        new PointMember(Vector.xy(220, 120))
                )
        ));
        state.clusters.add(new ClusterSite(
                "Rose",
                Color.hsb(330, 0.7, 1.0),
                List.of(
                        new PointMember(Vector.xy(-160, 160)),
                        new PointMember(Vector.xy(-40, 220))
                )
        ));
        state.clusters.add(new ClusterSite(
                "Lime",
                Color.hsb(110, 0.7, 0.9),
                List.of(
                        new PointMember(Vector.xy(60, 180)),
                        new PointMember(Vector.xy(170, 220)),
                        new PointMember(Vector.xy(280, 210)),
                        new PointMember(Vector.xy(210, 300)),
                        new PointMember(Vector.xy(110, 310))
                )
        ));
        return state;
    }

    public List<ClusterSite> clusters() {
        return clusters;
    }

    public Color backgroundColor() {
        return backgroundColor;
    }

    public int clusterCount() {
        return clusters.size();
    }

    public void cycleMetric() {
        metricKind = metricKind == MetricKind.NEAREST ? MetricKind.FARTHEST : MetricKind.NEAREST;
    }

    public void copyFrom(SceneState other) {
        metricKind = other.metricKind;
        showDiagram = other.showDiagram;
        showMembers = other.showMembers;
        showHelp = other.showHelp;
        snapToGrid = other.snapToGrid;

        clusters.clear();
        for (ClusterSite cluster : other.clusters) {
            clusters.add(new ClusterSite(cluster.name(), cluster.color(), cluster.members()));
        }
    }
}
