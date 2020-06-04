package servent.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import app.AppConfig;
import app.Job;
import app.Point;
import app.ServentInfo;
import cli.command.StartJobCommand;
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

			Job activeJob = updateMessage.getActiveJob();
			Set<Point> computedPoints = updateMessage.getComputedPoints();

			// 1. daj svoje tacke, ako sam ja radio job
			if (AppConfig.chordState.getJobRunner() != null) {
				if (activeJob == null) {
					activeJob = AppConfig.chordState.getJobRunner().getOriginalJob();
				}

				computedPoints.addAll(AppConfig.chordState.getJobRunner().getComputedPoints());
				// 2. zaustavi posao
				AppConfig.chordState.getJobRunner().stop();
				AppConfig.chordState.setJobRunner(null);
				AppConfig.chordState.clearFractalIdToNodeId();

				AppConfig.timestampedStandardPrint("Job was stopped and removed...");
			}

			Message updateNextNodeMessage = new UpdateMessage(
					clientMessage.getSenderIp(), clientMessage.getSenderPort(),
					AppConfig.chordState.getNextNodeIp(),  AppConfig.chordState.getNextNodePort(),
					AppConfig.chordState.getAllNodeInfo(), activeJob, computedPoints);
			MessageUtil.sendMessage(updateNextNodeMessage);
		} else if (updateMessage.getActiveJob() != null) {
			AppConfig.timestampedStandardPrint("Calculating new distribution. Stored points: " + updateMessage.getComputedPoints().size());
			StartJobCommand.startJob(updateMessage.getActiveJob(), updateMessage.getComputedPoints());
		}
	}

}
