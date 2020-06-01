package servent.message;

import app.JobDetails;
import app.Point;

import java.util.HashSet;
import java.util.Set;

public class ResultMessage extends BasicMessage {
    private static final long serialVersionUID = 2188561658297224254L;

    private final JobDetails jobDetails;
    private final Set<Point> resultPoints;
    private final String fractalId;

    public ResultMessage(String senderIp, int senderPort, String receiverIp, int receiverPort, JobDetails jobDetails, Set<Point> resultPoints, String fractalId) {
        super(MessageType.RESULT, senderIp, senderPort, receiverIp, receiverPort);
        this.jobDetails = jobDetails;
        this.resultPoints = resultPoints;
        this.fractalId = fractalId;
    }

    public Set<Point> getResultPoints() {
        return resultPoints;
    }

    public JobDetails getJobDetails() {
        return jobDetails;
    }

    public boolean hasFractalId() {
        return this.fractalId != null && !this.fractalId.equals("");
    }

    public String getFractalId() {
        return fractalId;
    }
}
