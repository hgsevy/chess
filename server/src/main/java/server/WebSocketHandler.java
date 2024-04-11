package server;

import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.GameService;
import service.UserService;
import service.exceptions.UnauthorizedException;
import webSocketMessages.serverMessages.Error;
import webSocketMessages.serverMessages.LoadGame;
import webSocketMessages.serverMessages.Notification;
import webSocketMessages.userCommands.*;

import java.io.IOException;
import java.util.*;


@WebSocket
public class WebSocketHandler {

    private final UserService userService;
    private final GameService gameService;

    private Collection<SessionInfo> connections;

    public record SessionInfo(String authToken, int gameID, Session session){}

    public WebSocketHandler(UserService userService, GameService gameService){
        connections = new HashSet<>();
        this.userService = userService;
        this.gameService = gameService;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        UserGameCommand action = new Gson().fromJson(message, UserGameCommand.class);
        switch (action.getCommandType()) {
            case JOIN_PLAYER -> joinPlayer(action.getAuthString(), new Gson().fromJson(message, JoinPlayer.class), session);
            case JOIN_OBSERVER -> joinObserver(action.getAuthString(), new Gson().fromJson(message, JoinObserver.class), session);
            case MAKE_MOVE -> makeMove(action.getAuthString(), new Gson().fromJson(message, MakeMove.class), session);
            case LEAVE -> leave(action.getAuthString(), new Gson().fromJson(message, Leave.class), session);
            case RESIGN -> resign(action.getAuthString(), new Gson().fromJson(message, Resign.class), session);
        }
    }

    private void joinPlayer(String token, JoinPlayer req, Session session){
        connections.add(new SessionInfo(token, req.getGameID(), session));

        String message;
        try {
            message = userService.getUsername(token) + " has now joined the game as ";
            if (req.getColor() == ChessGame.TeamColor.BLACK){
                message += "black";
            }
            else {
                message += "white";
            }

        } catch (UnauthorizedException e1){
            System.out.print("somehow sent websocket wo being signed in");
            return;
        }
        var notification = new Notification(message);
        broadcast(req.getGameID(), new Gson().toJson(notification));
    }

    private void joinObserver(String token, JoinObserver req, Session session){
        connections.add(new SessionInfo(token, req.getGameID(), session));

        String message;
        try {
            message = userService.getUsername(token) + " is now observing the game";

        } catch (UnauthorizedException e1){
            System.out.print("somehow sent websocket wo being signed in");
            return;
        }
        var notification = new Notification(message);
        broadcast(req.getGameID(), new Gson().toJson(notification));

    }

    private void makeMove(String token, MakeMove req, Session session){
        ChessGame.TeamColor color = null;
        int gameID = -1;

        for(SessionInfo sessionInfo : connections){
            if(sessionInfo.session.equals(session)){
                gameID = sessionInfo.gameID;
                try {
                    color = gameService.getPlayerColor(userService.getUsername(token), gameID);
                } catch (UnauthorizedException e1){
                    try {
                        session.getRemote().sendString(new Gson().toJson(new Error("you do not have access to chess")));
                        return;
                    } catch (IOException e2){
                        System.out.println("bad session l 108");
                    }
                }
            }
        }
        if (gameID == -1){
            try {
                session.getRemote().sendString(new Gson().toJson(new Error("game does not exist")));
                return;
            } catch (IOException e2){
                System.out.println("bad session l 108");
            }
        }

        if (color == null){
            try {
                session.getRemote().sendString(new Gson().toJson(new Error("you are not a player in this game")));
                return;
            } catch (IOException e2){
                System.out.println("bad session l 108");
            }
        }

        ChessGame game = gameService.getGame(gameID);

        if (game.getBoard().getPiece(req.getMove().start()) != null && game.getBoard().getPiece(req.getMove().start()).getTeamColor() != color){
            try {
                session.getRemote().sendString(new Gson().toJson(new Error("that piece doe not belong to you")));
                return;
            } catch (IOException e2){
                System.out.println("bad session l 108");
            }
        }

        try {
            game.makeMove(req.getMove());
        } catch (InvalidMoveException e1){
            try {
                session.getRemote().sendString(new Gson().toJson(new Error(e1.getMessage())));
                return;
            } catch (IOException e2){
                System.out.println("bad session l 108");
            }
        }

        gameService.updateGame(gameID, game);
        try {
            var notification = new Notification(userService.getUsername(token) + " just moved from " + req.getMove().getStartPosition() + " to " + req.getMove().getEndPosition());
            broadcast(gameID, new Gson().toJson(notification));
            var gameNot = new LoadGame(game);
            broadcast(gameID, new Gson().toJson(gameNot));
        } catch (UnauthorizedException e1){
            System.out.println("how the heck did I get this far unauthorized?? (line 166)");
        }

    }

    private void leave(String token, Leave req, Session session){
        SessionInfo toRemove = null;
        for (SessionInfo info : connections){
            if (session.equals(info.session)){
                toRemove = info;
            }
        }
        if (toRemove == null){
            try {
                session.getRemote().sendString(new Gson().toJson(new Error("somehow you were never here")));
                return;
            } catch (IOException e2){
                System.out.println("bad session l 108");
            }
            return;
        }
        connections.remove(toRemove);
        try {
            var notification = new Notification(userService.getUsername(token) + " just left the game");
            broadcast(req.getGameID(), new Gson().toJson(notification));
        } catch (UnauthorizedException e1){
            System.out.println("how the heck did I get this far unauthorized?? (line 166)");
        }
    }

    private void resign(String token, Resign req, Session session){
        ChessGame.TeamColor color = null;
        int gameID = -1;

        for(SessionInfo sessionInfo : connections){
            if(sessionInfo.session.equals(session)){
                gameID = sessionInfo.gameID;
                try {
                    color = gameService.getPlayerColor(userService.getUsername(token), gameID);
                } catch (UnauthorizedException e1){
                    try {
                        session.getRemote().sendString(new Gson().toJson(new Error("you do not have access to chess")));
                        return;
                    } catch (IOException e2){
                        System.out.println("bad session l 108");
                    }
                }
            }
        }
        if (gameID == -1){
            try {
                session.getRemote().sendString(new Gson().toJson(new Error("game does not exist")));
                return;
            } catch (IOException e2){
                System.out.println("bad session l 108");
            }
        }

        if (color == null){
            try {
                session.getRemote().sendString(new Gson().toJson(new Error("you are not a player in this game")));
                return;
            } catch (IOException e2){
                System.out.println("bad session l 108");
            }
        }

        ChessGame game = gameService.getGame(gameID);

        game.endGame();

        gameService.updateGame(gameID, game);
        try {
            var notification = new Notification(userService.getUsername(token) + " just resigned from this game. It is now over");
            broadcast(gameID, new Gson().toJson(notification));
        } catch (UnauthorizedException e1){
            System.out.println("how the heck did I get this far unauthorized?? (line 166)");
        }

    }



    private void broadcast(int gameID, String notification) {
        for (SessionInfo session : connections){
            if (session.gameID == gameID) {
                try {
                    session.session.getRemote().sendString(notification);
                } catch (IOException e1) {
                    System.out.print(e1.getMessage());
                }
            }
        }
    }


}
