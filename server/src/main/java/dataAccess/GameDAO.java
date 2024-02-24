package dataAccess;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;

public interface GameDAO extends ParentDAO{
    int createGame(String name);
    void joinGame(String username, int gameID, ChessGame.TeamColor color) throws DataAccessException;
    Collection<GameData> listGames();
    void updateGame (int gameID, ChessGame game) throws DataAccessException;
}
