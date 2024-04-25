package projects.circularSimpleElection.nodes.nodeImplementations;

import java.awt.Color;

import lombok.Getter;
import lombok.Setter;
import projects.circularSimpleElection.nodes.messages.ElectedMessage;
import projects.circularSimpleElection.nodes.messages.ElectionMessage;
import projects.defaultProject.nodes.timers.MessageTimer;
import sinalgo.exception.WrongConfigurationException;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

public class NodeImpl extends Node {

    @Getter
    @Setter
    private boolean partI = false;

    @Getter
    @Setter
    private boolean done = false;

    @Getter
    @Setter
    private boolean elected;

    @Getter
    @Setter
    private long leaderId;

    @Override
    public void checkRequirements() throws WrongConfigurationException {
    }

    @Override
    public void handleMessages(Inbox inbox) {
        while (inbox.hasNext()) {
            Message msg = inbox.next();
            if (msg instanceof ElectionMessage) {
                ElectionMessage electionMessage = (ElectionMessage) msg;
                System.out.println(electionMessage.getId());
                long id = electionMessage.getId();
                if (id > this.getID()) {
                    this.partI = true;
                    this.sendElection(id);
                } else if (id < this.getID()) {
                    if (!this.partI) {
                        this.partI = true;
                        this.sendElection(this.getID());
                    }
                } else if (id == this.getID()) {
                    this.elected = true;
                    this.setColor(Color.RED);
                    Tools.appendToOutput("new leader is :" + id);
                    this.sendElected(id);
                }
            }

            if (msg instanceof ElectedMessage) {
                ElectedMessage electedMessage = (ElectedMessage) msg;
                long id = electedMessage.getId();
                this.leaderId = id;
                this.done = true;

                Tools.appendToOutput("\n " + this.getID() + "received that " + id + " is the new leader");

                if (this.getID() != id) {
                    this.elected = false;
                    this.sendElected(id);
                }

            }

        }
    }

    @Override
    public void init() {
    }

    @Override
    public void neighborhoodChange() {
    }

    @Override
    public void postStep() {
    }

    @Override
    public void preStep() {
    }

    @NodePopupMethod(menuText = "Start election")
    public void start() {
        if (!partI) {
            partI = true;
            this.sendElection(getID());
        }
    }

    private void sendElection(long id) {

        MessageTimer msgTimer = new MessageTimer(new ElectionMessage(id));

        msgTimer.startRelative(1, this);
    }

    private void sendElected(long id) {
        MessageTimer msgTimer = new MessageTimer(new ElectedMessage(id));

        msgTimer.startRelative(id, this);
    }

}
