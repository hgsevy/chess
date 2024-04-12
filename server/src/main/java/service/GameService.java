package service;

import chess.ChessGame;
import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import dataAccess.GameDAO;
import model.GameData;
import service.exceptions.BadInputException;
import service.exceptions.NoCanDoException;
import service.exceptions.UnauthorizedException;
import model.requestsAndResults.JoinGameRequest;

import java.util.ArrayList;
import java.util.Collection;

public class GameService {


    AuthDAO authData;
    GameDAO gameData;

    public GameService(AuthDAO authData, GameDAO gameData){
        this.authData = authData;
        this.gameData = gameData;
    }

    public void join(String token, JoinGameRequest req) throws UnauthorizedException, NoCanDoException, BadInputException {
        String username;
        try {
            username = authData.getUsername(token);
        } catch (DataAccessException expt1) {
            throw new UnauthorizedException();
        }
        try {
            gameData.joinGame(username, req.gameID(), req.playerColor());
        } catch (DataAccessException expt2){
            if (expt2.getLocalizedMessage().contains("taken")) {
                throw new NoCanDoException();
            } else if (expt2.getLocalizedMessage().contains("exist") ){
                throw new BadInputException();
            }
        }
    }

    public ArrayList<GameData> list(String authToken) throws UnauthorizedException {
        try {
            authData.getUsername(authToken);
        } catch (DataAccessException expt1) {
            throw new UnauthorizedException();
        }
        return gameData.listGames();
    }

    public int create(String token, String gameName) throws UnauthorizedException {
        try {
            authData.getUsername(token);
        } catch (DataAccessException expt1) {
            throw new UnauthorizedException();
        }
        return gameData.createGame(gameName);
    }

    public ChessGame.TeamColor getPlayerColor(String username, int gameID){
        ArrayList<GameData> list = gameData.listGames();
        for (GameData data : list){
            if (data.gameID() == gameID){
                if (data.whiteUsername().equals(username)){
                    return ChessGame.TeamColor.WHITE;
                }
                else if (data.blackUsername().equals(username)){
                    return ChessGame.TeamColor.BLACK;
                }
                else {return null;}
            }
        }
        return null;
    }

    public ChessGame getGame(int gameID) throws DataAccessException {
        ArrayList<GameData> list = gameData.listGames();
        for (GameData data : list){
            if (data.gameID() == gameID){
                return data.game();
            }
        }
        throw new DataAccessException("game does not exist");
    }

    public void updateGame(int gameID, ChessGame newGame){
        try {
            gameData.updateGame(gameID, newGame);
        } catch (DataAccessException e1){
            System.out.println(e1.getMessage());
        }
    }

}
