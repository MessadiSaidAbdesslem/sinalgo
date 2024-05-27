package projects.LA_Gamma.nodes.messages;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import sinalgo.nodes.messages.Message;

public class PhaseAResponse extends Message {
    @Getter
    Set<Integer> vi;

    public PhaseAResponse(Set<Integer> vi) {
        this.vi = vi;
    }

    @Override
    public Message clone() {
        return new PhaseAResponse(new HashSet<>(vi));
    }

}
