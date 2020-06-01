package servent.message;

public class StopJobMessage extends BasicMessage {

    private static final long serialVersionUID = 1676476083861820600L;

    private final String jobName;

    public StopJobMessage(String senderIp, int senderPort, String receiverIp, int receiverPort, String jobName) {
        super(MessageType.STOP_JOB, senderIp, senderPort, receiverIp, receiverPort);
        this.jobName = jobName;
    }

    public String getJobName() {
        return jobName;
    }
}
