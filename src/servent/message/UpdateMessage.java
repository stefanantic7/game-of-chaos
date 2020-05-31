package servent.message;

import app.ServentInfo;

import java.util.HashMap;
import java.util.Map;

public class UpdateMessage extends BasicMessage {

	private static final long serialVersionUID = -6704819659614123646L;

	private final Map<Integer, ServentInfo> nodesInfo;

	public UpdateMessage(String senderIp, int senderPort, String receiverIp, int receiverPort, Map<Integer, ServentInfo> nodesInfo) {
		super(MessageType.UPDATE, senderIp, senderPort, receiverIp, receiverPort);
		this.nodesInfo = new HashMap<>(nodesInfo);
	}

	public Map<Integer, ServentInfo> getNodesInfo() {
		return nodesInfo;
	}
}
