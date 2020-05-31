package servent.message;

import app.Point;

import java.util.HashSet;
import java.util.Set;

public class ResultMessage extends BasicMessage {
    private static final long serialVersionUID = 2188561658297224254L;

    private Set<Point> resultPoints;

    public ResultMessage(String senderIp, int senderPort, String receiverIp, int receiverPort, Set<Point> resultPoints) {
        super(MessageType.RESULT, senderIp, senderPort, receiverIp, receiverPort);
        this.resultPoints = resultPoints;
    }

    public Set<Point> getResultPoints() {
        return resultPoints;
    }
}
