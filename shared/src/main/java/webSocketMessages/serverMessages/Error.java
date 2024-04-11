package webSocketMessages.serverMessages;

public class Error extends ServerMessage{
    String errorMessage;

    public Error(ServerMessageType type, String message, String errorMessage) {
        super(type, message);
        this.errorMessage = errorMessage;
        serverMessageType = ServerMessageType.ERROR;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
