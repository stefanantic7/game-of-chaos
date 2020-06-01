package servent.message;

import app.Point;

import java.util.HashSet;
import java.util.Set;

public class AskForResultMessage extends BasicMessage {
    private static final long serialVersionUID = -1723360019285468981L;

    private final String jobName;
    private final Set<Point> appendedResults;

    private final String fractalId;

    public AskForResultMessage(String senderIp, int senderPort, String receiverIp, int receiverPort, String jobName) {
        super(MessageType.ASK_FOR_RESULT, senderIp, senderPort, receiverIp, receiverPort);
        this.jobName = jobName;
        this.appendedResults = new HashSet<>();
        this.fractalId = null;
    }

    public AskForResultMessage(String senderIp, int senderPort, String receiverIp, int receiverPort, String jobName, Set<Point> appendedResults) {
        super(MessageType.ASK_FOR_RESULT, senderIp, senderPort, receiverIp, receiverPort);
        this.jobName = jobName;
        this.appendedResults = appendedResults;
        this.fractalId = null;
    }

    public AskForResultMessage(String senderIp, int senderPort, String receiverIp, int receiverPort, String jobName, String fractalId) {
        super(MessageType.ASK_FOR_RESULT, senderIp, senderPort, receiverIp, receiverPort);
        this.jobName = jobName;
        this.appendedResults = new HashSet<>();
        this.fractalId = fractalId;
    }

    public String getJobName() {
        return jobName;
    }

    public Set<Point> getAppendedResults() {
        return appendedResults;
    }

    public boolean hasFractalId() {
        return this.fractalId != null && !this.fractalId.equals("");
    }

    public String getFractalId() {
        return fractalId;
    }
}
