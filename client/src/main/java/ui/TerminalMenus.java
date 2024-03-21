package ui;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import model.UserData;
import model.requestsAndResults.LoginRequest;
import model.requestsAndResults.LoginResult;
import model.requestsAndResults.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ProtocolException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static ui.EscapeSequences.*;

public class TerminalMenus {
    private String authToken = "";
    private String username = "";
    private ArrayList<GameData> gameList;
    public void runThis(){

        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.print(ERASE_SCREEN);
        out.println("Welcome to the chess terminal. Possible commands are listed below");
        Scanner scanner = new Scanner(System.in);
        String line = "help";

        do{
            if (authToken.isEmpty()) { // pre-login menu
                if (line.contains("register")) {
                    try {
                        register(out, line);
                    } catch (BadInputException | IOException e1) {
                        errorDisplay(out);
                        out.println(e1.getMessage());
                    }
                } else if (line.contains("login")) {
                    try {
                        login(out, line);
                    } catch (BadInputException | IOException e1) {
                        errorDisplay(out);
                        out.println(e1.getMessage() + " try again");
                    }
                } else if (line.equals("help")) {
                    helpPreLoginDisplay(out);
                } else {
                    errorDisplay(out);
                    out.println("Unrecognised command");
                }
            }
            else { // post login menu
               if (line.contains("help")){
                   helpPostLoginDisplay(out);
               }
               else if (line.contains("logout")){
                   try {
                       logout(out);
                   } catch (BadInputException e1){
                       errorDisplay(out);
                       out.println("There was an exception thrown, try again");
                   }
               }
               else if (line.contains("create")){
                   try{
                       create(out, line);
                   } catch (BadInputException e1){
                       errorDisplay(out);
                       out.println(e1.getMessage());
                   }
               }
               else if (line.contains("list")){
                   try{
                       list(out);
                   } catch (BadInputException e1){
                       errorDisplay(out);
                       out.println(e1.getMessage());
                   }
               }
               else if (line.contains("join") | line.contains("observe")){
                   try{
                       join(out, line);
                   } catch (BadInputException e1){
                       errorDisplay(out);
                       out.println(e1.getMessage());
                   }
               } else {
                   errorDisplay(out);
                   out.println("Unrecognised command");
               }
            }
            printPrompt(out);
            line = scanner.nextLine();
        } while (!line.equals("quit"));
    }
    private static void helpPreLoginDisplay(PrintStream out){
        out.println("register <USERNAME> <PASSWORD> <EMAIL>: create an account by entering username, password, and email");
        out.println("login <USERNAME> <PASSWORD>: play chess after entering a valid username and password");
        out.println("quit: exits the program");
        out.println("help: displays possible commands and explanations");
        out.println();
    }

    private static void helpPostLoginDisplay(PrintStream out){
        out.println("create <NAME>: create a game by entering the name");
        out.println("list: list all games");
        out.println("join <ID> WHITE|BLACK|<empty>: joins a created chess game");
        out.println("observe <ID>: observe a create chess game");
        out.println("logout");
        out.println("quit: exits the program");
        out.println("help: displays possible commands and explanations");
        out.println();
    }

