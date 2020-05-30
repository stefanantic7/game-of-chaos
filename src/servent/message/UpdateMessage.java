package servent.message;

import app.ServentInfo;

import java.util.Map;

public class UpdateMessage extends BasicMessage {

	private static final long serialVersionUID = 3586102505319194978L;

	private Map<Integer, ServentInfo> nodesMap;

	public UpdateMessage(String senderIp, int senderPort, String receiverIp, int receiverPort, Map<Integer, ServentInfo> nodesMap, String text) {
		super(MessageType.UPDATE, senderIp, senderPort, receiverIp, receiverPort, text);
		this.nodesMap = nodesMap;
	}

	public Map<Integer, ServentInfo> getNodesMap() {
		return nodesMap;
	}
}
