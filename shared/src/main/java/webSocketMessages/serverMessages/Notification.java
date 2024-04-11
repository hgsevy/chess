package webSocketMessages.serverMessages;

public class Notification extends ServerMessage{
    String message;

    public Notification(ServerMessageType type, String message, String message1) {
        super(type, message);
        this.message = message1;
        serverMessageType = ServerMessageType.NOTIFICATION;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
