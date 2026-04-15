package cvdexplorer;

import cvdexplorer.model.CircleMember;
import cvdexplorer.model.ClusterMember;

public final class HandleVisibility {
    private HandleVisibility() {
    }

    public static boolean isVisible(ClusterMember member, int handleIndex, boolean memberSelected) {
        if (member instanceof CircleMember) {
            return handleIndex == 0 || memberSelected;
        }
        return true;
    }
}
