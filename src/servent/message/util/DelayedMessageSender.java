package servent.message.util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;

/**
 * This worker sends a message asynchronously. Doing this in a separate thread
 * has the added benefit of being able to delay without blocking main or somesuch.
 *
 * @author bmilojkovic
 *
 */
public class DelayedMessageSender implements Runnable {

	private Message messageToSend;

	public DelayedMessageSender(Message messageToSend) {
		this.messageToSend = messageToSend;
	}

	public void run() {
		/*
		 * A random sleep before sending.
		 * It is important to take regular naps for health reasons.
		 */
		try {
			Thread.sleep((long)(Math.random() * 1000) + 500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		if (MessageUtil.MESSAGE_UTIL_PRINTING) {
			AppConfig.timestampedStandardPrint("Sending message " + messageToSend);
		}

		String receiverIp = messageToSend.getReceiverIp();
		int receiverPort = messageToSend.getReceiverPort();

		if (!this.isThatMe(receiverIp, receiverPort)
				&& !messageToSend.getMessageType().equals(MessageType.NEW_NODE)
				&& !messageToSend.getMessageType().equals(MessageType.WELCOME)
				&& !messageToSend.getMessageType().equals(MessageType.UPDATE)) {

			int sendingToId = this.findNodeIdByIpAndPort(messageToSend.getReceiverIp(), messageToSend.getReceiverPort());
			ServentInfo receiver = this.getNextNodeForServentId(sendingToId);

			receiverIp = receiver.getIpAddress();
			receiverPort = receiver.getListenerPort();
		}

		try {
			Socket sendSocket = new Socket(receiverIp, receiverPort);

			ObjectOutputStream oos = new ObjectOutputStream(sendSocket.getOutputStream());

			messageToSend.beforeSending();

			oos.writeObject(messageToSend);
			oos.flush();

			sendSocket.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			AppConfig.timestampedErrorPrint("Couldn't send message: " + messageToSend.toString());
		}
	}

	private boolean isThatMe(String ip, int port) {
		return AppConfig.myServentInfo.getIpAddress().equals(ip)
				&& AppConfig.myServentInfo.getListenerPort() == port;
	}

	private int findNodeIdByIpAndPort(String ip, int port) {
		for (Map.Entry<Integer, ServentInfo> entry : AppConfig.chordState.getAllNodeInfo().entrySet()) {
			if (entry.getValue().getIpAddress().equals(ip) && entry.getValue().getListenerPort() == port) {
				return entry.getValue().getId();
			}
		}
		return -1;
	}

	// TODO: check
	public ServentInfo getNextNodeForServentId(int receiverId) {
		// if it is my successor send directly to it
		if (isServentMySuccessor(receiverId)) {
			return AppConfig.chordState.getAllNodeInfo().get(receiverId);
		}
		ServentInfo[] successorTable = AppConfig.chordState.getSuccessorTable();

		int leftId = successorTable[0].getId();
		for (int i = 1; i < successorTable.length; i++) {
			int rightId = successorTable[i].getId();
			if (isBetweenNodes(leftId, rightId, receiverId)) {
				AppConfig.timestampedStandardPrint("I do not know about receiver "+receiverId+" but will send to "+successorTable[i-1]);
				return successorTable[i-1];
			}
			leftId = rightId;
		}

		if (isBetweenNodes(leftId, successorTable[0].getId(), receiverId)) {
			AppConfig.timestampedStandardPrint("I do not know about receiver "+receiverId+" but will send to "+successorTable[successorTable.length - 1]);
			return successorTable[successorTable.length - 1];
		}

		AppConfig.timestampedStandardPrint("I do not know about receiver "+receiverId+" but will send to "+successorTable[0]);
		return successorTable[0];
	}

	// returns true if we can send message directly to servent
	private boolean isServentMySuccessor(int serventId) {
		for (ServentInfo successor: AppConfig.chordState.getSuccessorTable()) {
			if (successor.getId() == serventId) {
				return true;
			}
		}
		return false;
	}

	private boolean isBetweenNodes(int left, int right, int target) {
		int temp = target;
		while (true) {
			temp = (temp + 1) % AppConfig.chordState.getAllNodeInfo().size();
			if (temp == left) {
				return false;
			}
			if (temp == right) {
				return true;
			}
		}
	}

}
