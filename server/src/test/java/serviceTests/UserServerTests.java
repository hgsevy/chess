package serviceTests;

import dataAccess.MemoryAuthDAO;
import dataAccess.MemoryUserDAO;
import dataAccess.UserDAO;
import model.UserData;
import org.junit.jupiter.api.*;
import service.requestsAndResults.LoginRequest;
import service.requestsAndResults.LoginResult;
import service.UserService;
import service.exceptions.BadInputException;
import service.exceptions.NoCanDoException;
import service.exceptions.UnauthorizedException;

public class UserServerTests {

    private UserService service;
    private MemoryAuthDAO authData;

    @BeforeEach
    public void createObjects(){
        authData = new MemoryAuthDAO();
        UserDAO userData = new MemoryUserDAO();
        service = new UserService(userData, authData);
    }

    @Test
    @DisplayName("Successful Registration")
    public void goodRegister() throws NoCanDoException, BadInputException {
        String username = "johndoe";
        String password = "12345";
        String email = "jdoe@yahoo.com";
        LoginResult result = service.register(new UserData(username, password, email));
        Assertions.assertEquals(username, result.username());
        Assertions.assertNotNull(result.authToken());
    }

    @Test
    @DisplayName("Double Registration")
    public void dublicateRegister() throws NoCanDoException, BadInputException {
        String username = "johndoe";
        String password = "12345";
        String email = "jdoe@yahoo.com";
        UserData data = new UserData(username, password, email);
        service.register(data);
        Assertions.assertThrows(NoCanDoException.class, ()->service.register(data));
    }

    @Test
    @DisplayName("Incomplete Registration")
    public void incompleteRegister() {
        String username = "johndoe";
        String password = "12345";
        UserData data = new UserData(username, password, null);
        Assertions.assertThrows(BadInputException.class, ()->service.register(data));
    }

    @Test
    @DisplayName("Good Login")
    public void goodLogin() throws NoCanDoException, BadInputException, UnauthorizedException {
        String username = "johndoe";
        String password = "12345";
        String email = "jdoe@yahoo.com";
        UserData data = new UserData(username, password, email);
        service.register(data);

        LoginResult result = service.login(new LoginRequest(username, password));
        Assertions.assertEquals(username, result.username());
        Assertions.assertNotNull(result.authToken());

        Assertions.assertDoesNotThrow(()->authData.getUsername(result.authToken()));
    }

    @Test
    @DisplayName("Bad Login")
    public void badLogin() throws NoCanDoException, BadInputException {
        String username = "johndoe";
        String password = "12345";
        String email = "jdoe@yahoo.com";
        UserData data = new UserData(username, password, email);
        service.register(data);

        Assertions.assertThrows(UnauthorizedException.class, ()->service.login(new LoginRequest(username, "54321")));
    }

    @Test
    @DisplayName("Good Logout")
    public void logout() throws NoCanDoException, BadInputException {
        String username = "johndoe";
        String password = "12345";
        String email = "jdoe@yahoo.com";
        UserData data = new UserData(username, password, email);
        LoginResult result = service.register(data);
        // make sure auth token exists
        Assertions.assertDoesNotThrow(()->service.logout(result.authToken()));
        // make sure token got deleted
        Assertions.assertThrows(UnauthorizedException.class, ()->service.logout(result.authToken()));
    }

    @Test
    @DisplayName("Bad Logout")
    public void BadLogout() {
        Assertions.assertThrows(UnauthorizedException.class, ()->service.logout("username"));
    }
}
