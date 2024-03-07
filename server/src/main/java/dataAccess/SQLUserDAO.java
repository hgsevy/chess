package dataAccess;

import model.UserData;

import java.sql.ResultSet;
import java.sql.SQLException;

import static dataAccess.DatabaseManager.getConnection;

public class SQLUserDAO implements UserDAO{



    public SQLUserDAO() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = getConnection()) {
            //conn.setCatalog("userData"); MIGHT NOT NEED

            var createUserTable = """
            CREATE TABLE  IF NOT EXISTS userData (
                username varchar(255) NOT NULL,
                password VARCHAR(255) NOT NULL,
                email varchar(255) NOT NULL,
                PRIMARY KEY (username)
            )
            """;

            try (var createTableStatement = conn.prepareStatement(createUserTable)) {
                createTableStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("trouble making table; " + e.getMessage());
        }
    }

    @Override
    public void clear() {
        try (var conn = getConnection()) {
            String command = "TRUNCATE userData";
            try (var clearTableStatement = conn.prepareStatement(command)) {
                clearTableStatement.execute();
            }
        } catch (SQLException | DataAccessException e) {
            System.out.print("womp womp: " + e.getMessage());
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        try (var conn = getConnection()) {
            var checkTable = "SELECT username FROM userData WHERE username=? OR email=?";
            try (var selectTableStatement = conn.prepareStatement(checkTable)) {
                selectTableStatement.setString(1, user.username());
                selectTableStatement.setString(2, user.email());
                ResultSet rs = selectTableStatement.executeQuery();
                if(rs.next()){
                    throw new DataAccessException("email or username already used by another user");
                }
            }
            var addUser = "INSERT INTO userData(username, password, email) VALUE (?, ?, ?)";
            try (var insertTableStatement = conn.prepareStatement(addUser)) {
                insertTableStatement.setString(1, user.username());
                insertTableStatement.setString(2, user.password());
                insertTableStatement.setString(3, user.email());
                insertTableStatement.execute();
            }
        } catch (SQLException | DataAccessException e) {
            System.out.print("womp womp: " + e.getMessage());
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }
}
