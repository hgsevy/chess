package server;

import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataAccess.DataAccessException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.GameService;
import service.UserService;
import service.exceptions.UnauthorizedException;
import webSocketMessages.serverMessages.ErrorMessage;
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
            throwErrorMessage(session, "somehow send websocket without being signed in");
            return;
        }

        try{
            gameService.getGame(req.getGameID());
        } catch (DataAccessException e1){
            throwErrorMessage(session, e1.getMessage());
            return;
        }

        var notification = new Notification(message);
        broadcast(req.getGameID(), new Gson().toJson(notification), session);

        connections.add(new SessionInfo(token, req.getGameID(), session));

        LoadGame loadNotify;
        try{
            loadNotify = new LoadGame(gameService.getGame(req.getGameID()));
        } catch (DataAccessException e1){
            throwErrorMessage(session, e1.getMessage());
            return;
        }

        try {
            session.getRemote().sendString(new Gson().toJson(loadNotify));
        } catch (IOException e1) {
            System.out.print(e1.getMessage());
        }

    }

    private void joinObserver(String token, JoinObserver req, Session session){
        String message;
        try {
            message = userService.getUsername(token) + " is now observing the game";

        } catch (UnauthorizedException e1){
            throwErrorMessage(session, "somehow sent websocket without being signed in");
            return;
        }
        var notification = new Notification(message);
        broadcast(req.getGameID(), new Gson().toJson(notification), session);

        connections.add(new SessionInfo(token, req.getGameID(), session));

        LoadGame loadNotify;
        try {
            loadNotify = new LoadGame(gameService.getGame(req.getGameID()));
        } catch (DataAccessException e1){
            throwErrorMessage(session, "game somehow disappeared");
            return;
        }

        try {
            session.getRemote().sendString(new Gson().toJson(loadNotify));
        } catch (IOException e1) {
            System.out.print(e1.getMessage());
        }

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
                    throwErrorMessage(session, "you do not have access to chess");
                    return;
                }
            }
        }
        if (gameID == -1){
            throwErrorMessage(session, "game does not exist");
            return;
        }

        if (color == null){
            throwErrorMessage(session, "you are not a player in this game");
            return;
        }

        ChessGame game;
        try {
            game = gameService.getGame(gameID);
        } catch (DataAccessException e1){
            throwErrorMessage(session, "game somehow disappeared");
            return;
        }

        if (game.getBoard().getPiece(req.getMove().start()) != null && game.getBoard().getPiece(req.getMove().start()).getTeamColor() != color){
            throwErrorMessage(session, "that piece does not belong to you");
            return;
        }

        try {
            game.makeMove(req.getMove());
        } catch (InvalidMoveException e1){
            throwErrorMessage(session, e1.getMessage());
            return;
        }

        gameService.updateGame(gameID, game);
        try {
            var notification = new Notification(userService.getUsername(token) + " just moved from " + req.getMove().getStartPosition() + " to " + req.getMove().getEndPosition());
            broadcast(gameID, new Gson().toJson(notification), session);
            var gameNot = new LoadGame(game);
            broadcast(gameID, new Gson().toJson(gameNot), null);
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
            throwErrorMessage(session, "somehow you were never here");
            return;
        }
        connections.remove(toRemove);
        try {
            var notification = new Notification(userService.getUsername(token) + " just left the game");
            broadcast(req.getGameID(), new Gson().toJson(notification), session);
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
                    throwErrorMessage(session, "you do not have access to chess");
                    return;
                }
            }
        }
        if (gameID != req.getGameID()){
            throwErrorMessage(session, "game does not exist");
            return;
        }

        if (color == null){
            throwErrorMessage(session, "you are not a player in this game");
            return;
        }

        ChessGame game;
        try {
            game = gameService.getGame(gameID);
        } catch (DataAccessException e1){
            throwErrorMessage(session, "game somehow disappeared");
            return;
        }

        game.endGame();

        gameService.updateGame(gameID, game);
        try {
            var notification = new Notification(userService.getUsername(token) + " just resigned from this game. It is now over");
            broadcast(gameID, new Gson().toJson(notification), null);
        } catch (UnauthorizedException e1){
            System.out.println("how the heck did I get this far unauthorized?? (line 166)");
        }

    }

    private void throwErrorMessage(Session session, String message){
        try {
            session.getRemote().sendString(new Gson().toJson(new ErrorMessage(message)));
        } catch (IOException e2){
            System.out.println("bad session l 259");
        }
    }

    private void broadcast(int gameID, String notification, Session root) {
        ArrayList<SessionInfo> toRemove = new ArrayList<>();
        for (SessionInfo session : connections){
            if (session.gameID == gameID && !session.session.equals(root) && session.session.isOpen()) {
                try {
                    session.session.getRemote().sendString(notification);
                } catch (IOException e1) {
                    System.out.print(e1.getMessage());
                }
            }
            if (!session.session.isOpen()){
                toRemove.add(session);
            }
        }
        for (SessionInfo info : toRemove){
            connections.remove(info);
        }
    }


}
