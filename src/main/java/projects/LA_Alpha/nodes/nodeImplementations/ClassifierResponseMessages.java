package projects.LA_Alpha.nodes.nodeImplementations;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import sinalgo.nodes.messages.Message;

public class ClassifierResponseMessages extends Message {

    @Getter
    @Setter
    Set<Integer> vi;

    @Getter
    @Setter
    Integer li;

    public ClassifierResponseMessages(Set<Integer> vi, Integer li) {
        this.vi = vi;
        this.li = li;
    }

    @Override
    public Message clone() {
        return new ClassifierResponseMessages(new HashSet<>(vi), li);
    }
}