    private void register(PrintStream out, String command) throws IOException, BadInputException {
        String[] words = command.split(" ");
        if (words.length != 4){
            errorDisplay(out);
            throw new BadInputException("wrong number of arguments");
        }
        URL url;
        HttpURLConnection connection;
        try {
            url = new URL("http://localhost:8080/user");
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e1){
            System.out.println("You mistyped the url on line 75");
            throw new IOException("bad website");
        }

        connection.setReadTimeout(5000);
        try {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
        } catch (ProtocolException e1){
            System.out.println("You used the wrong HTTP method on line 83");
        }

        try(OutputStream requestBody = connection.getOutputStream();) {
            UserData input = new UserData(words[1], words[2], words[3]);
            requestBody.write((new Gson().toJson(input)).getBytes());
        } catch (IOException e) {
            System.out.println("Error on line 90");
            System.out.println(e.getMessage());
        }


        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream responseBody = connection.getInputStream();
            String output = new String(responseBody.readAllBytes(), StandardCharsets.UTF_8);
            authToken = new Gson().fromJson(output, LoginResult.class).authToken();
            username = words[1];
        } else {
            // SERVER RETURNED AN HTTP ERROR
            InputStream responseBody = connection.getErrorStream();
            String output = new String(responseBody.readAllBytes(), StandardCharsets.UTF_8);
            throw new BadInputException(new Gson().fromJson(output, MessageResponse.class).message());
        }
    }

    private void login(PrintStream out, String command) throws IOException, BadInputException {
        String[] words = command.split(" ");
        if (words.length != 3){
            errorDisplay(out);
            out.println("you had " + (words.length - 1) + " arguments when you should have had 2");
            throw new BadInputException("wrong number of arguments");
        }

        URL url;
        HttpURLConnection connection;
        try {
            url = new URL("http://localhost:8080/session");
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e1){
            System.out.println("You mistyped the url on line 121");
            return;
        }

        connection.setReadTimeout(5000);
        try {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
        } catch (ProtocolException e1){
            System.out.println("You used the wrong HTTP method on line 130");
        }

        try(OutputStream requestBody = connection.getOutputStream();) {
            LoginRequest req = new LoginRequest(words[1], words[2]);
            requestBody.write((new Gson().toJson(req)).getBytes());
        } catch (IOException e) {
            System.out.println("Error on line 79");
        }


        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream responseBody = connection.getInputStream();
            String output = new String(responseBody.readAllBytes(), StandardCharsets.UTF_8);
            authToken = new Gson().fromJson(output, LoginResult.class).authToken();
            username = words[1];
        } else {
            // SERVER RETURNED AN HTTP ERROR
            InputStream responseBody = connection.getErrorStream();
            String output = new String(responseBody.readAllBytes(), StandardCharsets.UTF_8);
            throw new BadInputException(new Gson().fromJson(output, MessageResponse.class).message());
        }
    }

    private void logout(PrintStream out) throws BadInputException {
        URL url;
        HttpURLConnection connection;
        try {
            url = new URL("http://localhost:8080/session");
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e1){
            System.out.println("You mistyped the url on line 121");
            return;
        }

        connection.setReadTimeout(5000);
        try {
            connection.setRequestMethod("DELETE");
            connection.setDoOutput(true);
        } catch (ProtocolException e1){
            System.out.println("You used the wrong HTTP method on line 130");
        }

        connection.addRequestProperty("Authorization", authToken);

        try {
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                authToken = "";
                // Read response body from InputStream ...
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

    private void create(PrintStream out, String command) throws BadInputException {
        String[] words = command.split(" ");
        if (words.length > 2){
            errorDisplay(out);
            out.println("you had " + (words.length - 1) + " arguments when you should have had 1");
            throw new BadInputException("wrong number of arguments");
        } else if (words.length < 2){
            errorDisplay(out);
            out.println("you didn't include a game name");
            throw new BadInputException("wrong number of arguments");
        }

        URL url;
        HttpURLConnection connection;
        try {
            url = new URL("http://localhost:8080/game");
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e1){
            System.out.println("You mistyped the url on line 121");
            return;
        }

        connection.setReadTimeout(5000);
        try {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
        } catch (ProtocolException e1){
            System.out.println("You used the wrong HTTP method on line 130");
        }

        connection.addRequestProperty("Authorization", authToken);

        try(OutputStream requestBody = connection.getOutputStream();) {
            CreateGameRequest req = new CreateGameRequest(words[1]);
            requestBody.write((new Gson().toJson(req)).getBytes());
        } catch (IOException e) {
            System.out.println("Error on line 79");
        }

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

    private void list(PrintStream out) throws BadInputException {
        URL url;
        HttpURLConnection connection;
        try {
            url = new URL("http://localhost:8080/game");
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e1){
            System.out.println("You mistyped the url on line 121");
            return;
        }

        connection.setReadTimeout(5000);
        try {
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
        } catch (ProtocolException e1){
            System.out.println("You used the wrong HTTP method on line 130");
        }

        connection.addRequestProperty("Authorization", authToken);

        try {
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream responseBody = connection.getInputStream();
                String output = new String(responseBody.readAllBytes(), StandardCharsets.UTF_8);
                gameList = new Gson().fromJson(output, ListGamesResponse.class).games();
            } else {
                // SERVER RETURNED AN HTTP ERROR
                InputStream responseBody = connection.getErrorStream();
                String output = new String(responseBody.readAllBytes(), StandardCharsets.UTF_8);
                throw new BadInputException(new Gson().fromJson(output, MessageResponse.class).message());
            }
        } catch (IOException e1){
            throw new BadInputException(e1.getMessage());
        }

        displayList(out);
    }

    private void join(PrintStream out, String command) throws BadInputException {
        String[] words = command.split(" ");
        if (words.length > 3){
            errorDisplay(out);
            out.println("you had " + (words.length - 1) + " arguments when you should have had 1 or 2");
            throw new BadInputException("wrong number of arguments");
        }

        URL url;
        HttpURLConnection connection;
        try {
            url = new URL("http://localhost:8080/game");
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e1){
            System.out.println("You mistyped the url on line 348");
            return;
        }

        connection.setReadTimeout(5000);
        try {
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
        } catch (ProtocolException e1){
            System.out.println("You used the wrong HTTP method on line 357");
        }

        connection.addRequestProperty("Authorization", authToken);

        try(OutputStream requestBody = connection.getOutputStream();) {
            ChessGame.TeamColor color;
            if (words.length == 2){
                color = null;
            } else if (words[2].equalsIgnoreCase("black")){
                color = ChessGame.TeamColor.BLACK;
            } else if (words[2].equalsIgnoreCase("white")){
                color = ChessGame.TeamColor.WHITE;
            } else {
                throw new BadInputException("invalid color entered");
            }
            JoinGameRequest req = new JoinGameRequest(color, gameList.get((Integer.parseInt(words[1]) - 1)).gameID());
            requestBody.write((new Gson().toJson(req)).getBytes());
        } catch (IOException e) {
            System.out.println("Error about line 372");
        }

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
        TerminalBoard.displayStartBoards(out);
    }

    private void printPrompt(PrintStream out){
        out.print(SET_BG_COLOR_DARK_GREY);
        out.print(SET_TEXT_COLOR_WHITE);
        if (authToken.isEmpty()){
            out.print("[LOGGED OUT] >>> ");
        } else {
            out.print("[" + username + "] >>> ");
        }
        out.flush();
    }

    private static void errorDisplay(PrintStream out) {
        //TODO: make it so text is red and bold??
        out.print(SET_BG_COLOR_RED);
        out.print(SET_TEXT_COLOR_WHITE);
    }

    private void displayList(PrintStream out){
        for (int i = 0 ; i < gameList.size(); i++){
            out.println((i+1) + ": " + gameList.get(i).gameName());
        }
    }

}
