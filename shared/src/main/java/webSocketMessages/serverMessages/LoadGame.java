package webSocketMessages.serverMessages;

import chess.ChessGame;

public class LoadGame extends ServerMessage{
    ChessGame game;

    public LoadGame(ServerMessageType type, String message, ChessGame game) {
        super(type, message);
        this.game = game;
        serverMessageType = ServerMessageType.LOAD_GAME;
    }

    public ChessGame getGame() {
        return game;
    }
}
