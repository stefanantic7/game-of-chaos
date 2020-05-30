package servent.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.UpdateMessage;
import servent.message.util.MessageUtil;

public class UpdateHandler implements MessageHandler {

	private Message clientMessage;

	public UpdateHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		try {
			this.handle();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	private void handle() {
		if (clientMessage.getMessageType() != MessageType.UPDATE) {
			AppConfig.timestampedErrorPrint("Update message handler got message that is not UPDATE");
			return;
		}

		UpdateMessage updateMessage = (UpdateMessage) clientMessage;
		Map<Integer, ServentInfo> allNodes = new HashMap<>(updateMessage.getNodesMap());
		allNodes.put(AppConfig.myServentInfo.getId(), AppConfig.myServentInfo);
		AppConfig.chordState.addNodes(allNodes);

		if (!updateMessage.getSenderIp().equals(AppConfig.myServentInfo.getIpAddress())
				|| updateMessage.getSenderPort() != AppConfig.myServentInfo.getListenerPort()) {
			Message nextUpdate = new UpdateMessage(clientMessage.getSenderIp(), clientMessage.getSenderPort(),
					AppConfig.chordState.getNextNodeIp(),  AppConfig.chordState.getNextNodePort(),
					AppConfig.chordState.getAllNodeInfo(), "newMessageText");
			MessageUtil.sendMessage(nextUpdate);
		}
	}

}
