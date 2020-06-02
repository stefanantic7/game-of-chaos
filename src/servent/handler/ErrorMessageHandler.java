package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.QuitMessage;
import servent.message.util.MessageUtil;

import java.util.HashMap;
import java.util.Map;

public class ErrorMessageHandler implements MessageHandler {
    private Message clientMessage;

    public ErrorMessageHandler(Message clientMessage) {
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
        if (clientMessage.getMessageType() != MessageType.ERROR) {
            AppConfig.timestampedErrorPrint("Handler got a message that is not ERROR");
            return;
        }

        AppConfig.timestampedErrorPrint(clientMessage.getMessageText());
    }
}
