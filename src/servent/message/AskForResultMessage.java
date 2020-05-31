package servent.message;

import app.Point;

import java.util.HashSet;
import java.util.Set;

public class AskForResultMessage extends BasicMessage {
    private static final long serialVersionUID = -1723360019285468981L;

    private final Set<Point> appendedResults;

    public AskForResultMessage(String senderIp, int senderPort, String receiverIp, int receiverPort) {
        super(MessageType.ASK_FOR_RESULT, senderIp, senderPort, receiverIp, receiverPort);
        this.appendedResults = new HashSet<>();
    }

    public AskForResultMessage(String senderIp, int senderPort, String receiverIp, int receiverPort, Set<Point> appendedResults) {
        super(MessageType.ASK_FOR_RESULT, senderIp, senderPort, receiverIp, receiverPort);
        this.appendedResults = appendedResults;
    }

    public Set<Point> getAppendedResults() {
        return appendedResults;
    }
}
