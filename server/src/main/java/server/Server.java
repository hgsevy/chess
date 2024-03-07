package server;

import com.google.gson.Gson;
import dataAccess.*;
import model.GameData;
import model.UserData;
import service.*;
import service.exceptions.BadInputException;
import service.exceptions.NoCanDoException;
import service.exceptions.ServiceException;
import service.exceptions.UnauthorizedException;
import service.requestsAndResults.*;
import spark.*;

import java.util.ArrayList;

public class Server {

    ClearService clearService;
    UserService userService;
    GameService gameService;

    public Server() {
        System.out.println("test");
        try {
            UserDAO testDAO = new SQLUserDAO();
        }
        catch (DataAccessException expt1){
            System.out.println(expt1.getMessage());
        }
        UserDAO userData = new MemoryUserDAO();
        AuthDAO authData = new MemoryAuthDAO();
        GameDAO gameData = new MemoryGameDAO();

        clearService = new ClearService(userData, authData, gameData);
        userService = new UserService(userData, authData);
        gameService = new GameService(authData, gameData);
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
        res.body(new Gson().toJson(new MessageResponse(ex.getMessage()), MessageResponse.class));
    }

    private Object clearHandler(Request req, Response resp){
        clearService.clear();
        resp.status(200);
        return new Gson().toJson(new MessageResponse("test clear"));
    }

    private Object registerHandler(Request req, Response resp) throws NoCanDoException, BadInputException {
        UserData newUser = new Gson().fromJson(req.body(), UserData.class);
        LoginResult result = userService.register(newUser);
        resp.status(200);
        String message = new Gson().toJson(result);
        resp.body(message);
        return message;
    }

    private Object loginHandler(Request req, Response resp) throws UnauthorizedException, BadInputException {
        LoginRequest enteredData = new Gson().fromJson(req.body(), LoginRequest.class);
        LoginResult result = userService.login(enteredData);
        resp.status(200);
        resp.body(new Gson().toJson(result));
        return resp.body();
    }

    private Object logoutHandler(Request req, Response resp) throws UnauthorizedException {
        String token = req.headers("authorization");
        userService.logout(token);
        resp.status(200);
        return "";
    }

    private Object listGamesHandler(Request req, Response resp) throws UnauthorizedException {
        String token = req.headers("authorization");
        ArrayList<GameData> list = gameService.list(token);
        resp.status(200);
        ListGamesResponse answer = new ListGamesResponse(list);
        resp.body(new Gson().toJson(answer, ListGamesResponse.class));
        return resp.body();
    }

    private Object createGameHandler(Request req, Response resp) throws UnauthorizedException {
        String authToken = req.headers("authorization");
        CreateGameRequest enteredData = new Gson().fromJson(req.body(), CreateGameRequest.class);
        int gameID = gameService.create(authToken, enteredData.gameName());
        resp.status(200);
        resp.body(new Gson().toJson(new CreateGameResponse(gameID)));
        return resp.body();
    }

    private Object joinGameHandler(Request req, Response resp) throws NoCanDoException, UnauthorizedException, BadInputException {
        String authToken = req.headers("authorization");
        JoinGameRequest gameDetails = new Gson().fromJson(req.body(), JoinGameRequest.class);
        gameService.join(authToken, gameDetails);
        resp.status(200);
        return new Gson().toJson(new MessageResponse(""));
    }

    // spark.exception


}
