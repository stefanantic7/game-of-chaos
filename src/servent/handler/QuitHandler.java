package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.QuitMessage;
import servent.message.util.MessageUtil;

import java.util.HashMap;
import java.util.Map;

public class QuitHandler implements MessageHandler {

    private Message clientMessage;

    public QuitHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() != MessageType.QUIT) {
            AppConfig.timestampedErrorPrint("Quit handler got a message that is not QUIT");
            return;
        }

        QuitMessage quitMessage = (QuitMessage) clientMessage;
        int quitterId = quitMessage.getQuitterId();
        int myId = AppConfig.myServentInfo.getId();

        // Stop when message turns the circle
        if (quitterId == myId || !AppConfig.chordState.getAllNodeInfo().containsKey(quitterId)) {
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

        AppConfig.chordState.getAllNodeInfo().clear();
        AppConfig.chordState.mergeNodesInfoAndUpdateSuccessors(newNodesMap);

        // Update other notes -> start chain reaction :)
        QuitMessage newQuitMessage = new QuitMessage(
                quitMessage.getSenderIp(), quitMessage.getSenderPort(),
                AppConfig.chordState.getNextNodeIp(), AppConfig.chordState.getNextNodePort(),
                quitterId);
        MessageUtil.sendMessage(newQuitMessage);
    }
}
