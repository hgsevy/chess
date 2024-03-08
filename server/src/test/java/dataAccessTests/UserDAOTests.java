package dataAccessTests;

import dataAccess.DataAccessException;
import dataAccess.SQLUserDAO;
import dataAccess.UserDAO;
import model.UserData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class UserDAOTests {
    private static UserDAO database;

    @BeforeAll
    public static void init() throws DataAccessException {
        database = new SQLUserDAO();
    }

    @Test
    public void clearTest() throws DataAccessException {
        database.createUser(new UserData("u", "p", "e"));
        Assertions.assertDoesNotThrow(()->database.getUser("u"));
        database.clear();
        Assertions.assertThrows(DataAccessException.class, ()->database.getUser("u"));
    }

    @Test
    public void createGood() {
        Assertions.assertDoesNotThrow(()->database.createUser(new UserData("u", "p", "e")));
    }

    @Test
    public void createDuplicate() throws DataAccessException {
        database.createUser(new UserData("u", "p", "e"));
        Assertions.assertThrows(DataAccessException.class, ()->database.createUser(new UserData("u", "p", "e")));
    }

    @Test
    public void getGood() throws DataAccessException {
        UserData data = new UserData("u", "p", "e");
        database.createUser(data);
        Assertions.assertEquals(data, database.getUser("u"));
    }

    @Test
    public void getBad(){
        Assertions.assertThrows(DataAccessException.class, ()->database.getUser("u"));
    }

    @AfterEach
    public void cleanSlate(){
        database.clear();
    }
}
