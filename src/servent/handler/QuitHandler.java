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

        if (quitterId == myId || !AppConfig.chordState.getAllNodeInfo().containsKey(quitterId)) {
            AppConfig.timestampedStandardPrint("Quit message made a circle.");
            return;
        }

        Map<Integer, ServentInfo> newNodesMap = new HashMap<>();
        AppConfig.chordState.getAllNodeInfo().remove(quitterId);
        for (Map.Entry<Integer, ServentInfo> entry: AppConfig.chordState.getAllNodeInfo().entrySet()) {
            if (entry.getKey() > quitterId) {
                ServentInfo serventInfo = entry.getValue();
                serventInfo.setId(entry.getKey() - 1);
                newNodesMap.put(entry.getKey() - 1, serventInfo);
            } else {
                newNodesMap.put(entry.getKey(), entry.getValue());
            }
        }
        if (myId > quitterId) {
            AppConfig.myServentInfo.setId(myId - 1);
        }
        AppConfig.chordState.getAllNodeInfo().clear();
        AppConfig.chordState.addNodes(newNodesMap);

        // slanje svima dalje
        QuitMessage newQuitMessage = new QuitMessage(quitMessage.getSenderIp(), quitMessage.getSenderPort(),
                AppConfig.chordState.getNextNodeIp(), AppConfig.chordState.getNextNodePort(),
                quitterId);
        MessageUtil.sendMessage(newQuitMessage);
    }
}
