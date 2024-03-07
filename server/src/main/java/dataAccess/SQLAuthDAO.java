package dataAccess;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static dataAccess.DatabaseManager.getConnection;

public class SQLAuthDAO implements AuthDAO{

    public SQLAuthDAO() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = getConnection()) {
            var createAuthTable = """
            CREATE TABLE  IF NOT EXISTS authData (
                authToken varchar(255) NOT NULL,
                username varchar(255) NOT NULL,
                PRIMARY KEY (authToken)
            )
            """;

            try (var createTableStatement = conn.prepareStatement(createAuthTable)) {
                createTableStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("trouble making table; " + e.getMessage());
        }
    }

    public String createAuthToken(String username) {
        String token = UUID.randomUUID().toString();
        try (var conn = getConnection()) {
            String command = "INSERT INTO authData(authToken, username) VALUES(?, ?)";
            try (var addTokenStatement = conn.prepareStatement(command)) {
                addTokenStatement.setString(1, token);
                addTokenStatement.setString(2, username);
                addTokenStatement.execute();
            }
        } catch (SQLException | DataAccessException e) {
            System.out.print("womp womp: " + e.getMessage());
        }
        return token;
    }

    public String getUsername(String token) throws DataAccessException {
        try (var conn = getConnection()) {
            String command = "SELECT username FROM authData WHERE authToken=?";
            try (var addTokenStatement = conn.prepareStatement(command)) {
                addTokenStatement.setString(1, token);
                ResultSet rs = addTokenStatement.executeQuery();
                if (!rs.next()){
                    throw new DataAccessException("auth token does not exist");
                }
                return rs.getString(1);
            }
        } catch (DataAccessException e1) {
            if (e1.getMessage().contains("not exist")){
                throw e1;
            }
            System.out.print("womp womp: " + e1.getMessage());
        } catch (SQLException e2){
            System.out.print("womp womp: " + e2.getMessage());
        }
        return null;
    }

    public void deleteAuthToken(String token) throws DataAccessException {
        try (var conn = getConnection()) {
            String command = "SELECT username FROM authData WHERE authToken=?";
            try (var addTokenStatement = conn.prepareStatement(command)) {
                addTokenStatement.setString(1, token);
                ResultSet rs = addTokenStatement.executeQuery();
                if (!rs.next()){
                    throw new DataAccessException("auth token does not exist");
                }
            }
            String command2 = "DELETE FROM authData WHERE authToken=?";
            try (var addTokenStatement = conn.prepareStatement(command2)) {
                addTokenStatement.setString(1, token);
                addTokenStatement.execute();
            }
        } catch (DataAccessException e1) {
            if (e1.getMessage().contains("not exist")){
                throw e1;
            }
            System.out.print("womp womp: " + e1.getMessage());
        } catch (SQLException e2){
            System.out.print("womp womp: " + e2.getMessage());
        }
    }

    public void clear() {
        try (var conn = getConnection()) {
            String command = "TRUNCATE authData";
            try (var clearTableStatement = conn.prepareStatement(command)) {
                clearTableStatement.execute();
            }
        } catch (SQLException | DataAccessException e) {
            System.out.print("womp womp: " + e.getMessage());
        }
    }
}
