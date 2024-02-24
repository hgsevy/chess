package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dataAccess.*;
import model.GameData;
import model.UserData;
import service.*;
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
        Spark.delete("/dp", this::clearHandler);

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

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private Object clearHandler(Request req, Response resp){
        try {
            clearService.clear();
            resp.status(200);
        } catch (Exception what){
            resp.status(500);
            resp.body("Something went wrong but I have no idea what");
        }
        return resp;
    }

    private Object registerHandler(Request req, Response resp){
        try {
            UserData newUser = new Gson().fromJson(req.body(), UserData.class);
            LoginResult result = userService.register(newUser);
            resp.status(200);
            resp.body(new Gson().toJson(result));
        } catch (JsonSyntaxException expt1){
            resp.status(400);
            resp.body("Error: bad request");
        } catch (DataAccessException expt2){
            resp.status(403);
            resp.body(expt2.getMessage());
        } catch (Exception what){
            resp.status(500);
            resp.body("Error: this shouldn't happen ever");
        }
        return resp;
    }

    private Object loginHandler(Request req, Response resp){
        try{
            LoginRequest enteredData = new Gson().fromJson(req.body(), LoginRequest.class);
            LoginResult result = userService.login(enteredData);
            resp.status(200);
            resp.body(new Gson().toJson(result));
        } catch (DataAccessException | UnauthorizedException expt1){
            resp.status(401);
            resp.body("Error: unauthorized");
        } catch (Exception what){
            resp.status(500);
            resp.body("Error: please don't ever");
        }
        return resp;
    }

    private Object logoutHandler(Request req, Response resp){
        try{
            String token = new Gson().fromJson(req.body(), String.class);
            userService.logout(token);
            resp.status(200);
        } catch (DataAccessException expt1){
            resp.status(401);
            resp.body("Error: unauthorized");
        } catch (Exception what){
            resp.status(500);
            resp.body("Error: something bad happened");
        }
        return resp;
    }

    private Object listGamesHandler(Request req, Response resp){
        try{
            String token = new Gson().fromJson(req.body(), String.class);
            Collection<GameData> list = gameService.list(token);
            resp.status(200);
            resp.body(new Gson().toJson(list, Collection.class));
        } catch (UnauthorizedException expt1){
            resp.status(401);
            resp.body("Error: unauthorized");
        } catch (Exception what){
            resp.status(500);
            resp.body("Error: something bad happened here");
        }
        return resp;
    }

    private Object createGameHandler(Request req, Response resp){
        try{
            CreateGameRequest gameDetails = new Gson().fromJson(req.body(), CreateGameRequest.class);
            int gameID = gameService.create(gameDetails);
            resp.status(200);
            resp.body(new Gson().toJson(gameID, int.class));
        } catch (JsonSyntaxException expt1){
            resp.status(400);
            resp.body("Error: bad request");
        } catch (UnauthorizedException expt1){
            resp.status(401);
            resp.body("Error: unauthorized");
        } catch (Exception what){
            resp.status(500);
            resp.body("Error: something bad happened here");
        }
        return resp;
    }

    private Object joinGameHandler(Request req, Response resp){
        try{
            JoinGameRequest gameDetails = new Gson().fromJson(req.body(), JoinGameRequest.class);
            gameService.join(gameDetails);
            resp.status(200);
        } catch (JsonSyntaxException expt1){
            resp.status(400);
            resp.body("Error: bad request");
        } catch (UnauthorizedException expt2){
            resp.status(401);
            resp.body("Error: unauthorized");
        } catch (DataAccessException expt3){
            resp.status(403);
            resp.body("Error: already taken");
        } catch (Exception what){
            resp.status(500);
            resp.body("Error: something bad happened here");
        }
        return resp;
    }

}
