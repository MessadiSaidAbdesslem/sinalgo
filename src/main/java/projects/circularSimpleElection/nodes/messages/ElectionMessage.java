package projects.circularSimpleElection.nodes.messages;

import lombok.Getter;
import sinalgo.nodes.messages.Message;

public class ElectionMessage extends Message {

    @Getter
    private long id;

    public ElectionMessage(long id) {
        this.id = id;
    }

    @Override
    public Message clone() {
        return new ElectionMessage(this.id);
    }

}
