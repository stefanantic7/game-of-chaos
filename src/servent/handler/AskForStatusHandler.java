package servent.handler;

import app.AppConfig;
import servent.message.*;
import servent.message.util.MessageUtil;

import java.util.Collections;
import java.util.Map;

public class AskForStatusHandler implements MessageHandler {

    private Message clientMessage;

    public AskForStatusHandler(Message clientMessage) {
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
        if (clientMessage.getMessageType() != MessageType.ASK_FOR_STATUS) {
            AppConfig.timestampedErrorPrint("Handler got a message that is not ASK_FOR_STATUS");
            return;
        }

        AskForStatusMessage askForStatusMessage = (AskForStatusMessage) clientMessage;

        if (AppConfig.chordState.getJobRunner() == null
                || !AppConfig.chordState.getJobRunner().getJobName().equals(askForStatusMessage.getJobName())) {
            BasicMessage errorMessage = new BasicMessage(MessageType.ERROR,
                    AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    clientMessage.getSenderIp(), clientMessage.getSenderPort(),
                    "The job \"" + askForStatusMessage.getJobName() + "\" is not running");
            MessageUtil.sendMessage(errorMessage);
            return;
        }

//        String myFractalId = this.getMyFractalId();
        String myFractalId = AppConfig.chordState.getJobRunner().getFractalId();


        Map<String, Integer> fractalIdToPointsCountMap = askForStatusMessage.getFractalIdToPointCountMap();
        fractalIdToPointsCountMap.put(myFractalId, AppConfig.chordState.getJobRunner().getComputedPoints().size());

        int lastActiveNodeId = getLastActiveNodeId();
        if (AppConfig.myServentInfo.getId() == lastActiveNodeId || askForStatusMessage.hasFractalId()) {
            StatusMessage message = new StatusMessage(
                    AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    clientMessage.getSenderIp(), clientMessage.getSenderPort(),
                    askForStatusMessage.getJobName(), fractalIdToPointsCountMap);
            MessageUtil.sendMessage(message);
        } else {
            AskForStatusMessage message = new AskForStatusMessage(
                    clientMessage.getSenderIp(), clientMessage.getSenderPort(),
                    AppConfig.chordState.getNextNodeIp(), AppConfig.chordState.getNextNodePort(),
                    askForStatusMessage.getJobName(), fractalIdToPointsCountMap);
            MessageUtil.sendMessage(message);
        }

    }

    private String getMyFractalId() {
        for (Map.Entry<String, Integer> entry : AppConfig.chordState.getFractalIdToNodeIdMap().entrySet()) {
            if (AppConfig.myServentInfo.getId() == entry.getValue()) {
                return entry.getKey();
            }
        }
        return "";
    }

    private int getLastActiveNodeId() {
        return Collections.max(AppConfig.chordState.getFractalIdToNodeIdMap().values());
    }
}
