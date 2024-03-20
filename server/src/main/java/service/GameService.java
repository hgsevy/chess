package service;

import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import dataAccess.GameDAO;
import model.GameData;
import service.exceptions.BadInputException;
import service.exceptions.NoCanDoException;
import service.exceptions.UnauthorizedException;
import model.requestsAndResults.JoinGameRequest;

import java.util.ArrayList;

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
}
