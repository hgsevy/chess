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
                game varchar(255) NOT NULL,
                PRIMARY KEY (gameID)
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
        System.out.print("womp womp: somehow got to SQLGameData line 54");
        return -1;
    }

    public void joinGame(String username, int gameID, ChessGame.TeamColor color) throws DataAccessException {
        try (var conn = getConnection()) {
            var checkTable = "SELECT blackUsername, whiteUsername FROM gameData WHERE gameID=?";
            try (var selectTableStatement = conn.prepareStatement(checkTable)) {
                selectTableStatement.setInt(1, gameID);
                ResultSet rs = selectTableStatement.executeQuery();
                // make sure table exists
                if(!rs.next()){
                    throw new DataAccessException("game does not exist");
                }
                // choose which spot to join
                if (color == ChessGame.TeamColor.BLACK){
                    if (rs.getString(1) == null){
                        var updateUsers = "UPDATE gameData SET blackUsername=? WHERE id = ?;";
                        try (var joinStatement = conn.prepareStatement(updateUsers)) {
                            joinStatement.setString(1, username);
                            joinStatement.setInt(2, gameID);
                            joinStatement.execute();
                        }
                    }
                    else if (!rs.getString(1).equals(username)){
                        throw new DataAccessException("spot already taken");
                    }
                }
                else if (color == ChessGame.TeamColor.WHITE){
                    if (rs.getString(2) == null){
                        var updateUsers = "UPDATE gameData SET whiteUsername=? WHERE id = ?;";
                        try (var joinStatement = conn.prepareStatement(updateUsers)) {
                            joinStatement.setString(1, username);
                            joinStatement.setInt(2, gameID);
                            joinStatement.execute();
                        }
                    }
                    else if (!rs.getString(2).equals(username)){
                        throw new DataAccessException("spot already taken");
                    }
                }
                else {
                    throw new DataAccessException("color does not exist");
                }

            }
        } catch (DataAccessException e1) {
            if (e1.getMessage().contains("not exist") || e1.getMessage().contains("taken")){
                throw e1;
            }
            System.out.print(" DataAccess womp womp: " + e1.getMessage());
        } catch (SQLException e2){
            System.out.print("SQL womp womp: " + e2.getMessage());
        }
    }

    public ArrayList<GameData> listGames() {
        ArrayList<GameData> answer = new ArrayList<>();
        try (var conn = getConnection()) {
            String command = "SELECT gameID, whiteUsername, blackUsername, gameName, game";
            try (var listGamesStatement = conn.prepareStatement(command)) {
                ResultSet rs = listGamesStatement.executeQuery();

                while (rs.next()){
                    answer.add(new GameData(rs.getInt(1), rs.getString(2), rs.getString(3),
                            rs.getString(4), new Gson().fromJson(rs.getString(5), ChessGame.class)));
                }
            }
        } catch (SQLException | DataAccessException e) {
            System.out.print("womp womp: " + e.getMessage());
        }
        return answer;
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
