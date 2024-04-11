package clientAPI;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import webSocketMessages.userCommands.*;

import javax.websocket.*;
import java.net.URI;

public class WSClient extends Endpoint {

    public Session session;
    private final String authToken;
    private final int gameID;

    public WSClient(int portNum, String authToken, int gameID) throws Exception {
        this.authToken = authToken;
        this.gameID = gameID;

        URI uri = new URI("ws://localhost:"+portNum+"/connect");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);

        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                System.out.println(message);
            }
        });
    }

    public void makeMove(ChessMove move) throws Exception {
        MakeMove req = new MakeMove(authToken, gameID, move);
        send(new Gson().toJson(req));
    }

    public void resign() throws Exception {
        Resign req = new Resign(authToken, gameID);
        send(new Gson().toJson(req));
    }

    public void leave() throws Exception {
        Leave req = new Leave(authToken, gameID);
        send(new Gson().toJson(req));
    }

    public void joinObserver() throws Exception {
        JoinObserver req = new JoinObserver(authToken, gameID);
        send(new Gson().toJson(req));
    }

    public void joinPlayer(ChessGame.TeamColor color) throws Exception {
        JoinPlayer req = new JoinPlayer(authToken, gameID, color);
        send(new Gson().toJson(req));
    }

    public void send(String msg) throws Exception {
        this.session.getBasicRemote().sendText(msg);
    }

    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}