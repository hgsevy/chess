package serviceTests;

import chess.ChessGame;
import dataAccess.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.GameService;
import service.exceptions.BadInputException;
import service.exceptions.NoCanDoException;
import service.exceptions.UnauthorizedException;
import service.requestsAndResults.JoinGameRequest;

public class GameServiceTests {
    private GameService gameService;
    private AuthDAO authData;
    private GameDAO gameData;
    @BeforeEach
    public void setup(){
        authData = new MemoryAuthDAO();
        gameData = new MemoryGameDAO();

        gameService = new GameService(authData, gameData);
    }

    @Test
    @DisplayName("Successful Join")
    public void joinTest(){
        String username = "jane";
        String token = authData.createAuthToken(username);
        int gameID = gameData.createGame("test");

        Assertions.assertDoesNotThrow(()->gameService.join(token, new JoinGameRequest(ChessGame.TeamColor.BLACK, gameID)));
    }

    @Test
    @DisplayName("Join Game that Doesn't Exist")
    public void joinFakeGame(){
        String username = "jane";
        String token = authData.createAuthToken(username);

        Assertions.assertThrows(BadInputException.class, ()->gameService.join(token, new JoinGameRequest(ChessGame.TeamColor.BLACK, 4)));
    }

    @Test
    @DisplayName("Join Game that's already taken")
    public void joinTakenGame() throws DataAccessException {
        String username1 = "jill";
        authData.createAuthToken(username1);
        int gameID = gameData.createGame("test");

        gameData.joinGame(username1, gameID, ChessGame.TeamColor.BLACK);

        String username2 = "jack";
        String token2 = authData.createAuthToken(username2);

        Assertions.assertThrows(NoCanDoException.class, ()->gameService.join(token2, new JoinGameRequest(ChessGame.TeamColor.BLACK, gameID)));
    }

    @Test
    @DisplayName("Join Game without good auth Token")
    public void joinGameUnauthorized(){
        int gameID = gameData.createGame("test");

        Assertions.assertThrows(UnauthorizedException.class, ()->gameService.join("bad token", new JoinGameRequest(ChessGame.TeamColor.BLACK, gameID)));
    }

    @Test
    @DisplayName("ListGames")
    public void listGamesTest() throws UnauthorizedException, DataAccessException {
        String token = authData.createAuthToken("paul");

        int gameID1 = gameData.createGame("test1");
        int gameID2 = gameData.createGame("test2");
        int gameID3 = gameData.createGame("test3");
        int gameID4 = gameData.createGame("test4");

        gameData.joinGame("jane", gameID3, ChessGame.TeamColor.BLACK);

        Assertions.assertEquals(4, gameService.list(token).size());
        // TODO: figure out how to make sure the lists are equal
    }

    @Test
    @DisplayName("ListGames with Authorization")
    public void listGamesUnauthorized() {
        Assertions.assertThrows(UnauthorizedException.class, ()->gameService.list("bad token").size());
    }

    @Test
    @DisplayName("Create Game")
    public void createGameTest(){
        String token = authData.createAuthToken("june");

        Assertions.assertDoesNotThrow(()->gameService.create(token, "name"));
        Assertions.assertEquals(1, gameData.listGames().size());
    }

    @Test
    @DisplayName("Create Game without Auth Token")
    public void createGameUnauthorized(){
        Assertions.assertThrows(UnauthorizedException.class, ()->gameService.create("bad token", "name"));
    }

}
