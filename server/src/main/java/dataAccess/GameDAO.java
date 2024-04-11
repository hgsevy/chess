package dataAccess;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import model.GameData;

import java.util.ArrayList;

public interface GameDAO extends ParentDAO{
    int createGame(String name);
    void joinGame(String username, int gameID, ChessGame.TeamColor color) throws DataAccessException;
    ArrayList<GameData> listGames();
    void updateGame(int gameID, ChessGame newGame) throws DataAccessException;
}
