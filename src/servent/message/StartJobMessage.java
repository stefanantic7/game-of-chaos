package servent.message;

import app.Job;
import app.Point;

import java.util.List;
import java.util.Map;

public class StartJobMessage extends BasicMessage {

    private static final long serialVersionUID = 129440548111564517L;

    private final List<String> fractalIds;
    private final List<Point> initialPoints;
    private final Job job;
    private final int level;
    private final Map<String, Integer> fractalIdToNodeIdMap;

    public StartJobMessage(String senderIp, int senderPort,
                           String receiverIp, int receiverPort,
                           List<String> fractalIds, List<Point> initialPoints, Job job, int level, Map<String, Integer> fractalIdToNodeIdMap) {
        super(MessageType.START_JOB, senderIp, senderPort, receiverIp, receiverPort);
        this.fractalIds = fractalIds;
        this.initialPoints = initialPoints;
        this.job = job;
        this.level = level;
        this.fractalIdToNodeIdMap = fractalIdToNodeIdMap;
    }

    public List<String> getFractalIds() {
        return fractalIds;
    }

    public List<Point> getInitialPoints() {
        return initialPoints;
    }

    public Job getJob() {
        return job;
    }

    public int getLevel() {
        return level;
    }

    public Map<String, Integer> getFractalIdToNodeIdMap() {
        return fractalIdToNodeIdMap;
    }
}
