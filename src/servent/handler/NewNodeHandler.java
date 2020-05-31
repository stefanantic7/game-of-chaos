package servent.handler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.NewNodeMessage;
import servent.message.WelcomeMessage;
import servent.message.util.MessageUtil;

public class NewNodeHandler implements MessageHandler {

	private Message clientMessage;

	public NewNodeHandler(Message clientMessage) {
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
		if (clientMessage.getMessageType() != MessageType.NEW_NODE) {
			AppConfig.timestampedErrorPrint("NEW_NODE handler got something that is not new node message.");
			return;
		}

		NewNodeMessage newNodeMessage = (NewNodeMessage) clientMessage;
		int newNodePort = newNodeMessage.getSenderPort();
		String newNodeIpAddress = newNodeMessage.getSenderIp();

		ServentInfo firstServent = AppConfig.myServentInfo;
		if (AppConfig.chordState.getSuccessorTable()[0] != null) {
			firstServent = AppConfig.chordState.getSuccessorTable()[0];
		}

		String firstServentIp = firstServent.getIpAddress();
		int firstServentPort = firstServent.getListenerPort();
		String firstServentIpAndPort = firstServentIp + ":" + firstServentPort;

		int newNodeId = AppConfig.myServentInfo.getId() + 1;

		WelcomeMessage wm = new WelcomeMessage(
				AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
				newNodeIpAddress, newNodePort,
				newNodeId, firstServentIpAndPort);
		MessageUtil.sendMessage(wm);
	}

}
