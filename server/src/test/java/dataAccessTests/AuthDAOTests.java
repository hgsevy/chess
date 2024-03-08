package dataAccessTests;

import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import dataAccess.SQLAuthDAO;
import org.junit.jupiter.api.*;

public class AuthDAOTests {

    static AuthDAO database;

    @BeforeAll
    public static void init() throws DataAccessException {
        database = new SQLAuthDAO();
    }
    @Test
    public void createAuthGood(){
        String token = database.createAuthToken("username");
        Assertions.assertNotNull(token);
    }

    @Test
    public void createAuthBad(){
        Assertions.assertEquals("-1", database.createAuthToken(null));
    }

    @Test
    public void getUsernameGood() throws DataAccessException {
        String token = database.createAuthToken("username");
        Assertions.assertEquals("username", database.getUsername(token));
    }

    @Test
    public void getUsernameBad(){
        Assertions.assertThrows(DataAccessException.class, ()->database.getUsername("wrong"));
    }

    @Test
    public void deleteAuthGood(){
        String token = database.createAuthToken("username");
        Assertions.assertDoesNotThrow(()->database.deleteAuthToken(token));
    }

    @Test
    public void deleteAuthBad(){
        Assertions.assertThrows(DataAccessException.class, ()->database.deleteAuthToken("wrong"));
    }

    @Test
    public void clearTest(){
        String token = database.createAuthToken("username");
        Assertions.assertDoesNotThrow(()->database.getUsername(token));
        database.clear();
        Assertions.assertThrows(DataAccessException.class, ()->database.getUsername(token));
    }

    @AfterEach
    public void cleanSlate(){
        database.clear();
    }
}
