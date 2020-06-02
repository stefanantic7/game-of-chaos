package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.StopJobMessage;
import servent.message.util.MessageUtil;

import java.util.Collections;

public class StopJobHandler implements MessageHandler {

    private Message clientMessage;

    public StopJobHandler(Message clientMessage) {
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

    public void handle() {
        if (clientMessage.getMessageType() != MessageType.STOP_JOB) {
            AppConfig.timestampedErrorPrint("Handler got a message that is not STOP_JOB");
            return;
        }

        StopJobMessage stopJobMessage = (StopJobMessage) clientMessage;

        if (AppConfig.chordState.getJobRunner() == null
                || !AppConfig.chordState.getJobRunner().getJobName().equals(stopJobMessage.getJobName())) {
            // TODO: error message
            AppConfig.timestampedErrorPrint("The job \"" + stopJobMessage.getJobName() + "\" is not running");
            return;
        }

        int lastActiveNodeId = getLastActiveNodeId();

        AppConfig.chordState.getJobRunner().stop();
        AppConfig.chordState.setJobRunner(null);
        AppConfig.chordState.clearFractalIdToNodeId();

        AppConfig.timestampedStandardPrint("I stopped job: \"" + stopJobMessage.getJobName() + "\"");

        if (AppConfig.myServentInfo.getId() != lastActiveNodeId) {
            StopJobMessage message = new StopJobMessage(
                    clientMessage.getSenderIp(), clientMessage.getSenderPort(),
                    AppConfig.chordState.getNextNodeIp(), AppConfig.chordState.getNextNodePort(),
                    stopJobMessage.getJobName());
            MessageUtil.sendMessage(message);
        }

    }

    private int getLastActiveNodeId() {
        return Collections.max(AppConfig.chordState.getFractalIdToNodeIdMap().values());
    }
}
