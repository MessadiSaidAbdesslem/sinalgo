package projects.LA_Gamma.nodes.messages;

import sinalgo.nodes.messages.Message;

public class PhaseARequest extends Message {

    @Override
    public Message clone() {
        return new PhaseARequest();
    }

}
