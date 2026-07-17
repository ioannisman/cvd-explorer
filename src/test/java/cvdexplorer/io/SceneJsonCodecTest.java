package cvdexplorer.io;

import cvdexplorer.metric.MetricKind;
import cvdexplorer.model.CircleMember;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.EllipseMember;
import cvdexplorer.model.LineMember;
import cvdexplorer.model.NeighborOrder;
import cvdexplorer.model.PointMember;
import cvdexplorer.model.SceneState;
import cvdexplorer.model.SegmentMember;
import cvdexplorer.model.SiteMemberKind;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import xyz.marsavic.geometry.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SceneJsonCodecTest {

    @Test
    void encodeDecodeRoundTripPreservesClustersMetricAndMembers() throws Exception {
        SceneState source = new SceneState();
        source.metricKind = MetricKind.MINIMUM_DISTANCE;
        source.neighborOrder = NeighborOrder.FARTHEST;
        source.siteMemberKind = SiteMemberKind.LINE;
        source.clusters().clear();
        source.clusters().add(new ClusterSite(
                "Alpha",
                Color.color(0.2, 0.4, 0.6, 0.95),
                List.of(
                        new PointMember(Vector.xy(10, -20)),
                        new SegmentMember(Vector.xy(0, 0), Vector.xy(100, 5)),
                        new CircleMember(Vector.xy(20, 30), Vector.xy(26, 30)),
                        new LineMember(Vector.xy(-10, 5), Vector.xy(15, 35)),
                        new EllipseMember(
                                Vector.xy(-20, 0),
                                Vector.xy(20, 0),
                                Vector.xy(0, 15)
                        )
                )
        ));
        source.clusters().add(new ClusterSite(
                "Beta",
                Color.hsb(120, 0.5, 0.9),
                List.of(new PointMember(Vector.xy(3, 4)))
        ));
        source.numberOfClusters = source.clusters().size();

        String json = SceneJsonCodec.encode(source);

        SceneState restored = new SceneState();
        restored.metricKind = MetricKind.MINIMUM_DISTANCE;
        restored.siteMemberKind = SiteMemberKind.POINT;
        restored.clusters().add(new ClusterSite("X", Color.BLACK, List.of(new PointMember(Vector.xy(0, 0)))));
        restored.numberOfClusters = 1;

        SceneJsonCodec.applyJson(restored, json);

        assertEquals(MetricKind.MINIMUM_DISTANCE, restored.metricKind);
        assertEquals(NeighborOrder.FARTHEST, restored.neighborOrder);
        assertEquals(SiteMemberKind.LINE, restored.siteMemberKind);
        assertEquals(source.nearestNeighborK, restored.nearestNeighborK);
        assertEquals(2, restored.clusters().size());

        ClusterSite a = restored.clusters().get(0);
        assertEquals("Alpha", a.name());
        assertEquals(5, a.members().size());
        PointMember p = (PointMember) a.members().get(0);
        assertEquals(10.0, p.position().x(), 1e-9);
        assertEquals(-20.0, p.position().y(), 1e-9);
        SegmentMember s = (SegmentMember) a.members().get(1);
        assertEquals(0.0, s.a().x(), 1e-9);
        assertEquals(100.0, s.b().x(), 1e-9);
        CircleMember c = assertInstanceOf(CircleMember.class, a.members().get(2));
        assertEquals(20.0, c.center().x(), 1e-9);
        assertEquals(30.0, c.center().y(), 1e-9);
        assertEquals(6.0, c.radius(), 1e-9);
        LineMember l = (LineMember) a.members().get(3);
        assertEquals(-10.0, l.a().x(), 1e-9);
        EllipseMember e = assertInstanceOf(EllipseMember.class, a.members().get(4));
        assertEquals(-20.0, e.focusA().x(), 1e-9);
        assertEquals(20.0, e.focusB().x(), 1e-9);
        assertEquals(0.0, e.controlHandle().x(), 1e-9);
        assertEquals(15.0, e.controlHandle().y(), 1e-9);

        assertTrue(json.contains("\"kind\": \"CIRCLE\""));
        assertTrue(json.contains("\"kind\": \"ELLIPSE\""));
    }

    @Test
    void rejectsWrongVersion() {
        String json = "{\"version\":\"99\",\"metricKind\":\"MINIMUM_DISTANCE\",\"siteMemberKind\":\"POINT\",\"clusters\":[]}";
        SceneState state = SceneState.demo();
        SceneJsonException ex = assertThrows(SceneJsonException.class, () -> SceneJsonCodec.applyJson(state, json));
        assertTrue(ex.getMessage().contains("version"));
    }

    @Test
    void rejectsNegativeCircleRadius() {
        String json = """
                {
                  "version": "1",
                  "metricKind": "MINIMUM_DISTANCE",
                  "siteMemberKind": "CIRCLE",
                  "clusters": [
                    {
                      "name": "Alpha",
                      "color": {"r": 0.1, "g": 0.2, "b": 0.3, "opacity": 1.0},
                      "members": [
                        {"kind": "CIRCLE", "cx": 1.0, "cy": 2.0, "radius": -4.0}
                      ]
                    }
                  ]
                }
                """;
        SceneState state = SceneState.demo();
        SceneJsonException ex = assertThrows(SceneJsonException.class, () -> SceneJsonCodec.applyJson(state, json));
        assertTrue(ex.getMessage().contains("radius"));
    }

    @Test
    void rejectsEllipseWithoutHandles() {
        String json = """
                {
                  "version": "1",
                  "metricKind": "MINIMUM_DISTANCE",
                  "siteMemberKind": "ELLIPSE",
                  "clusters": [
                    {
                      "name": "Alpha",
                      "color": {"r": 0.1, "g": 0.2, "b": 0.3, "opacity": 1.0},
                      "members": [
                        {"kind": "ELLIPSE", "ax": 0.0, "ay": 0.0}
                      ]
                    }
                  ]
                }
                """;
        SceneState state = SceneState.demo();
        SceneJsonException ex = assertThrows(SceneJsonException.class, () -> SceneJsonCodec.applyJson(state, json));
        assertTrue(ex.getMessage().contains("ELLIPSE"));
    }

    @Test
    void rejectsSumOfDistancesWithCircleMembers() {
        String json = """
                {
                  "version": "1",
                  "metricKind": "SUM_OF_DISTANCES",
                  "siteMemberKind": "POINT",
                  "clusters": [
                    {
                      "name": "Alpha",
                      "color": {"r": 0.1, "g": 0.2, "b": 0.3, "opacity": 1.0},
                      "members": [
                        {"kind": "CIRCLE", "cx": 1.0, "cy": 2.0, "radius": 4.0}
                      ]
                    }
                  ]
                }
                """;
        SceneState state = SceneState.demo();
        SceneJsonException ex = assertThrows(SceneJsonException.class, () -> SceneJsonCodec.applyJson(state, json));
        assertTrue(ex.getMessage().contains("SUM_OF_DISTANCES"));
    }

    @Test
    void rejectsMeanDistanceWithCircleMembers() {
        String json = """
                {
                  "version": "1",
                  "metricKind": "MEAN_DISTANCE",
                  "siteMemberKind": "POINT",
                  "clusters": [
                    {
                      "name": "Alpha",
                      "color": {"r": 0.1, "g": 0.2, "b": 0.3, "opacity": 1.0},
                      "members": [
                        {"kind": "CIRCLE", "cx": 1.0, "cy": 2.0, "radius": 4.0}
                      ]
                    }
                  ]
                }
                """;
        SceneState state = SceneState.demo();
        SceneJsonException ex = assertThrows(SceneJsonException.class, () -> SceneJsonCodec.applyJson(state, json));
        assertTrue(ex.getMessage().contains("MEAN_DISTANCE"));
    }

    @Test
    void legacyAverageDistanceMetricKindInJsonLoadsAsMeanDistance() throws Exception {
        String json = """
                {
                  "version": "1",
                  "metricKind": "AVERAGE_DISTANCE",
                  "siteMemberKind": "POINT",
                  "clusters": [
                    {
                      "name": "Alpha",
                      "color": {"r": 0.1, "g": 0.2, "b": 0.3, "opacity": 1.0},
                      "members": [
                        {"kind": "POINT", "x": 0.0, "y": 0.0}
                      ]
                    }
                  ]
                }
                """;
        SceneState state = SceneState.demo();
        SceneJsonCodec.applyJson(state, json);
        assertEquals(MetricKind.MEAN_DISTANCE, state.metricKind);
    }

    @Test
    void encodeDecodeRoundTripPreservesNearestNeighborK() throws Exception {
        SceneState source = new SceneState();
        source.metricKind = MetricKind.KTH_NEAREST_DISTANCE;
        source.neighborOrder = NeighborOrder.FARTHEST;
        source.siteMemberKind = SiteMemberKind.POINT;
        source.nearestNeighborK = 2;
        source.clusters().clear();
        source.clusters().add(new ClusterSite(
                "A",
                Color.RED,
                List.of(new PointMember(Vector.xy(0, 0)), new PointMember(Vector.xy(10, 0)), new PointMember(Vector.xy(5, 5)))
        ));
        source.clusters().add(new ClusterSite(
                "B",
                Color.BLUE,
                List.of(new PointMember(Vector.xy(100, 0)), new PointMember(Vector.xy(110, 0)))
        ));
        source.numberOfClusters = source.clusters().size();

        String json = SceneJsonCodec.encode(source);
        SceneState restored = SceneState.demo();
        SceneJsonCodec.applyJson(restored, json);

        assertEquals(MetricKind.KTH_NEAREST_DISTANCE, restored.metricKind);
        assertEquals(NeighborOrder.FARTHEST, restored.neighborOrder);
        assertEquals(2, restored.nearestNeighborK);
    }

    @Test
    void missingNeighborOrderInJsonDefaultsToNearest() throws Exception {
        String json = """
                {
                  "version": "1",
                  "metricKind": "MINIMUM_DISTANCE",
                  "siteMemberKind": "POINT",
                  "clusters": [
                    {
                      "name": "Alpha",
                      "color": {"r": 0.1, "g": 0.2, "b": 0.3, "opacity": 1.0},
                      "members": [{"kind": "POINT", "x": 0.0, "y": 0.0}]
                    }
                  ]
                }
                """;
        SceneState state = SceneState.demo();
        state.neighborOrder = NeighborOrder.FARTHEST;
        SceneJsonCodec.applyJson(state, json);
        assertEquals(NeighborOrder.NEAREST, state.neighborOrder);
    }

    @Test
    void rejectsInvalidNearestNeighborK() {
        String json = """
                {
                  "version": "1",
                  "metricKind": "MINIMUM_DISTANCE",
                  "siteMemberKind": "POINT",
                  "nearestNeighborK": 0,
                  "clusters": [
                    {
                      "name": "Alpha",
                      "color": {"r": 0.1, "g": 0.2, "b": 0.3, "opacity": 1.0},
                      "members": [{"kind": "POINT", "x": 0.0, "y": 0.0}]
                    }
                  ]
                }
                """;
        SceneState state = SceneState.demo();
        SceneJsonException ex = assertThrows(SceneJsonException.class, () -> SceneJsonCodec.applyJson(state, json));
        assertTrue(ex.getMessage().contains("nearestNeighborK"));
    }
}
