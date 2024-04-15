package server;

import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataAccess.DataAccessException;
import model.GameData;
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
        String username;
        String color = "";
        try {
            username = userService.getUsername(req.getAuthString());
        } catch (UnauthorizedException e1){
            throwErrorMessage(session, "not authorized");
            return;
        }

        // ensure player actually joined
        try{
            ArrayList<GameData> games = gameService.list(req.getAuthString());
            boolean exists = false;
            for (GameData game : games){
                if (game.gameID() == req.getGameID()){
                    exists = true;
                    if ((req.getColor() == ChessGame.TeamColor.BLACK && game.blackUsername().equals(username))){
                        color = "black";
                    } else if ((req.getColor() == ChessGame.TeamColor.WHITE && game.whiteUsername().equals(username))){
                        color = "white";
                    } else{
                        throwErrorMessage(session, "you did not actually join the game");
                        return;
                    }
                    break;
                }
            }
            if (!exists) {
                throwErrorMessage(session, "game does not exist");
                return;
            }
        } catch (UnauthorizedException e1){
            throwErrorMessage(session, "you do not have access");
            return;
        }

        String message = username + " has now joined the game as " + color;

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
        InfoForNotification info;
        try {
            info = getInfoForNotification(token, req.getGameID(), session);
        } catch (DataAccessException e1){
            return;
        }

        if (info.game.getBoard().getPiece(req.getMove().start()) != null && info.game.getBoard().getPiece(req.getMove().start()).getTeamColor() != info.color){
            throwErrorMessage(session, "that piece does not belong to you");
            return;
        }

        try {
            info.game.makeMove(req.getMove());
        } catch (InvalidMoveException e1){
            throwErrorMessage(session, e1.getMessage());
            return;
        }

        Notification notification1 = null;
        if (info.game.isInCheck(ChessGame.TeamColor.WHITE)){
            notification1 = new Notification(gameService.getPlayer(info.gameID, false) + " is in check");
        }
        if (info.game.isInCheckmate(ChessGame.TeamColor.BLACK)){
            notification1 = new Notification(gameService.getPlayer(info.gameID, true) + " is in check");
        }
        if (info.game.isInCheckmate(ChessGame.TeamColor.WHITE)){
            notification1 = new Notification("game is over because " + gameService.getPlayer(info.gameID, false) + " is in checkmate");
        }
        if (info.game.isInCheckmate(ChessGame.TeamColor.BLACK)){
            notification1 = new Notification("game is over because " + gameService.getPlayer(info.gameID, true) + " is in checkmate");
        }

        gameService.updateGame(info.gameID, info.game);
        try {
            var notification2 = new Notification(userService.getUsername(token) + " just moved from " + req.getMove().getStartPosition() + " to " + req.getMove().getEndPosition());
            broadcast(info.gameID, new Gson().toJson(notification2), session);
            if (notification1 != null){
                broadcast(info.gameID, new Gson().toJson(notification1), null);
            }
            var gameNot = new LoadGame(info.game);
            broadcast(info.gameID, new Gson().toJson(gameNot), null);
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
        InfoForNotification info;
        try {
            info = getInfoForNotification(token, req.getGameID(), session);
        } catch (DataAccessException e1){
            return;
        }

        if (info.game.isOver()){
            throwErrorMessage(session, "game is already over");
            return;
        }

        info.game.endGame();

        gameService.updateGame(info.gameID, info.game);
        try {
            var notification = new Notification(userService.getUsername(token) + " just resigned from this game. It is now over");
            broadcast(info.gameID, new Gson().toJson(notification), null);
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

    private record InfoForNotification(int gameID, ChessGame.TeamColor color, ChessGame game){ }

    private InfoForNotification getInfoForNotification(String token, int gameID, Session session) throws DataAccessException {
        InfoForNotification info = null;
        for(SessionInfo sessionInfo : connections){
            if(sessionInfo.session.equals(session)){
                try {
                    info = new InfoForNotification(sessionInfo.gameID, gameService.getPlayerColor(userService.getUsername(token), gameID), gameService.getGame(sessionInfo.gameID));
                } catch (UnauthorizedException e1){
                    throwErrorMessage(session, "you do not have access to chess");
                    throw new DataAccessException("unauthorized. error message sent");
                }
            }
        }
        if (info == null) {
            throwErrorMessage(session, "game does not exist");
            throw new DataAccessException("game doesn't exist. error message sent");
        }

        if (info.color == null){
            throwErrorMessage(session, "you are not a player in this game");
            throw new DataAccessException("not a player. error message sent");
        }

        return info;
    }

}
