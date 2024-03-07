package dataAccess;

import chess.ChessGame;
import model.GameData;

import java.sql.SQLException;
import java.util.ArrayList;

import static dataAccess.DatabaseManager.getConnection;

public class SQLGameData implements GameDAO{

    public SQLGameData() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = getConnection()) {
            var createGameTable = """
            CREATE TABLE  IF NOT EXISTS gameData (
                gameID INT NOT NULL AUTO_INCREMENT,
                whiteUsername varchar(255) NOT NULL,
                blackUsername varchar(255) NOT NULL,
                gameName varchar(255) NOT NULL,
                game varchar(255) NOT NULL
            )
            """;

            try (var createTableStatement = conn.prepareStatement(createGameTable)) {
                createTableStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("trouble making table; " + e.getMessage());
        }
    }

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
