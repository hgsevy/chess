package dataAccessTests;

import chess.ChessGame;
import dataAccess.DataAccessException;
import dataAccess.GameDAO;
import dataAccess.SQLGameDAO;
import dataAccess.SQLUserDAO;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class GameDAOTests {

    private static GameDAO database;

    @BeforeAll
    public static void init() throws DataAccessException {
        database = new SQLGameDAO();
    }

    @Test
    public void createGood() {
        Assertions.assertNotEquals(-1, database.createGame("name"));
    }

    @Test
    public void createBad() {
        Assertions.assertEquals(-1, database.createGame(null));
    }

    @Test
    public void joinGood() {
        int id = database.createGame("name");
        Assertions.assertDoesNotThrow(()->database.joinGame("user", id, ChessGame.TeamColor.BLACK));
    }

    @Test
    public void joinBad() {
        int id = database.createGame("name");
        Assertions.assertThrows(DataAccessException.class, ()->database.joinGame("user", 7506, ChessGame.TeamColor.BLACK));
    }

    @Test
    public void listGood() {
        int id = database.createGame("name");
        Assertions.assertFalse(database.listGames().isEmpty());
    }

    @Test
    public void listBad() {
        int id = database.createGame("name");
        database.clear();
        Assertions.assertTrue(database.listGames().isEmpty());
    }

    @Test
    public void clearTest() throws DataAccessException {
        int id = database.createGame("name");
        Assertions.assertFalse(database.listGames().isEmpty());
        database.clear();
        Assertions.assertTrue(database.listGames().isEmpty());
    }
    @AfterEach
    public void cleanSlate(){
        database.clear();
    }
}
