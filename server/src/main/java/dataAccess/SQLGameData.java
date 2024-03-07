package dataAccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.ResultSet;
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
                whiteUsername varchar(255),
                blackUsername varchar(255),
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
        try (var conn = getConnection()) {
            var addGame = "INSERT INTO gameData(gameName, game) VALUE (?, ?)";
            try (var insertTableStatement = conn.prepareStatement(addGame)) {
                insertTableStatement.setString(1, name);
                insertTableStatement.setString(2, new Gson().toJson(new ChessGame()));
                insertTableStatement.execute();
            }
            var findGame = "SELECT gameID from gameData WHERE gameName=?";
            try (var insertTableStatement = conn.prepareStatement(addGame)) {
                insertTableStatement.setString(1, name);
                ResultSet rs = insertTableStatement.executeQuery();
                return rs.getInt(1);
            }
        } catch (DataAccessException | SQLException e1) {
            System.out.print("womp womp: " + e1.getMessage());
        }
        System.out.print("womp womp: somehow got to SQLGameData line 53");
        return -1;
    }

    public void joinGame(String username, int gameID, ChessGame.TeamColor color) throws DataAccessException {

    }

    public ArrayList<GameData> listGames() {
        return null;
    }

    public void clear() {
        try (var conn = getConnection()) {
            String command = "TRUNCATE gameData";
            try (var clearTableStatement = conn.prepareStatement(command)) {
                clearTableStatement.execute();
            }
        } catch (SQLException | DataAccessException e) {
            System.out.print("womp womp: " + e.getMessage());
        }
    }
}
