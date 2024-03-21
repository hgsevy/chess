package clientTests;

import chess.ChessGame;
import clientAPI.ServerFacade;
import org.junit.jupiter.api.*;
import server.Server;

import ui.BadInputException;


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
        thing.register("username1", "123", "gmail1");
        Assertions.assertThrows(BadInputException.class, ()->thing.register("username1", "123", "gmail1"));
    }

    @Test
    public void loginGood() throws BadInputException {
        thing.register("username2", "123", "gmail2");
        Assertions.assertDoesNotThrow(()->thing.login("username2", "123"));
    }

    @Test
    public void loginBad() throws BadInputException {
        thing.register("username3", "123", "gmail3");
        Assertions.assertThrows(BadInputException.class, ()->thing.login("username", "1234"));
    }

    @Test
    public void logoutGood() throws BadInputException {
        thing.register("username4", "123", "gmail4");
        Assertions.assertDoesNotThrow(()->thing.logout());
    }

    @Test
    public void logoutBad() {
        Assertions.assertThrows(BadInputException.class, ()->thing.logout());
    }

    @Test
    public void createGood() throws BadInputException {
        thing.register("username5", "123", "gmail5");
        Assertions.assertDoesNotThrow(()->thing.create("name"));
    }

    @Test
    public void createBad() {
        Assertions.assertThrows(BadInputException.class, ()->thing.create("name"));
    }

    @Test
    public void listGood() throws BadInputException {
        thing.register("username6", "123", "gmail6");
        thing.create("gmae");
        Assertions.assertDoesNotThrow(()->thing.list());
    }

    @Test
    public void listBad() throws BadInputException {
        thing.register("username7", "123", "gmail7");
        thing.create("gmae");
        thing.logout();
        Assertions.assertThrows(BadInputException.class, ()->thing.list());
    }

    @Test
    public void joinGood() throws BadInputException {
        thing.register("username8", "123", "gmail8");
        thing.create("gmae");
        int id = thing.list().getFirst().gameID();
        Assertions.assertDoesNotThrow(()->thing.join(id, ChessGame.TeamColor.BLACK));
    }

    @Test
    public void joinBad() throws BadInputException {
        thing.register("username9", "123", "gmail9");
        thing.create("gmae");
        int id = thing.list().getFirst().gameID();
        Assertions.assertThrows(BadInputException.class, ()->thing.join(id+2, ChessGame.TeamColor.BLACK));
    }

    @Test
    public void isLogged() throws BadInputException {
        thing.register("username10", "123", "gmail10");
        Assertions.assertTrue(thing.isLoggedIn());
    }

    @Test
    public void isNotLogged() throws BadInputException {
        thing.register("username11", "123", "gmail11");
        thing.logout();
        Assertions.assertFalse(thing.isLoggedIn());
    }











}
