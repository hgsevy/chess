package ui;

import com.google.gson.Gson;
import model.UserData;
import model.requestsAndResults.LoginRequest;
import model.requestsAndResults.LoginResult;
import model.requestsAndResults.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ProtocolException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static ui.EscapeSequences.*;

public class TerminalMenus {
    private String authToken = "";
    public void runThis(String[] args){
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.println("Welcome to the chess terminal. Type \"help\" to get started");
        Scanner scanner = new Scanner(System.in);
        String line = "";

        do{
            if (authToken.isEmpty()) { // pre-login menu
                if (line.contains("register")) {
                    try {
                        authToken = register(out, line);
                    } catch (BadInputException | IOException e1) {
                        errorDisplay(out);
                        out.println(e1.getMessage());
                    }
                } else if (line.contains("login")) {
                    try {
                        authToken = login(out, line);
                    } catch (BadInputException | IOException e1) {
                        errorDisplay(out);
                        out.println("There was an exception thrown, try again");
                    }
                } else if (line.equals("help")) {
                    helpPreLoginDisplay(out);
                } else {
                    out.println("Unrecognised command");
                }
                out.print("[LOGGED OUT] >>> ");
            }
            else { // post login menu
               if (line.contains("help")){
                   helpPostLoginDisplay(out);
               }
               else if (line.contains("logout")){
                   logout(out);
               }
               else if (line.contains("create")){
                   create(out, line);
               }
               else if (line.contains("list")){
                   list(out);
               }
               else if (line.contains("join")){
                   join(out);
               }
               else if (line.contains("observe")){
                   observe(out);
               }
               out.print("[LOGGED IN] >>>");
            }
            out.flush();
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

    public static void helpPostLoginDisplay(PrintStream out){
        out.println("create <NAME>: create a game by entering the name");
        out.println("list: list all games");
        out.println("join <ID> WHITE|BLACK|<empty>: joins a created chess game");
        out.println("observe <ID>: observe a create chess game");
        out.println("logout");
        out.println("quit: exits the program");
        out.println("help: displays possible commands and explanations");
        out.println();
    }

    public static String register(PrintStream out, String command) throws IOException, BadInputException {
        String[] words = command.split(" ");
        if (words.length != 4){
            errorDisplay(out);
            throw new BadInputException("invalid input (line 57)");
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
            return new Gson().fromJson(output, LoginResult.class).authToken();
        } else {
            // SERVER RETURNED AN HTTP ERROR
            InputStream responseBody = connection.getErrorStream();
            String output = new String(responseBody.readAllBytes(), StandardCharsets.UTF_8);
            throw new BadInputException(new Gson().fromJson(output, MessageResponse.class).message());
        }
    }

    public static String login(PrintStream out, String command) throws IOException, BadInputException {
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
            throw new IOException("bad website");
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
            return new String(responseBody.readAllBytes(), StandardCharsets.UTF_8);
            // Read response body from InputStream ...
        } else {
            // SERVER RETURNED AN HTTP ERROR
            InputStream responseBody = connection.getErrorStream();
            String output = new String(responseBody.readAllBytes(), StandardCharsets.UTF_8);
            throw new BadInputException(new Gson().fromJson(output, MessageResponse.class).message());
        }
    }

    public static void errorDisplay(PrintStream out) {
        //TODO: make it so text is red and bold??
        out.print(SET_BG_COLOR_RED);
    }

    public void doGet(String urlString) throws IOException {
        URL url = new URL(urlString);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setReadTimeout(5000);
        connection.setRequestMethod("GET");

        // Set HTTP request headers, if necessary
        // connection.addRequestProperty("Accept", "text/html");
        // connection.addRequestProperty("Authorization", "fjaklc8sdfjklakl");

        connection.connect();

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            // Get HTTP response headers, if necessary
            // Map<String, List<String>> headers = connection.getHeaderFields();

            // OR

            //connection.getHeaderField("Content-Length");

            InputStream responseBody = connection.getInputStream();
            // Read and process response body from InputStream ...
        } else {
            // SERVER RETURNED AN HTTP ERROR

            InputStream responseBody = connection.getErrorStream();
            // Read and process error response body from InputStream ...
        }
    }

}
