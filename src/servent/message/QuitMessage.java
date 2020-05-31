package servent.message;

public class QuitMessage extends BasicMessage {

    private static final long serialVersionUID = 549279197594251394L;

    private final int quitterId;

    public QuitMessage(String senderIp, int senderPort,
                       String receiverIp, int receiverPort,
                       int quitterId) {
        super(MessageType.QUIT, senderIp, senderPort, receiverIp, receiverPort);
        this.quitterId = quitterId;
    }

    public int getQuitterId() { return quitterId; }
}
