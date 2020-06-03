package servent.message;

import app.Job;
import app.Point;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class StartJobMessage extends BasicMessage {

    private static final long serialVersionUID = 129440548111564517L;

    private final List<String> fractalIds;
    private final List<Point> initialPoints;
    private final Job job;
    private final int level;
    private final Map<String, Integer> fractalIdToNodeIdMap;

    private final Set<Point> precomputedPoints;

    public StartJobMessage(String senderIp, int senderPort,
                           String receiverIp, int receiverPort,
                           List<String> fractalIds, List<Point> initialPoints, Job job, int level, Map<String, Integer> fractalIdToNodeIdMap, Set<Point> precomputedPoints) {
        super(MessageType.START_JOB, senderIp, senderPort, receiverIp, receiverPort);
        this.fractalIds = fractalIds;
        this.initialPoints = initialPoints;
        this.job = job;
        this.level = level;
        this.fractalIdToNodeIdMap = fractalIdToNodeIdMap;

        this.precomputedPoints = precomputedPoints;
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

    public Set<Point> getPrecomputedPoints() {
        return precomputedPoints;
    }
}
