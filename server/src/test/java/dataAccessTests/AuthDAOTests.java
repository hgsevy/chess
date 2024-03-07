package dataAccessTests;

import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import dataAccess.SQLAuthDAO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AuthDAOTests {
    @Test
    public void createAuthGood() throws DataAccessException {
        AuthDAO database = new SQLAuthDAO();
        String token = database.createAuthToken("username");
        Assertions.assertNotNull(token);
    }

    @Test
    public void createAuthBad() throws DataAccessException {
        AuthDAO database = new SQLAuthDAO();
        Assertions.assertEquals("-1", database.createAuthToken(null));
    }

    @Test
    public void getUsernameGood() throws DataAccessException {
        AuthDAO database = new SQLAuthDAO();
        String token = database.createAuthToken("username");
        Assertions.assertEquals("username", database.getUsername(token));
    }

    @Test
    public void getUsernameBad() throws DataAccessException {
        AuthDAO database = new SQLAuthDAO();
        Assertions.assertThrows(DataAccessException.class, ()->database.getUsername("wrong"));
    }

    @Test
    public void deleteAuthGood() throws DataAccessException {
        AuthDAO database = new SQLAuthDAO();
        String token = database.createAuthToken("username");
        Assertions.assertDoesNotThrow(()->database.deleteAuthToken(token));
    }

    @Test
    public void deleteAuthBad() throws DataAccessException {
        AuthDAO database = new SQLAuthDAO();
        Assertions.assertThrows(DataAccessException.class, ()->database.deleteAuthToken("wrong"));
    }

    @Test
    public void clearTest() throws DataAccessException {
        AuthDAO database = new SQLAuthDAO();
        String token = database.createAuthToken("username");
        Assertions.assertDoesNotThrow(()->database.getUsername(token));
        database.clear();
        Assertions.assertThrows(DataAccessException.class, ()->database.getUsername(token));
    }
}
