package cvdexplorer.model;

import cvdexplorer.metric.MetricKind;
import javafx.scene.paint.Color;
import xyz.marsavic.drawingfx.gadgets.annotations.GadgetBoolean;
import xyz.marsavic.drawingfx.gadgets.annotations.GadgetEnum;
import xyz.marsavic.drawingfx.gadgets.annotations.GadgetInteger;
import xyz.marsavic.drawingfx.gadgets.annotations.Properties;
import xyz.marsavic.geometry.Vector;

import java.util.ArrayList;
import java.util.List;

public final class SceneState {
    public static final int MAX_CLUSTERS = 32;
    public static final int MAX_MEMBERS_PER_CLUSTER = 32;

    @GadgetInteger(min = 1, max = MAX_CLUSTERS)
    @Properties(name = "Number of clusters")
    public int numberOfClusters = 1;

    @GadgetInteger(min = 1, max = MAX_CLUSTERS)
    @Properties(name = "Active cluster")
    public int activeClusterOneBased = 1;

    @GadgetInteger(min = 1, max = MAX_MEMBERS_PER_CLUSTER)
    @Properties(name = "Members in active cluster")
    public int targetPointCountForActiveCluster = 1;

    @GadgetEnum(enumClass = SiteMemberKind.class)
    @Properties(name = "New member type")
    public SiteMemberKind siteMemberKind = SiteMemberKind.POINT;

    @GadgetEnum(enumClass = MetricKind.class)
    @Properties(name = "Metric (m)")
    public MetricKind metricKind = MetricKind.MINIMUM_DISTANCE;

    @GadgetInteger(min = 1, max = MAX_CLUSTERS)
    @Properties(name = "Order k")
    public int orderKOneBased = 1;

    @GadgetBoolean
    @Properties(name = "Show diagram (d)")
    public boolean showDiagram = true;

    @GadgetBoolean
    @Properties(name = "Show members (p)")
    public boolean showMembers = true;

    @GadgetBoolean
    @Properties(name = "Show skeleton (k)")
    public boolean showSkeleton = false;

    @GadgetBoolean
    @Properties(name = "Show help (h)")
    public boolean showHelp = true;

    @GadgetBoolean
    @Properties(name = "Snap to grid (g)")
    public boolean snapToGrid = false;

    @GadgetBoolean
    @Properties(name = "Snap to handles (f)")
    public boolean snapToHandles = false;

    @GadgetBoolean
    @Properties(name = "Distance shading (s)")
    public boolean showShading = true;

    private final List<ClusterSite> clusters = new ArrayList<>();
    private final Color backgroundColor = Color.gray(0.92);

