package cvdexplorer.io;

import cvdexplorer.metric.MetricKind;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.PointMember;
import cvdexplorer.model.SceneState;
import cvdexplorer.model.SegmentMember;
import cvdexplorer.model.SiteMemberKind;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import xyz.marsavic.geometry.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Round-trip and validation; file I/O is tested indirectly via the codec string API. */
class SceneJsonCodecTest {

    @Test
    void encodeDecodeRoundTripPreservesClustersMetricAndMembers() throws Exception {
        SceneState source = new SceneState();
        source.metricKind = MetricKind.SUM_OF_DISTANCES;
        source.orderKOneBased = 1;
        source.siteMemberKind = SiteMemberKind.LINE_SEGMENT;
        source.clusters().clear();
        source.clusters().add(new ClusterSite(
                "Alpha",
                Color.color(0.2, 0.4, 0.6, 0.95),
                List.of(
                        new PointMember(Vector.xy(10, -20)),
                        new SegmentMember(Vector.xy(0, 0), Vector.xy(100, 5))
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

        assertEquals(MetricKind.SUM_OF_DISTANCES, restored.metricKind);
        assertEquals(1, restored.orderKOneBased);
        assertEquals(SiteMemberKind.LINE_SEGMENT, restored.siteMemberKind);
        assertEquals(2, restored.clusters().size());

        ClusterSite a = restored.clusters().get(0);
        assertEquals("Alpha", a.name());
        assertEquals(2, a.members().size());
        PointMember p = (PointMember) a.members().get(0);
        assertEquals(10.0, p.position().x(), 1e-9);
        assertEquals(-20.0, p.position().y(), 1e-9);
        SegmentMember s = (SegmentMember) a.members().get(1);
        assertEquals(0.0, s.a().x(), 1e-9);
        assertEquals(100.0, s.b().x(), 1e-9);

        ClusterSite b = restored.clusters().get(1);
        assertEquals("Beta", b.name());
        assertEquals(1, b.size());
    }

    @Test
    void rejectsWrongVersion() {
        String json = "{\"version\":\"99\",\"metricKind\":\"MINIMUM_DISTANCE\",\"siteMemberKind\":\"POINT\",\"clusters\":[]}";
        SceneState state = SceneState.demo();
        SceneJsonException ex = assertThrows(SceneJsonException.class, () -> SceneJsonCodec.applyJson(state, json));
        assertTrue(ex.getMessage().contains("version"));
    }

    @Test
    void missingOrderKDefaultsToOneForOlderFiles() throws Exception {
        String json = """
                {
                  "version": "1",
                  "metricKind": "MINIMUM_DISTANCE",
                  "siteMemberKind": "POINT",
                  "clusters": [
                    {
                      "name": "Alpha",
                      "color": { "r": 0.1, "g": 0.2, "b": 0.3, "opacity": 1.0 },
                      "members": [
                        { "kind": "POINT", "x": 1.0, "y": 2.0 }
                      ]
                    }
                  ]
                }
                """;

        SceneState state = new SceneState();
        SceneJsonCodec.applyJson(state, json);

        assertEquals(1, state.orderKOneBased);
    }

    @Test
    void legacyNeighborOrderFieldStillLoadsIntoOrderK() throws Exception {
        String json = """
                {
                  "version": "1",
                  "metricKind": "MINIMUM_DISTANCE",
                  "neighborOrderOneBased": 2,
                  "siteMemberKind": "POINT",
                  "clusters": [
                    {
                      "name": "Alpha",
                      "color": { "r": 0.1, "g": 0.2, "b": 0.3, "opacity": 1.0 },
                      "members": [
                        { "kind": "POINT", "x": 1.0, "y": 2.0 }
                      ]
                    },
                    {
                      "name": "Beta",
                      "color": { "r": 0.4, "g": 0.5, "b": 0.6, "opacity": 1.0 },
                      "members": [
                        { "kind": "POINT", "x": 3.0, "y": 4.0 }
                      ]
                    },
                    {
                      "name": "Gamma",
                      "color": { "r": 0.7, "g": 0.8, "b": 0.9, "opacity": 1.0 },
                      "members": [
                        { "kind": "POINT", "x": 5.0, "y": 6.0 }
                      ]
                    }
                  ]
                }
                """;

        SceneState state = new SceneState();
        SceneJsonCodec.applyJson(state, json);

        assertEquals(2, state.orderKOneBased);
    }
}
