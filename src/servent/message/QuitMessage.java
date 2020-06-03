package servent.message;

import app.Job;
import app.Point;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class QuitMessage extends BasicMessage {

    private static final long serialVersionUID = 549279197594251394L;

    private final int quitterId;

    private final Job activeJob;
    private final Set<Point> computedPoints;

    public QuitMessage(String senderIp, int senderPort,
                       String receiverIp, int receiverPort,
                       int quitterId) {
        super(MessageType.QUIT, senderIp, senderPort, receiverIp, receiverPort);
        this.quitterId = quitterId;

        this.activeJob = null;
        this.computedPoints = new HashSet<>();
    }

    public QuitMessage(String senderIp, int senderPort,
                       String receiverIp, int receiverPort,
                       int quitterId, Job activeJob, Set<Point> computedPoints) {
        super(MessageType.QUIT, senderIp, senderPort, receiverIp, receiverPort);
        this.quitterId = quitterId;

        this.activeJob = activeJob;
        this.computedPoints = new HashSet<>(computedPoints);
    }

    public int getQuitterId() { return quitterId; }

    public Job getActiveJob() {
        return activeJob;
    }

    public Set<Point> getComputedPoints() {
        return computedPoints;
    }
}
