package servent.handler;

import app.AppConfig;
import app.Job;
import app.Point;
import app.ServentInfo;
import cli.command.StartJobCommand;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.QuitMessage;
import servent.message.util.MessageUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class QuitHandler implements MessageHandler {

    private Message clientMessage;

    public QuitHandler(Message clientMessage) {
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
        if (clientMessage.getMessageType() != MessageType.QUIT) {
            AppConfig.timestampedErrorPrint("Quit handler got a message that is not QUIT");
            return;
        }

        QuitMessage quitMessage = (QuitMessage) clientMessage;
        int quitterId = quitMessage.getQuitterId();
        int myId = AppConfig.myServentInfo.getId();

        // Stop when message turns the circle
        // zavrsavamo kad dodjemo do noda koji je preuzeo id quitera
        if (quitterId == myId || !AppConfig.chordState.getAllNodeInfo().containsKey(quitterId)) {
            // start job if needed (if it was running)
            if (quitMessage.getActiveJob() != null) {
                AppConfig.timestampedStandardPrint("Calculating new distribution. Stored points: " + quitMessage.getComputedPoints().size());
                StartJobCommand.startJob(quitMessage.getActiveJob(), quitMessage.getComputedPoints());
                return;
            }

            return;
        }

        Map<Integer, ServentInfo> newNodesMap = new HashMap<>();
        AppConfig.chordState.getAllNodeInfo().remove(quitterId);

        AppConfig.chordState.getAllNodeInfo().forEach((nodeId, serventInfo) -> {
            if (nodeId > quitterId) {
                int newId = nodeId - 1;
                serventInfo.setId(newId);
                newNodesMap.put(newId, serventInfo);
            } else {
                newNodesMap.put(nodeId, serventInfo);
            }
        });
        if (myId > quitterId) {
            AppConfig.myServentInfo.setId(myId - 1);
        }

        Job activeJob = quitMessage.getActiveJob();
        Set<Point> computedPoints = quitMessage.getComputedPoints();

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




        AppConfig.chordState.getAllNodeInfo().clear();
        AppConfig.chordState.mergeNodesInfoAndUpdateSuccessors(newNodesMap);

        // Update other notes -> start chain reaction :)
        QuitMessage newQuitMessage = new QuitMessage(
                quitMessage.getSenderIp(), quitMessage.getSenderPort(),
                AppConfig.chordState.getNextNodeIp(), AppConfig.chordState.getNextNodePort(),
                quitterId, activeJob, computedPoints);
        MessageUtil.sendMessage(newQuitMessage);
    }
}
