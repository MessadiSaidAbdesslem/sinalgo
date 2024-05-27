package projects.LA_Gamma.nodes.nodeImplementations;

import projects.LA_Gamma.nodes.messages.ClassificationResult;
import projects.LA_Gamma.nodes.messages.ClassifierMessage;
import projects.LA_Gamma.nodes.messages.NodeClass;
import projects.LA_Gamma.nodes.messages.PhaseARequest;
import projects.LA_Gamma.nodes.messages.PhaseAResponse;
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

public class LA_GammaNode extends Node {

    boolean defectNextRound = false;

    boolean defect = false;

    Phase currentPhase = Phase.A;

    int guess = 1;

    static Map<Long, NodeData> nodesDataMap = new HashMap<>();

    @Override
    public void handleMessages(Inbox inbox) {
        List<ClassifierResponseMessages> classifierResponseMessages = new ArrayList<>();

        List<PhaseAResponse> phaseAResponses = new ArrayList<>();

        if (!defect) {
            while (inbox.hasNext()) {
                Message message = inbox.next();
                if (message instanceof ClassifierResponseMessages) {
                    classifierResponseMessages.add((ClassifierResponseMessages) message);
                } else if (message instanceof ClassifierMessage) {
                    this.send(new ClassifierResponseMessages(this.getNodeData().vi, this.getNodeData().li),
                            inbox.getSender());
                } else if (message instanceof PhaseARequest) {
                    this.send(new PhaseAResponse(this.getNodeData().vi), inbox.getSender());
                } else if (message instanceof PhaseAResponse) {
                    phaseAResponses.add((PhaseAResponse) message);
                }
            }

            if (!classifierResponseMessages.isEmpty()) {
                this.classifier(ClassifierSteps.RECEIVE_MESSAGE, classifierResponseMessages);
            }

            if (!phaseAResponses.isEmpty()) {
                this.endPhaseA(phaseAResponses);
            }

        }
    }

    @Override
    public void preStep() {
        if (this.currentPhase == Phase.A) {
            this.startPhaseA();
            return;
        }

        // phase B
        if (!this.getNodeData().decided && !defect) {
            this.guess *= 2;

            ClassificationResult res = this.classifier(ClassifierSteps.SEND_MESSAGE, null);
            if (res == null) {
                return;
            }
            if (res.isDecided()) {
                this.getNodeData().decided = res.isDecided();
                this.getNodeData().vi = res.getNextRoundValue();
            } else if (res.getClazz() == NodeClass.MASTER) {
                int r = Tools.getRuntime().getNumberOfRounds();
                Integer li_ = this.getNodeData().li + (int) (this.guess / Math.pow(2, r + 1));
                this.getNodeData().li = li_;
            } else {
                int r = Tools.getRuntime().getNumberOfRounds();
                Integer li_ = this.getNodeData().li - (int) (this.guess / Math.pow(2, r + 1));
                this.getNodeData().li = li_;
            }
        }
    }

    @Override
    public void init() {
        Integer li = this.guess;
        Integer setSize = (int) (Math.random() * (3 + 1));
        boolean decided = false;
        Set<Integer> vi = new HashSet<Integer>();
        for (int i = 0; i < setSize; i++) {
            Integer randomValue = (int) (Math.random() * (10000 + 1));
            vi.add(randomValue);
        }
        NodeData nodeData = new NodeData(vi, li, decided);
        nodesDataMap.put(this.getID(), nodeData);
    }

    @Override
    public void neighborhoodChange() {
    }

    @Override
    public void postStep() {
        if (defectNextRound) {
            defect = true;
        } else {
            defect = false;
        }
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
            this.getNodeData().li = this.guess;
            Integer k = this.getNodeData().li;

            if (h_w > k) {
                int r = Tools.getRuntime().getNumberOfRounds();
                Integer li_ = this.getNodeData().li + (int) (this.guess / Math.pow(2, r + 1));
                this.getNodeData().li = li_;
                this.setColor(Color.green);
                this.getNodeData().vi = new HashSet<>(w);

                return new ClassificationResult(w, NodeClass.MASTER, false);
            } else {
                int r = Tools.getRuntime().getNumberOfRounds();
                Integer li_ = this.getNodeData().li - (int) (this.guess / Math.pow(2, r + 1));
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

    private void startPhaseA() {
        this.getOutgoingConnections().iterator().forEachRemaining((e) -> {
            this.send(new PhaseARequest(), e.getEndNode());
        });
    }

    private void endPhaseA(List<PhaseAResponse> responses) {
        Set<Integer> newVi = new HashSet<>();

        for (PhaseAResponse response : responses) {
            newVi.addAll(response.getVi());
        }

        this.getNodeData().vi = new HashSet<>(newVi);
        this.currentPhase = Phase.B;
    }

    private void sendClassifierMessage() {
        // MessageTimer msgTimer = new MessageTimer(new
        // ClassifierMessage(this.getNodeData().li, this.getNodeData().vi));

        if (defectNextRound || defect) {
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

    @NodePopupMethod(menuText = "Toggle Defect Node")
    public void togleDefect() {
        if (defectNextRound) {
            defectNextRound = false;
            this.setColor(Color.black);
        } else {
            defectNextRound = true;
            this.setColor(Color.red);
        }
    }

}
