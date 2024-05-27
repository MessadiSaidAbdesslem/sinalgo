package projects.LA_Alpha.nodes.nodeImplementations;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;

public class NodeData {
    @Getter
    @Setter
    Set<Integer> vi;

    @Getter
    @Setter
    Integer li;

    @Getter
    @Setter
    boolean decided;

    public NodeData(Set<Integer> vi, Integer li, boolean decided) {
        this.vi = vi;
        this.li = li;
        this.decided = decided;
    }

}
