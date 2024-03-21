package clientTests;

import chess.ChessGame;
import clientAPI.ServerFacade;
import org.junit.jupiter.api.*;
import server.Server;

import ui.BadInputException;
import ui.TerminalMenus;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade thing;

    @BeforeEach
    public void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);

        thing = new ServerFacade(port);
    }

    @AfterEach
    public void stopServer() {
        server.stop();
    }

    @Test
    public void registerGood(){
        Assertions.assertDoesNotThrow(()->thing.register("username", "123", "gmail"));
    }

    @Test
    public void registerRepeat() throws BadInputException{
        thing.register("username", "123", "gmail");
        Assertions.assertThrows(BadInputException.class, ()->thing.register("username", "123", "gmail"));
    }

    @Test
    public void loginGood() throws BadInputException {
        thing.register("username", "123", "gmail");
        Assertions.assertDoesNotThrow(()->thing.login("username", "123"));
    }

    @Test
    public void loginBad() throws BadInputException {
        thing.register("username", "123", "gmail");
        Assertions.assertThrows(BadInputException.class, ()->thing.login("username", "1234"));
    }

    @Test
    public void logoutGood() throws BadInputException {
        thing.register("username", "123", "gmail");
        Assertions.assertDoesNotThrow(()->thing.logout());
    }

    @Test
    public void logoutBad() {
        Assertions.assertThrows(BadInputException.class, ()->thing.logout());
    }

    @Test
    public void createGood() throws BadInputException {
        thing.register("username", "123", "gmail");
        Assertions.assertDoesNotThrow(()->thing.create("name"));
    }

    @Test
    public void createBad() {
        Assertions.assertThrows(BadInputException.class, ()->thing.create("name"));
    }

    @Test
    public void listGood() throws BadInputException {
        thing.register("username", "123", "gmail");
        thing.create("gmae");
        Assertions.assertDoesNotThrow(()->thing.list());
    }

    @Test
    public void listBad() throws BadInputException {
        thing.register("username", "123", "gmail");
        thing.create("gmae");
        thing.logout();
        Assertions.assertThrows(BadInputException.class, ()->thing.list());
    }

    @Test
    public void joinGood() throws BadInputException {
        thing.register("username", "123", "gmail");
        thing.create("gmae");
        int id = thing.list().getFirst().gameID();
        Assertions.assertDoesNotThrow(()->thing.join(id, ChessGame.TeamColor.BLACK));
    }

    @Test
    public void joinBad() throws BadInputException {
        thing.register("username", "123", "gmail");
        thing.create("gmae");
        int id = thing.list().getFirst().gameID();
        Assertions.assertThrows(BadInputException.class, ()->thing.join(id+2, ChessGame.TeamColor.BLACK));
    }

    @Test
    public void isLogged() throws BadInputException {
        thing.register("username", "123", "gmail");
        Assertions.assertTrue(thing.isLoggedIn());
    }

    @Test
    public void isNotLogged() throws BadInputException {
        thing.register("username", "123", "gmail");
        thing.logout();
        Assertions.assertFalse(thing.isLoggedIn());
    }











}
