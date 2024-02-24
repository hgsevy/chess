package service;

import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import dataAccess.GameDAO;
import dataAccess.UserDAO;
import model.GameData;
import spark.Request;
import spark.Response;

import java.util.Collection;

public class GameService {

    UserDAO userData;
    AuthDAO authData;
    GameDAO gameData;

    public GameService(UserDAO userData, AuthDAO authData, GameDAO gameData){
        this.userData = userData;
        this.authData = authData;
        this.gameData = gameData;
    }

    public void join(JoinGameRequest req) throws DataAccessException, UnauthorizedException {
        String username;
        try {
            username = authData.getUsername(req.token());
        } catch (DataAccessException expt1) {
            throw new UnauthorizedException("no authorization");
        }
        gameData.joinGame(username, req.gameID(), req.color());
    }

    public Collection<GameData> list(String authToken) throws UnauthorizedException {
        try {
            authData.getUsername(authToken);
        } catch (DataAccessException expt1) {
            throw new UnauthorizedException("no authorization");
        }
        return gameData.listGames();
    }

    public int create(CreateGameRequest req) throws UnauthorizedException {
        try {
            authData.getUsername(req.authToken());
        } catch (DataAccessException expt1) {
            throw new UnauthorizedException("no authorization");
        }
        return gameData.createGame(req.gameName());
    }
}
