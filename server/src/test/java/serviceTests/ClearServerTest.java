package serviceTests;

import dataAccess.*;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import service.ClearService;
import service.UserService;

public class ClearServerTest {
    @Test
    public void clearTest() throws DataAccessException {
        UserDAO userData = new MemoryUserDAO();
        AuthDAO authData = new MemoryAuthDAO();
        GameDAO gameData = new MemoryGameDAO();

        ClearService clearService = new ClearService(userData, authData, gameData);

        String username = "johndoe";

        userData.createUser(new UserData(username, "12345", "jdoe@yahoo.com"));
        String token = authData.createAuthToken(username);
        gameData.createGame("cool game bruh");

        clearService.clear();

        Assertions.assertThrows(DataAccessException.class, ()->userData.getUser(username));
        Assertions.assertThrows(DataAccessException.class, ()->authData.getAuth(token));
        Assertions.assertTrue(gameData.listGames().isEmpty());
    }
}
