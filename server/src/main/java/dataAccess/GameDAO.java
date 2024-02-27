package dataAccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;

public interface GameDAO extends ParentDAO{
    int createGame(String name);
    void joinGame(String username, int gameID, ChessGame.TeamColor color) throws DataAccessException;
    ArrayList<GameData> listGames();
}
