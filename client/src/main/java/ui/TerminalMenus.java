package ui;

import chess.ChessGame;
import clientAPI.ServerFacade;
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

    private String username = "";
    private String authToken = ""; //FIXME: remove once server facade fixed
    private ArrayList<GameData> gameList;

    private ServerFacade server;


    public TerminalMenus(int num){
        server = new ServerFacade(num);
    }
    public void runThis(){

        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.print(ERASE_SCREEN);
        out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE);
        out.println("Welcome to the chess terminal. Possible commands are listed below");
        out.print(SET_BG_COLOR_DARK_GREY);
        Scanner scanner = new Scanner(System.in);
        String line = "help";

        do{
            if (!server.isLoggedIn()) { // pre-login menu
                if (line.contains("register")) {
                    try {
                        register(out, line);
                    } catch (BadInputException e1) {
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
        helpHelper(out, "register", new String[]{"USERNAME", "PASSWORD", "EMAIL"}, "create an account by entering username, password, and email");
        helpHelper(out, "login", new String[]{"USERNAME", "PASSWORD"}, "play chess after entering a valid username and password");
        helpHelper(out, "quit", new String[]{}, "exits the program");
        helpHelper(out, "help", new String[]{}, "displays possible commands and explanations");
        out.println();
    }

    private static void helpPostLoginDisplay(PrintStream out){
        helpHelper(out, "create", new String[]{"NAME"}, "create a game by entering the name");
        helpHelper(out, "list", new String[]{}, "list all games");
        helpHelper(out, "join", new String[]{"GAME NUMBER", "WHITE|BLACK|<empty>"}, "joins a created game");
        helpHelper(out, "observe", new String[]{"GAME NUMBER"}, "observe a created game");
        helpHelper(out, "logout", new String[]{}, "go back to pre-login menu");
        helpHelper(out, "quit", new String[]{}, "exits the program");
        helpHelper(out, "help", new String[]{}, "displays possible commands and explanations");
        out.println();
    }

    private static void helpHelper(PrintStream out, String command, String[] arguments, String description){
        out.print(SET_TEXT_BOLD + SET_TEXT_COLOR_BLUE);
        out.print(command);
        out.print(RESET_TEXT_BOLD_FAINT);
        for (String argument:arguments){
            out.print(" <");
            out.print(argument);
            out.print(">");
        }
        out.print(": ");
        out.print(SET_TEXT_COLOR_WHITE);
        out.print(description);
        out.println();
    }

    private void register(PrintStream out, String command) throws BadInputException {
        String[] words = command.split(" ");
        if (words.length != 4){
            errorDisplay(out);
            throw new BadInputException("wrong number of arguments");
        }

        server.register(words[1], words[2], words[3]);
        username = words[1];
    }

    private void login(PrintStream out, String command) throws IOException, BadInputException {
        String[] words = command.split(" ");
        if (words.length != 3){
            errorDisplay(out);
            out.println("you had " + (words.length - 1) + " arguments when you should have had 2");
            throw new BadInputException("wrong number of arguments");
        }

        server.login(words[1], words[2]);
        username = words[1];
    }

    private void logout(PrintStream out) throws BadInputException {
        server.logout();
        username = null;
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

        server.create(words[1]);
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
        if (!server.isLoggedIn()){
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
