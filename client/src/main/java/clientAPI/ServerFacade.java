package clientAPI;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import model.UserData;
import model.requestsAndResults.*;
import ui.BadInputException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ServerFacade {
    int portNum;

    private String authToken = "";
    private NotificationHandler notificationHandler;

    public ServerFacade(int portNum, NotificationHandler notificationHandler) {
        this.portNum = portNum;
        this.notificationHandler = notificationHandler;
    }

    public void register(String username, String password, String email) throws BadInputException {
        URL url;
        try {
            url = new URL("http://localhost:"+portNum+"/user");
        } catch (MalformedURLException e1){
            throw new BadInputException("wrong url");
        }

        HttpURLConnection connection;

        UserData req = new UserData(username, password, email);
        try{
            connection = HTTP(url, "POST", new Gson().toJson(req));
        } catch (IOException e1){
            throw new BadInputException("can't connect to server: " + e1.getMessage());
        }

        authToken = readAuthToken(connection);
    }

    public void login(String username, String password) throws BadInputException {
        URL url;
        HttpURLConnection connection;
        try {
            url = new URL("http://localhost:"+portNum+"/session");
        } catch (IOException e1){
            throw new BadInputException("bad url");
        }

        LoginRequest req = new LoginRequest(username, password);
        try{
            connection = HTTP(url, "POST", new Gson().toJson(req));
        } catch (IOException e1){
            throw new BadInputException("can't connect to server: " + e1.getMessage());
        }

        authToken = readAuthToken(connection);
    }

    private String readAuthToken(HttpURLConnection connection) throws BadInputException {
        try {
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream responseBody = connection.getInputStream();
                String output = new String(responseBody.readAllBytes(), StandardCharsets.UTF_8);
                return new Gson().fromJson(output, LoginResult.class).authToken();
            } else {
                // SERVER RETURNED AN HTTP ERROR
                InputStream responseBody = connection.getErrorStream();
                String output = new String(responseBody.readAllBytes(), StandardCharsets.UTF_8);
                throw new BadInputException(new Gson().fromJson(output, MessageResponse.class).message());
            }
        } catch (IOException e1){
            throw new BadInputException("couldn't read JSON: " + e1.getMessage());
        }
    }

    public void logout() throws BadInputException {
        URL url;
        HttpURLConnection connection;
        try {
            url = new URL("http://localhost:"+portNum+"/session");
        } catch (IOException e1){
            throw new BadInputException("bad url");
        }

        try{
            connection = HTTP(url, "DELETE", "");
        } catch (IOException e1){
            throw new BadInputException("can't connect to server: " + e1.getMessage());
        }

        try {
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                authToken = "";
            } else {
                // SERVER RETURNED AN HTTP ERROR
                InputStream responseBody = connection.getErrorStream();
                String output = new String(responseBody.readAllBytes(), StandardCharsets.UTF_8);
                throw new BadInputException(new Gson().fromJson(output, MessageResponse.class).message());
            }
        } catch (IOException e1){
            throw new BadInputException(e1.getMessage());
        }
    }

    public void create(String name) throws BadInputException {
        URL url;
        HttpURLConnection connection;
        try {
            url = new URL("http://localhost:"+portNum+"/game");
        } catch (IOException e1){
            throw new BadInputException("bad url");
        }

        CreateGameRequest req = new CreateGameRequest(name);

        try{
            connection = HTTP(url, "POST", new Gson().toJson(req));
        } catch (IOException e1){
            throw new BadInputException("can't connect to server: " + e1.getMessage());
        }

        checkForError(connection);
    }

    public ArrayList<GameData> list() throws BadInputException {
        URL url;
        HttpURLConnection connection;
        try {
            url = new URL("http://localhost:"+portNum+"/game");
        } catch (IOException e1){
            throw new BadInputException("bad url");
        }

        try{
            connection = HTTP(url, "GET", "");
        } catch (IOException e1){
            throw new BadInputException("can't connect to server: " + e1.getMessage());
        }

        try {
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream responseBody = connection.getInputStream();
                String output = new String(responseBody.readAllBytes(), StandardCharsets.UTF_8);
                return new Gson().fromJson(output, ListGamesResponse.class).games();
            } else {
                // SERVER RETURNED AN HTTP ERROR
                InputStream responseBody = connection.getErrorStream();

                String output = new String(responseBody.readAllBytes(), StandardCharsets.UTF_8);
                throw new BadInputException(new Gson().fromJson(output, MessageResponse.class).message());
            }
        } catch (IOException e1){
            throw new BadInputException(e1.getMessage());
        }
    }

    public WSClient join(int id, ChessGame.TeamColor color) throws BadInputException {
        URL url;
        HttpURLConnection connection;
        try {
            url = new URL("http://localhost:"+portNum+"/game");
        } catch (IOException e1){
            throw new BadInputException("bad url");
        }

        JoinGameRequest req = new JoinGameRequest(color, id);
        try{
            connection = HTTP(url, "PUT", new Gson().toJson(req));
        } catch (IOException e1){
            throw new BadInputException("can't connect to server: " + e1.getMessage());
        }

        checkForError(connection);

        try {
            return new WSClient(portNum, authToken, req.gameID(), notificationHandler);
        } catch (Exception e1){
            throw new BadInputException("couldn't websocket because " + e1.getMessage());
        }
    }

    private void checkForError(HttpURLConnection connection) throws BadInputException {
        try {
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                // SERVER RETURNED AN HTTP ERROR
                InputStream responseBody = connection.getErrorStream();
                String output = new String(responseBody.readAllBytes(), StandardCharsets.UTF_8);
                throw new BadInputException(new Gson().fromJson(output, MessageResponse.class).message());
            }
        } catch (IOException e1){
            throw new BadInputException(e1.getMessage());
        }
    }

    private HttpURLConnection HTTP(URL url, String method, String req) throws IOException {
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e1){
            System.out.println("You mistyped the url on clientAPI.ServerFacade line 35");
            throw new IOException("bad website");
        }

        connection.setReadTimeout(5000);
        try {
            connection.setRequestMethod(method);
            if (!method.equals("GET")) {
                connection.setDoOutput(true);
            }
        } catch (ProtocolException e1){
            System.out.println("You used the wrong HTTP method on line 200");
        }

        if (!authToken.isEmpty()){
            connection.addRequestProperty("Authorization", authToken);
        }

        if (!method.equals("GET")) {
            try (OutputStream requestBody = connection.getOutputStream()) {
                requestBody.write(req.getBytes());
            } catch (IOException e) {
                System.out.println("Error on line 210");
                System.out.println(e.getMessage());
            }
        }

        return connection;
    }

    public boolean isLoggedIn(){
        return !authToken.isEmpty();
    }
}
