package cvdexplorer.model;

import java.util.ArrayList;
import java.util.List;

public final class ClusterSite {
    private final String name;
    private final Rgba color;
    private final List<ClusterMember> members;

    public ClusterSite(String name, Rgba color, List<ClusterMember> members) {
        this.name = name;
        this.color = color;
        this.members = new ArrayList<>(members);
    }

    public String name() {
        return name;
    }

    public Rgba color() {
        return color;
    }

    public List<ClusterMember> members() {
        return members;
    }

    public int size() {
        return members.size();
    }

    public void addMember(ClusterMember member) {
        members.add(member);
    }

    public void setMember(int index, ClusterMember member) {
        members.set(index, member);
    }

    public void removeMember(int index) {
        members.remove(index);
    }
}
