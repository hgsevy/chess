package dataAccess;

import model.UserData;

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

    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }
}