    private int lastActiveClusterOneBasedForMemberSync = -1;

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
        state.numberOfClusters = state.clusters.size();
        state.activeClusterOneBased = 1;
        state.targetPointCountForActiveCluster = state.clusters.get(0).size();
        state.siteMemberKind = SiteMemberKind.POINT;
        state.lastActiveClusterOneBasedForMemberSync = -1;
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
        metricKind = switch (metricKind) {
            case MINIMUM_DISTANCE -> MetricKind.MAXIMUM_DISTANCE;
            case MAXIMUM_DISTANCE -> MetricKind.SUM_OF_DISTANCES;
            case SUM_OF_DISTANCES -> MetricKind.MINIMUM_DISTANCE;
        };
    }

    public void cycleOrderK() {
        int maxOrder = maxOrderK();
        orderKOneBased = orderKOneBased >= maxOrder ? 1 : orderKOneBased + 1;
    }

    /**
     * Replaces all clusters and metric authoring fields from a loaded file.
     * View toggles and camera are unchanged. Gadget fields are normalized to the new cluster list.
     */
    public void applyLoadedScene(MetricKind newMetricKind, SiteMemberKind newSiteMemberKind, List<ClusterSite> newClusters) {
        applyLoadedScene(newMetricKind, 1, newSiteMemberKind, newClusters);
    }

    /**
     * Replaces all clusters and metric authoring fields from a loaded file.
     * View toggles and camera are unchanged. Gadget fields are normalized to the new cluster list.
     */
    public void applyLoadedScene(
            MetricKind newMetricKind,
            int newOrderKOneBased,
            SiteMemberKind newSiteMemberKind,
            List<ClusterSite> newClusters
    ) {
        if (newClusters.isEmpty() || newClusters.size() > MAX_CLUSTERS) {
            throw new IllegalArgumentException("Cluster list must be non-empty and at most " + MAX_CLUSTERS);
        }
        metricKind = newMetricKind;
        orderKOneBased = newOrderKOneBased;
        siteMemberKind = newSiteMemberKind;
        clusters.clear();
        for (ClusterSite site : newClusters) {
            clusters.add(site);
        }
        numberOfClusters = clusters.size();
        normalizeOrderK();
        activeClusterOneBased = Math.max(1, Math.min(numberOfClusters, activeClusterOneBased));
        targetPointCountForActiveCluster = clusters.get(activeClusterOneBased - 1).size();
        // Keeps ensureActiveClusterMemberCount from fighting the loaded member lists.
        lastActiveClusterOneBasedForMemberSync = activeClusterOneBased;
    }

    public void copyFrom(SceneState other) {
        metricKind = other.metricKind;
        orderKOneBased = other.orderKOneBased;
        showDiagram = other.showDiagram;
        showMembers = other.showMembers;
        showSkeleton = other.showSkeleton;
        showHelp = other.showHelp;
        snapToGrid = other.snapToGrid;
        snapToHandles = other.snapToHandles;
        showShading = other.showShading;
        siteMemberKind = other.siteMemberKind;
        numberOfClusters = other.numberOfClusters;
        activeClusterOneBased = other.activeClusterOneBased;
        targetPointCountForActiveCluster = other.targetPointCountForActiveCluster;
        lastActiveClusterOneBasedForMemberSync = -1;

        clusters.clear();
        for (ClusterSite cluster : other.clusters) {
            clusters.add(new ClusterSite(cluster.name(), cluster.color(), cluster.members()));
        }
        if (numberOfClusters != clusters.size()) {
            numberOfClusters = clusters.size();
        }
        normalizeOrderK();
        int n = Math.max(1, clusters.size());
        activeClusterOneBased = Math.max(1, Math.min(n, activeClusterOneBased));
    }

    public void ensureClusterCountMatchesGadget() {
        numberOfClusters = Math.max(1, Math.min(MAX_CLUSTERS, numberOfClusters));
        while (clusters.size() < numberOfClusters) {
            clusters.add(defaultCluster(clusters.size()));
        }
        while (clusters.size() > numberOfClusters) {
            clusters.remove(clusters.size() - 1);
        }
        normalizeOrderK();
        activeClusterOneBased = Math.max(1, Math.min(clusters.size(), activeClusterOneBased));
    }

    public int maxOrderK() {
        return Math.max(1, clusters.size() - 1);
    }

    public void normalizeOrderK() {
        orderKOneBased = Math.max(1, Math.min(maxOrderK(), orderKOneBased));
    }

    public void ensureActiveClusterMemberCount() {
        if (clusters.isEmpty()) {
            return;
        }

        activeClusterOneBased = Math.max(1, Math.min(clusters.size(), activeClusterOneBased));
        targetPointCountForActiveCluster = Math.max(
                1,
                Math.min(MAX_MEMBERS_PER_CLUSTER, targetPointCountForActiveCluster)
        );

        if (activeClusterOneBased != lastActiveClusterOneBasedForMemberSync) {
            targetPointCountForActiveCluster = clusters.get(activeClusterOneBased - 1).size();
            lastActiveClusterOneBasedForMemberSync = activeClusterOneBased;
        }

        int clusterIndex = activeClusterOneBased - 1;
        ClusterSite cluster = clusters.get(clusterIndex);

        while (cluster.size() < targetPointCountForActiveCluster) {
            Vector hint = jitteredNewMemberHint(cluster, clusterIndex, cluster.size());
            cluster.addMember(SiteMemberFactory.createDefault(siteMemberKind, clusterIndex, cluster.size(), hint));
        }
        while (cluster.size() > targetPointCountForActiveCluster) {
            cluster.removeMember(cluster.size() - 1);
        }
    }

    private static Vector centroidOfMembers(List<ClusterMember> members) {
        double sx = 0.0;
        double sy = 0.0;
        for (ClusterMember m : members) {
            Vector c = m.placementCentroid();
            sx += c.x();
            sy += c.y();
        }
        int n = members.size();
        return Vector.xy(sx / n, sy / n);
    }

    private Vector jitteredNewMemberHint(ClusterSite cluster, int clusterIndex, int newMemberIndex) {
        List<ClusterMember> members = cluster.members();
        Vector c;
        if (members.isEmpty()) {
            double x = -280 + (clusterIndex % 5) * 140;
            double y = -200 + (clusterIndex / 5) * 140;
            c = Vector.xy(x, y);
        } else {
            c = centroidOfMembers(members);
        }
        double angle = 2 * Math.PI * (newMemberIndex * 0.618033988749895);
        double radius = 18.0 + 6.0 * (newMemberIndex % 7);
        return c.add(Vector.polar(radius, angle));
    }

    private ClusterSite defaultCluster(int index) {
        double hue = (360 * index * 0.618033988749895) % 360;
        Color color = Color.hsb(hue, 0.65, 0.95);
        double x = -280 + (index % 5) * 140;
        double y = -200 + (index / 5) * 140;
        Vector center = Vector.xy(x, y);
        ClusterMember first = SiteMemberFactory.createDefault(siteMemberKind, index, 0, center);
        return new ClusterSite(
                "Cluster " + (index + 1),
                color,
                List.of(first)
        );
    }
}
