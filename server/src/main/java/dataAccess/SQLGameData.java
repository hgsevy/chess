package dataAccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;

public class SQLGameData implements GameDAO{

    public int createGame(String name) {
        return 0;
    }

    public void joinGame(String username, int gameID, ChessGame.TeamColor color) throws DataAccessException {

    }

    public ArrayList<GameData> listGames() {
        return null;
    }

    public void clear() {

    }
}
