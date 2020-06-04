package servent.message;

import app.Job;
import app.Point;
import app.ServentInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UpdateMessage extends BasicMessage {

	private static final long serialVersionUID = -6704819659614123646L;

	private final Map<Integer, ServentInfo> nodesInfo;

	private final Job activeJob;
	private final Set<Point> computedPoints;

	public UpdateMessage(String senderIp, int senderPort, String receiverIp, int receiverPort, Map<Integer, ServentInfo> nodesInfo, Job activeJob, Set<Point> computedPoints) {
		super(MessageType.UPDATE, senderIp, senderPort, receiverIp, receiverPort);
		this.nodesInfo = new HashMap<>(nodesInfo);

		this.activeJob = activeJob;
		this.computedPoints = computedPoints;
	}

	public Map<Integer, ServentInfo> getNodesInfo() {
		return nodesInfo;
	}

	public Job getActiveJob() {
		return activeJob;
	}

	public Set<Point> getComputedPoints() {
		return computedPoints;
	}
}
