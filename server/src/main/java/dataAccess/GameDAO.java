package dataAccess;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;

public interface GameDAO extends ParentDAO{
    public int createGame(String name);
    public void joinGame(String username, int gameID, ChessGame.TeamColor color) throws DataAccessException;
    public Collection<GameData> listGames();
    public void updateGame (int gameID, ChessGame game) throws DataAccessException;
}
