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
			AppConfig.timestampedErrorPrint("Handler got a message that is not WELCOME");
			return;
		}

		WelcomeMessage welcomeMessage = (WelcomeMessage)clientMessage;
		AppConfig.myServentInfo.setId(welcomeMessage.getNewId());
		AppConfig.chordState.init(welcomeMessage);

		UpdateMessage updateMessage = new UpdateMessage(
				AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
				AppConfig.chordState.getNextNodeIp(), AppConfig.chordState.getNextNodePort(),
				AppConfig.chordState.getAllNodeInfo());

		MessageUtil.sendMessage(updateMessage);
	}
}
