package servent.message;

import java.util.Map;

public class StatusMessage extends BasicMessage {
    private static final long serialVersionUID = -3764526626754500021L;

    private final String jobName;
    private final Map<String, Integer> fractalIdToPointCountMap;

    public StatusMessage(String senderIp, int senderPort, String receiverIp, int receiverPort, String jobName, Map<String, Integer> fractalIdToPointCountMap) {
        super(MessageType.STATUS_MESSAGE, senderIp, senderPort, receiverIp, receiverPort);
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
