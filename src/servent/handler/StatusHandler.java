package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.StatusMessage;

import java.util.Map;

public class StatusHandler implements MessageHandler {
    private Message clientMessage;

    public StatusHandler(Message clientMessage) {
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
        if (clientMessage.getMessageType() != MessageType.STATUS) {
            AppConfig.timestampedErrorPrint("Handler got a message that is not STATUS_MESSAGE");
            return;
        }
        StatusMessage statusMessage = (StatusMessage) clientMessage;

        StringBuilder output = new StringBuilder("Received status for job: " + statusMessage.getJobName() + "\n");
        output.append("Active nodes: ").append(statusMessage.getFractalIdToPointCountMap().size()).append("\n");
        int totalPoints = 0;
        for (Map.Entry<String, Integer> entry: statusMessage.getFractalIdToPointCountMap().entrySet()) {
            output.append("Fractal id ").append(entry.getKey())
                    .append(", servent id ").append(AppConfig.chordState.getFractalIdToNodeIdMap().get(entry.getKey()))
                    .append(": ").append(entry.getValue()).append(" points\n");
            totalPoints += entry.getValue();
        }
        output.append("Total points: ").append(totalPoints);

        AppConfig.timestampedStandardPrint(output.toString());
    }
}
