package app;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import servent.message.WelcomeMessage;
import servent.message.util.MessageUtil;

/**
 * This class implements all the logic required for Chord to function.
 * It has a static method <code>chordHash</code> which will calculate our chord ids.
 * It also has a static attribute <code>CHORD_SIZE</code> that tells us what the maximum
 * key is in our system.
 *
 * Other public attributes and methods:
 * <ul>
 *   <li><code>chordLevel</code> - log_2(CHORD_SIZE) - size of <code>successorTable</code></li>
 *   <li><code>successorTable</code> - a map of shortcuts in the system.</li>
 *   <li><code>predecessorInfo</code> - who is our predecessor.</li>
 *   <li><code>valueMap</code> - DHT values stored on this node.</li>
 *   <li><code>init()</code> - should be invoked when we get the WELCOME message.</li>
 *   <li><code>isCollision(int chordId)</code> - checks if a servent with that Chord ID is already active.</li>
 *   <li><code>isKeyMine(int key)</code> - checks if we have a key locally.</li>
 *   <li><code>getNextNodeForKey(int key)</code> - if next node has this key, then return it, otherwise returns the nearest predecessor for this key from my successor table.</li>
 *   <li><code>addNodes(List<ServentInfo> nodes)</code> - updates the successor table.</li>
 *   <li><code>putValue(int key, int value)</code> - stores the value locally or sends it on further in the system.</li>
 *   <li><code>getValue(int key)</code> - gets the value locally, or sends a message to get it from somewhere else.</li>
 * </ul>
 * @author bmilojkovic
 *
 */
public class ChordState {

	public static int CHORD_SIZE;
	public static int chordHash(int value) {
		return 61 * value % CHORD_SIZE;
	}

	private int chordLevel; //log_2(CHORD_SIZE)

	private ServentInfo[] successorTable;
	private ServentInfo predecessorInfo;

	//we DO NOT use this to send messages, but only to construct the successor table
	private Map<Integer, ServentInfo> allNodeInfo;

	public ChordState() {
		this.chordLevel = 1;
		int tmp = CHORD_SIZE;
		while (tmp != 2) {
			if (tmp % 2 != 0) { //not a power of 2
				throw new NumberFormatException();
			}
			tmp /= 2;
			this.chordLevel++;
		}

		successorTable = new ServentInfo[chordLevel];
		for (int i = 0; i < chordLevel; i++) {
			successorTable[i] = null;
		}

		predecessorInfo = null;
		allNodeInfo = new HashMap<>();
	}

	/**
	 * This should be called once after we get <code>WELCOME</code> message.
	 * It sets up our initial value map and our first successor so we can send <code>UPDATE</code>.
	 * It also lets bootstrap know that we did not collide.
	 */
	public void init(WelcomeMessage welcomeMsg) {
		// set as predecessor the node who sent the message
		predecessorInfo =  new ServentInfo(welcomeMsg.getSenderIp(), welcomeMsg.getSenderPort());
		// set as first successor servent with id 0, for sending of update message
		String firstServentIp = welcomeMsg.getFirstServentIpAndPort().split(":")[0];
		int firstServentPort = Integer.parseInt(welcomeMsg.getFirstServentIpAndPort().split(":")[1]);
		successorTable[0] = new ServentInfo(firstServentIp, firstServentPort);

		allNodeInfo.put(AppConfig.myServentInfo.getId(), AppConfig.myServentInfo);
		AppConfig.timestampedStandardPrint(allNodeInfo.toString());

		//tell bootstrap this node is not a collider
		try {
			Socket bsSocket = new Socket(AppConfig.BOOTSTRAP_IP, AppConfig.BOOTSTRAP_PORT);

			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("New\n" + AppConfig.myServentInfo.getIpAddress() + ":" + AppConfig.myServentInfo.getListenerPort() + "\n");

			bsWriter.flush();
			bsSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getChordLevel() {
		return chordLevel;
	}

	public ServentInfo[] getSuccessorTable() {
		return successorTable;
	}

	public int getNextNodePort() {
		return successorTable[0].getListenerPort();
	}

	public String getNextNodeIp() {
		return successorTable[0].getIpAddress();
	}

	public ServentInfo getPredecessor() {
		return predecessorInfo;
	}

	public void setPredecessor(ServentInfo newNodeInfo) {
		this.predecessorInfo = newNodeInfo;
	}

	/**
	 * Returns true if we are the owner of the specified key.
	 */
	public boolean isKeyMine(int key) {
		if (predecessorInfo == null) {
			return true;
		}

		int predecessorChordId = predecessorInfo.getChordId();
		int myChordId = AppConfig.myServentInfo.getChordId();

		if (predecessorChordId < myChordId) { //no overflow
			if (key <= myChordId && key > predecessorChordId) {
				return true;
			}
		} else { //overflow
			if (key <= myChordId || key > predecessorChordId) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Main chord operation - find the nearest node to hop to to find a specific key.
	 * We have to take a value that is smaller than required to make sure we don't overshoot.
	 * We can only be certain we have found the required node when it is our first next node.
	 */
	public ServentInfo getNextNodeForKey(int key) {
		if (isKeyMine(key)) {
			return AppConfig.myServentInfo;
		}

		int previousId = successorTable[0].getChordId();
		for (int i = 1; i < successorTable.length; i++) {
			if (successorTable[i] == null) {
				AppConfig.timestampedErrorPrint("Couldn't find successor for " + key);
				break;
			}

			int successorId = successorTable[i].getChordId();

			if (successorId >= key) {
				return successorTable[i-1];
			}
			if (key > previousId && successorId < previousId) { //overflow
				return successorTable[i-1];
			}
			previousId = successorId;
		}
		//if we have only one node in all slots in the table, we might get here
		//then we can return any item
		return successorTable[0];
	}

	private void calculateChordLevel() {
		this.chordLevel = 1;
		int tmp = allNodeInfo.size();
		while (tmp > 2) {
			tmp /= 2;
			this.chordLevel++;
		}

		successorTable = new ServentInfo[chordLevel];
		for (int i = 0; i < chordLevel; i++) {
			successorTable[i] = null;
		}
	}

	private void updateSuccessorTable() {
		//first node after me has to be successorTable[0]
		AppConfig.timestampedStandardPrint(allNodeInfo.toString());

		calculateChordLevel();
		int firstSuccessorIndex = AppConfig.myServentInfo.getId() + 1;
		ServentInfo firstSuccessor = null;
		if (allNodeInfo.get(firstSuccessorIndex) != null) {
			firstSuccessor = allNodeInfo.get(firstSuccessorIndex);
		} else {
			firstSuccessor = allNodeInfo.get(0);
		}
		successorTable[0] = firstSuccessor;

		//i is successorTable index
		int successorIndex = 1;
		for(int i = 1; i < chordLevel; i++) {
			int id = (AppConfig.myServentInfo.getId() + (int)(Math.pow(2, i))) % allNodeInfo.size();
			if (allNodeInfo.containsKey(id)) {
				successorTable[successorIndex] = allNodeInfo.get(id);
				successorIndex++;
			}
		}
	}

	public Map<Integer, ServentInfo> getAllNodeInfo() {
		return allNodeInfo;
	}

	public void addNodes(Map<Integer, ServentInfo> newNodes) {
		for (Map.Entry<Integer, ServentInfo> entry: newNodes.entrySet()) {
			allNodeInfo.put(entry.getKey(), entry.getValue());
		}

		updateSuccessorTable();
	}
}
