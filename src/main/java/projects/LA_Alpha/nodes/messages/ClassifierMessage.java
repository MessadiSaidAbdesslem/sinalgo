package projects.LA_Alpha.nodes.messages;

import java.util.HashSet;
import java.util.Set;

import sinalgo.nodes.messages.Message;

public class ClassifierMessage extends Message {

    Integer k;
    Set<Integer> v;

    public ClassifierMessage(Integer k, Set<Integer> v) {
        this.k = k;
        this.v = v;
    }

    @Override
    public Message clone() {
        return new ClassifierMessage(k, new HashSet<>(v));
    }

}
