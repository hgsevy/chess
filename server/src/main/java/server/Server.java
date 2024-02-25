package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dataAccess.*;
import model.GameData;
import model.UserData;
import service.*;
import service.exceptions.NoCanDoException;
import service.exceptions.ServiceException;
import service.exceptions.UnauthorizedException;
import spark.*;

import java.util.Collection;

public class Server {

    ClearService clearService;
    UserService userService;
    GameService gameService;

    public Server(){
        UserDAO userData = new MemoryUserDAO();
        AuthDAO authData = new MemoryAuthDAO();
        GameDAO gameData = new MemoryGameDAO();

        clearService = new ClearService(userData, authData, gameData);
        userService = new UserService(userData, authData);
        gameService = new GameService(userData, authData, gameData);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        //clear
        Spark.delete("/db", this::clearHandler);
        //register
        Spark.post("/user", this::registerHandler);
        //login
        Spark.post("/session", this::loginHandler);
        //logout
        Spark.delete("/session", this::logoutHandler);

        //list games
        Spark.get("/game", this::listGamesHandler);
        //create game
        Spark.post("/game", this::createGameHandler);
        //join game
        Spark.put("/game", this::joinGameHandler);

        // exception handler??
        Spark.exception(ServiceException.class,this::exceptionHandler );

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private void exceptionHandler(ServiceException ex, Request req, Response res) {
        res.status(ex.getStatusCode());
        res.body(new Gson().toJson(ex));
    }

    private Object clearHandler(Request req, Response resp){
        clearService.clear();
        resp.status(200);
        return "";
    }

    private Object registerHandler(Request req, Response resp) throws NoCanDoException {
        UserData newUser = new Gson().fromJson(req.body(), UserData.class);
        LoginResult result = userService.register(newUser);
        resp.status(200);
        String message = new Gson().toJson(result);
        resp.body(message);
        return message;
    }

    private Object loginHandler(Request req, Response resp) throws UnauthorizedException {
        LoginRequest enteredData = new Gson().fromJson(req.body(), LoginRequest.class);
        LoginResult result = userService.login(enteredData);
        resp.status(200);
        resp.body(new Gson().toJson(result));
        return resp.body();
    }

    private Object logoutHandler(Request req, Response resp) throws UnauthorizedException {
        String token = new Gson().fromJson(req.body(), String.class);
        userService.logout(token);
        resp.status(200);
        return resp.body();
    }

    private Object listGamesHandler(Request req, Response resp) throws UnauthorizedException {
        String token = new Gson().fromJson(req.body(), String.class);
        Collection<GameData> list = gameService.list(token);
        resp.status(200);
        resp.body(new Gson().toJson(list, Collection.class));
        return resp.body();
    }

    private Object createGameHandler(Request req, Response resp) throws UnauthorizedException {
        CreateGameRequest gameDetails = new Gson().fromJson(req.body(), CreateGameRequest.class);
        int gameID = gameService.create(gameDetails);
        resp.status(200);
        resp.body(new Gson().toJson(gameID, int.class));
        return resp.body();
    }

    private Object joinGameHandler(Request req, Response resp) throws NoCanDoException, UnauthorizedException {
        JoinGameRequest gameDetails = new Gson().fromJson(req.body(), JoinGameRequest.class);
        gameService.join(gameDetails);
        resp.status(200);
        return resp.body();
    }

    // spark.exception


}
