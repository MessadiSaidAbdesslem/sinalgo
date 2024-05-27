package projects.LA_Alpha.nodes.nodeImplementations;

import projects.LA_Alpha.CustomGlobal;
import projects.LA_Alpha.nodes.messages.ClassificationResult;
import projects.LA_Alpha.nodes.messages.ClassifierMessage;
import projects.LA_Alpha.nodes.messages.NodeClass;
import sinalgo.exception.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;

public class LA_AlphaNode extends Node {
    static final int H = 20;

    static Map<Long, NodeData> nodesDataMap = new HashMap<>();

    Nodestatus nodeState = Nodestatus.READY;

    @Override
    public void handleMessages(Inbox inbox) {
        List<ClassifierResponseMessages> classifierResponseMessages = new ArrayList<>();

        // if it is defect it doesn't receive any messages
        if (this.isDefect()) {
            while (inbox.hasNext()) {
                inbox.next();
            }
            return;
        }

        while (inbox.hasNext()) {
            Message message = inbox.next();
            if (message instanceof ClassifierResponseMessages) {
                classifierResponseMessages.add((ClassifierResponseMessages) message);
            } else if (message instanceof ClassifierMessage) {
                this.send(new ClassifierResponseMessages(this.getNodeData().vi, this.getNodeData().li),
                        inbox.getSender());
            }
        }

        if (!classifierResponseMessages.isEmpty()) {
            this.classifier(ClassifierSteps.RECEIVE_MESSAGE, classifierResponseMessages);
        }

    }

    private boolean isDefect() {
        boolean defective = (Tools.getGlobalTime() % (this.getID() + 1)) == 0;

        if (defective) {
            this.setColor(Color.GRAY);
        } else {
            this.setColor(Color.black);
        }

        return defective;
    }

    @Override
    public void preStep() {
        if (!this.getNodeData().decided && !this.isDefect()) {
            ClassificationResult res = this.classifier(ClassifierSteps.SEND_MESSAGE, null);
            if (res == null) {
                return;
            }
            if (res.isDecided()) {
                this.getNodeData().decided = res.isDecided();
                this.getNodeData().vi = res.getNextRoundValue();
            } else if (res.getClazz() == NodeClass.MASTER) {
                int r = Tools.getRuntime().getNumberOfRounds();
                Integer li_ = this.getNodeData().li + (int) (H / Math.pow(2, r + 1));
                this.getNodeData().li = li_;
            } else {
                int r = Tools.getRuntime().getNumberOfRounds();
                Integer li_ = this.getNodeData().li - (int) (H / Math.pow(2, r + 1));
                this.getNodeData().li = li_;
            }
        }
    }

    @Override
    public void init() {
        Integer li = H / 2;
        // Integer setSize = (int) (Math.random() * (H + 1));
        boolean decided = false;
        Set<Integer> vi = new HashSet<Integer>();
        vi.add(((int) this.getID()));
        // for (int i = 0; i < setSize; i++) {
        // Integer randomValue = (int) (Math.random() * (H + 1));
        // vi.add(randomValue);
        // }
        NodeData nodeData = new NodeData(vi, li, decided);
        nodesDataMap.put(this.getID(), nodeData);
    }

    @Override
    public void neighborhoodChange() {
    }

    @Override
    public void postStep() {
    }

    @Override
    public void checkRequirements() throws WrongConfigurationException {
    }

    private ClassificationResult classifier(ClassifierSteps step, List<ClassifierResponseMessages> receivedMessages) {
        if (step == ClassifierSteps.SEND_MESSAGE) {
            this.sendClassifierMessage();
            return null;
        }

        if (step == ClassifierSteps.RECEIVE_MESSAGE) {
            List<Set<Integer>> U = new ArrayList<>();

            for (int i = 0; i < receivedMessages.size(); i++) {
                ClassifierResponseMessages currentMessage = receivedMessages.get(i);
                U.add(currentMessage.vi);
            }

            if (U.size() == 0 || this.inclusionTest(U)) {
                this.getNodeData().decided = true;
                return new ClassificationResult(nodesDataMap.get(this.getID()).vi, NodeClass.UNDEFINED, true);
            }

            Set<Integer> w = new HashSet<>();

            for (Set<Integer> x : U) {
                w.addAll(x);
            }

            Integer h_w = w.size();
            Integer k = nodesDataMap.get(this.getID()).li;

            if (h_w > k) {
                int r = Tools.getRuntime().getNumberOfRounds();
                Integer li_ = this.getNodeData().li + (int) (H / Math.pow(2, r + 1));
                this.getNodeData().li = li_;
                this.setColor(Color.green);
                this.getNodeData().vi = new HashSet<>(w);

                return new ClassificationResult(w, NodeClass.MASTER, false);
            } else {
                int r = Tools.getRuntime().getNumberOfRounds();
                Integer li_ = this.getNodeData().li - (int) (H / Math.pow(2, r + 1));
                this.getNodeData().li = li_;
                this.setColor(Color.CYAN);

                return new ClassificationResult(this.getNodeData().vi, NodeClass.SLAVE, false);
            }
        }

        return null;
    }

    private boolean inclusionTest(List<Set<Integer>> U) {
        Set<Integer> myVi = this.getNodeData().vi;
        return U.stream().allMatch((data) -> {
            return data.containsAll(myVi) || myVi.containsAll(data);
        });
    }

    private NodeData getNodeData() {
        return nodesDataMap.get(this.getID());
    }

    private void sendClassifierMessage() {
        // MessageTimer msgTimer = new MessageTimer(new
        // ClassifierMessage(this.getNodeData().li, this.getNodeData().vi));

        if (this.isDefect()) {
            int size = (int) (Math.random() * (Tools.getNodeList().size() - 1));

            for (int i = 0; i < size; i++) {
                this.send(new ClassifierMessage(this.getNodeData().li, this.getNodeData().vi),
                        Tools.getNodeList().getRandomNode());
            }

        } else {
            for (Node node : Tools.getNodeList()) {
                this.send(new ClassifierMessage(this.getNodeData().li, this.getNodeData().vi), node);
            }
        }
        // msgTimer.startRelative(1, this);
    }

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        super.drawNodeAsDiskWithText(g, pt, highlight, this.toString(), 20, Color.white);
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Li:" + this.getNodeData().li);
        strBuilder.append(" |  vi:" + Arrays.toString(this.getNodeData().vi.stream().toArray()));
        strBuilder.append(" | Id:" + getID());
        strBuilder.append(" | decided:" + this.getNodeData().decided);
        return strBuilder.toString();
    }

}
