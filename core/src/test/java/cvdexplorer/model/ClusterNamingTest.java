package cvdexplorer.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ClusterNamingTest {
    @Test
    void knownHuesMapToExpectedNames() {
        assertEquals("Amber", ClusterNaming.fromHue(30));
        assertEquals("Azure", ClusterNaming.fromHue(210));
        assertEquals("Rose", ClusterNaming.fromHue(330));
        assertEquals("Lime", ClusterNaming.fromHue(110));
    }

    @Test
    void newClusterNamesAreNonEmptyColorWords() {
        for (int i = 0; i < 16; i++) {
            String name = ClusterNaming.forNewCluster(i);
            assertFalse(name.isBlank());
            assertFalse(name.startsWith("Cluster "));
        }
    }
}
