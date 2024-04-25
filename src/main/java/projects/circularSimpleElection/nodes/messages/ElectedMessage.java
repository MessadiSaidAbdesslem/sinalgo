package projects.circularSimpleElection.nodes.messages;

import lombok.Getter;
import sinalgo.nodes.messages.Message;

public class ElectedMessage extends Message {

    @Getter
    private long id;

    public ElectedMessage(long id) {
        this.id = id;
    }

    @Override
    public Message clone() {
        return new ElectedMessage(this.id);
    }

}
