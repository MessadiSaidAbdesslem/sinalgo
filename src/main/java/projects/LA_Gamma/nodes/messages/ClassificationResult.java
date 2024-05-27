package projects.LA_Gamma.nodes.messages;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;

public class ClassificationResult {
    @Getter
    @Setter
    Set<Integer> nextRoundValue;

    @Getter
    @Setter
    NodeClass clazz;

    @Setter
    @Getter
    boolean decided;

    public ClassificationResult(Set<Integer> nextRoundValue, NodeClass clazz, boolean decided) {
        this.nextRoundValue = nextRoundValue;
        this.clazz = clazz;
        this.decided = decided;
    }

}
