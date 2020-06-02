package servent.handler;

import java.util.HashMap;
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
			AppConfig.timestampedErrorPrint("Handler got message that is not UPDATE");
			return;
		}

		UpdateMessage updateMessage = (UpdateMessage) clientMessage;

		Map<Integer, ServentInfo> allNodes = new HashMap<>(updateMessage.getNodesInfo());
		allNodes.put(AppConfig.myServentInfo.getId(), AppConfig.myServentInfo);

		AppConfig.chordState.mergeNodesInfoAndUpdateSuccessors(allNodes);

		if (!clientMessage.getSenderIp().equals(AppConfig.myServentInfo.getIpAddress())
				|| clientMessage.getSenderPort() != AppConfig.myServentInfo.getListenerPort()) {
			Message updateNextNodeMessage = new UpdateMessage(
					clientMessage.getSenderIp(), clientMessage.getSenderPort(),
					AppConfig.chordState.getNextNodeIp(),  AppConfig.chordState.getNextNodePort(),
					AppConfig.chordState.getAllNodeInfo());
			MessageUtil.sendMessage(updateNextNodeMessage);
		}
	}

}
