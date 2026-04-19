package cvdexplorer;

import cvdexplorer.model.CircleMember;
import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.LineMember;

public final class HandleVisibility {
    private HandleVisibility() {
    }

    public static boolean isVisible(ClusterMember member, int handleIndex, boolean memberSelected) {
        if (member instanceof CircleMember || member instanceof LineMember) {
            return handleIndex == 0 || memberSelected;
        }
        return true;
    }
}
