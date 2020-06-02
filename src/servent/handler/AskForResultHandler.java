package servent.handler;

import app.AppConfig;
import app.JobDetails;
import app.Point;
import servent.message.*;
import servent.message.util.MessageUtil;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class AskForResultHandler implements MessageHandler {

    private Message clientMessage;

    public AskForResultHandler(Message clientMessage) {
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
        if (clientMessage.getMessageType() != MessageType.ASK_FOR_RESULT) {
            AppConfig.timestampedErrorPrint("Handler got a message that is not ASK_FOR_RESULT");
            return;
        }

        AskForResultMessage askForResultMessage = (AskForResultMessage) clientMessage;
        if (AppConfig.chordState.getJobRunner() == null
                || !AppConfig.chordState.getJobRunner().getJobName().equals(askForResultMessage.getJobName())) {
            BasicMessage errorMessage = new BasicMessage(MessageType.ERROR,
                    AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    clientMessage.getSenderIp(), clientMessage.getSenderPort(),
                    "The job \"" + askForResultMessage.getJobName() + "\" is not running");
            MessageUtil.sendMessage(errorMessage);
            return;
        }

        int lastServentId = this.getLastActiveNodeId();
        Set<Point> receivedComputedPoints = askForResultMessage.getAppendedResults();

        // add my points
        Set<Point> myComputedPoints = AppConfig.chordState.getJobRunner().getComputedPoints();
        AppConfig.timestampedStandardPrint("Points count: " + myComputedPoints.size());
        receivedComputedPoints.addAll(myComputedPoints);
        if (AppConfig.myServentInfo.getId() == lastServentId || askForResultMessage.hasFractalId()) {
            // send result to the node which requested it
            String name = AppConfig.chordState.getJobRunner().getJobName();
            int width = AppConfig.chordState.getJobRunner().getWidth();
            int height = AppConfig.chordState.getJobRunner().getHeight();
            double proportion = AppConfig.chordState.getJobRunner().getProportion();

            JobDetails jobDetails = new JobDetails(name, proportion, width, height);
            // todo: popravi slanje vise
            ResultMessage resultMessage = new ResultMessage(
                    AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    clientMessage.getSenderIp(), clientMessage.getSenderPort(),
                    jobDetails, receivedComputedPoints, askForResultMessage.getFractalId());
            MessageUtil.sendMessage(resultMessage);
        } else {
            // send to first successor
            AskForResultMessage newAskForResultMessage = new AskForResultMessage(
                    clientMessage.getSenderIp(), clientMessage.getSenderPort(),
                    AppConfig.chordState.getNextNodeIp(), AppConfig.chordState.getNextNodePort(),
                    askForResultMessage.getJobName(), receivedComputedPoints);
            MessageUtil.sendMessage(newAskForResultMessage);
        }

    }

    private int getLastActiveNodeId() {
        return Collections.max(AppConfig.chordState.getFractalIdToNodeIdMap().values());
    }
}
