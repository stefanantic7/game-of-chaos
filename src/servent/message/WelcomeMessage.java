package servent.message;

import java.util.Map;

public class WelcomeMessage extends BasicMessage {

	private static final long serialVersionUID = -8981406250652693908L;

	private final int newId;

	private final String firstServentIpAndPort;

	public WelcomeMessage(String senderIp, int senderPort, String receiverIp, int receiverPort, int newId, String firstServentIpAndPort) {
		super(MessageType.WELCOME, senderIp, senderPort, receiverIp, receiverPort);

		this.newId = newId;
		this.firstServentIpAndPort = firstServentIpAndPort;
	}

	public int getNewId() {
		return newId;
	}

	public String getFirstServentIpAndPort() {
		return firstServentIpAndPort;
	}
}
