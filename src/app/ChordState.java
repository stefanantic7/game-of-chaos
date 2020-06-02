package app;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import servent.message.WelcomeMessage;

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

	private int chordLevel; //log_2(CHORD_SIZE)

	private ServentInfo[] successorTable;

	//we DO NOT use this to send messages, but only to construct the successor table
	private Map<Integer, ServentInfo> allNodeInfo;

	private Map<String, Integer> fractalIdToNodeIdMap;

	private JobRunner jobRunner;

	public ChordState() {
		allNodeInfo = new HashMap<>();

		this.calculateChordLevel();

		this.fractalIdToNodeIdMap = new HashMap<>();
	}

	/**
	 * This should be called once after we get <code>WELCOME</code> message.
	 * It sets up our initial value map and our first successor so we can send <code>UPDATE</code>.
	 * It also lets bootstrap know that we did not collide.
	 */
	public void init(WelcomeMessage welcomeMsg) {
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ServentInfo[] getSuccessorTable() {
		return successorTable;
	}

	public boolean hasNextNode() {
		return successorTable.length > 0 && successorTable[0] != null;
	}

	public ServentInfo getNextNode() {
		if (successorTable == null || successorTable.length == 0) {
			return null;
		}

		return successorTable[0];
	}

	public int getNextNodePort() {
		return successorTable[0].getListenerPort();
	}

	public String getNextNodeIp() {
		return successorTable[0].getIpAddress();
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
		this.calculateChordLevel();

		int firstSuccessorIndex = AppConfig.myServentInfo.getId() + 1;
		ServentInfo firstSuccessor = null;
		if (allNodeInfo.get(firstSuccessorIndex) != null) {
			firstSuccessor = allNodeInfo.get(firstSuccessorIndex);
		} else if (allNodeInfo.size() > 1){
			firstSuccessor = allNodeInfo.get(0);
		}
		successorTable[0] = firstSuccessor;

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

	public void mergeNodesInfoAndUpdateSuccessors(Map<Integer, ServentInfo> newNodesInfo) {
		newNodesInfo.forEach((key, value) -> allNodeInfo.put(key, value));
		updateSuccessorTable();
	}

	public Integer getIdForFractalId(String fractalId) {
		return this.fractalIdToNodeIdMap.get(fractalId);
	}

	public Map<String, Integer> getFractalIdToNodeIdMap() {
		return fractalIdToNodeIdMap;
	}

	public void setFractalIdToNodeIdMap(Map<String, Integer> fractalIdToNodeIdMap) {
		this.fractalIdToNodeIdMap = fractalIdToNodeIdMap;
	}

	public void clearFractalIdToNodeId() {
		this.fractalIdToNodeIdMap = new HashMap<>();
	}

	public void registerFractalId(String fractalId, int nodeId) {
		this.fractalIdToNodeIdMap.put(fractalId, nodeId);
	}

	public void setJobRunner(JobRunner jobRunner) {
		this.jobRunner = jobRunner;
	}

	public JobRunner getJobRunner() {
		return jobRunner;
	}
}
