package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.UpdateMessage;
import servent.message.WelcomeMessage;
import servent.message.util.MessageUtil;

import java.util.HashMap;
import java.util.Map;

public class WelcomeHandler implements MessageHandler {

	private Message clientMessage;

	public WelcomeHandler(Message clientMessage) {
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
		if (clientMessage.getMessageType() != MessageType.WELCOME) {
			AppConfig.timestampedErrorPrint("Welcome handler got a message that is not WELCOME");
			return;
		}

		WelcomeMessage welcomeMsg = (WelcomeMessage)clientMessage;
		AppConfig.myServentInfo.setId(welcomeMsg.getNewId());
		AppConfig.chordState.init(welcomeMsg);

		Map<Integer, ServentInfo> nodesMap = new HashMap<>(AppConfig.chordState.getAllNodeInfo());

		UpdateMessage um = new UpdateMessage(
				AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
				AppConfig.chordState.getNextNodeIp(), AppConfig.chordState.getNextNodePort(),
				nodesMap);

		MessageUtil.sendMessage(um);
	}
}
