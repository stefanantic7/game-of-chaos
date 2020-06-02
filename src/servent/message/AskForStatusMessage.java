package servent.message;

import java.util.Map;

public class AskForStatusMessage extends BasicMessage {

    private static final long serialVersionUID = 1800523321250703314L;

    private final String jobName;
    private final Map<String, Integer> fractalIdToPointCountMap;

    public AskForStatusMessage(String senderIp, int senderPort, String receiverIp, int receiverPort, String jobName, Map<String, Integer> fractalIdToPointCountMap) {
        super(MessageType.ASK_FOR_STATUS, senderIp, senderPort, receiverIp, receiverPort);
        this.jobName = jobName;
        this.fractalIdToPointCountMap = fractalIdToPointCountMap;
    }

    public String getJobName() {
        return jobName;
    }

    public Map<String, Integer> getFractalIdToPointCountMap() {
        return fractalIdToPointCountMap;
    }
}
