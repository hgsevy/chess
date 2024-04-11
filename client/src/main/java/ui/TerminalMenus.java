package ui;

import chess.ChessGame;
import clientAPI.ServerFacade;
import clientAPI.WSClient;
import model.GameData;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.IOException;

import static ui.EscapeSequences.*;

public class TerminalMenus {

    private String username = "";
    private ArrayList<GameData> gameList;

    private ServerFacade server;


    public TerminalMenus(int num){
        server = new ServerFacade(num);
    }
    public void runThis(){

        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.print(ERASE_SCREEN);
        out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + SET_TEXT_BOLD);
        out.println("Welcome to the chess terminal. Possible commands are listed below");
        out.print(SET_BG_COLOR_DARK_GREY + RESET_TEXT_BOLD_FAINT);
        Scanner scanner = new Scanner(System.in);
        String line = "help";

        do{
            if (!server.isLoggedIn()) { // pre-login menu
                if (line.contains("register")) {
                    try {
                        register(line);
                    } catch (BadInputException e1) {
                        errorDisplay(out);
                        out.println(e1.getMessage());
                    }
                } else if (line.contains("login")) {
                    try {
                        login(line);
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
                       logout();
                   } catch (BadInputException e1){
                       errorDisplay(out);
                       out.println("There was an exception thrown, try again");
                   }
               }
               else if (line.contains("create")){
                   try{
                       create(line);
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

    private void register(String command) throws BadInputException {
        String[] words = command.split(" ");
        if (words.length != 4){
            throw new BadInputException("wrong number of arguments");
        }

        server.register(words[1], words[2], words[3]);
        username = words[1];
    }

    private void login(String command) throws IOException, BadInputException {
        String[] words = command.split(" ");
        if (words.length != 3){
            throw new BadInputException("you had " + (words.length - 1) + " arguments when you should have had 2");
        }

        server.login(words[1], words[2]);
        username = words[1];
    }

    private void logout() throws BadInputException {
        server.logout();
        username = null;
    }

    private void create(String command) throws BadInputException {
        String[] words = command.split(" ");
        if (words.length > 2){
            throw new BadInputException("you had " + (words.length - 1) + " arguments when you should have had 1");
        } else if (words.length < 2){
            throw new BadInputException("you didn't include a game name");
        }

        server.create(words[1]);
    }

    private void list(PrintStream out) throws BadInputException {
        gameList = server.list();
        displayList(out);
    }

    private void join(PrintStream out, String command) throws BadInputException {
        String[] words = command.split(" ");
        if (words.length > 3){
            throw new BadInputException("you had " + (words.length - 1) + " arguments when you should have had 1 or 2");
        }

        ChessGame.TeamColor color;
        if (words.length == 2) {
            color = null;
        } else if (words[2].equalsIgnoreCase("black")) {
            color = ChessGame.TeamColor.BLACK;
        } else if (words[2].equalsIgnoreCase("white")) {
            color = ChessGame.TeamColor.WHITE;
        } else {
            throw new BadInputException("invalid color entered");
        }

        int gameIndex;
        try{
            gameIndex = Integer.parseInt(words[1]) - 1;
            if (gameIndex >= gameList.size()){
                throw new BadInputException("game number entered is not valid");
            }
        } catch (NumberFormatException e1){
            throw new BadInputException("game number not entered");
        }

        WSClient ws = server.join(gameList.get(gameIndex).gameID(), color);
        TerminalGamePlay gamePlay = new TerminalGamePlay(out, color, ws);
        gamePlay.runThis();
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
        out.print(SET_BG_COLOR_RED);
        out.print(SET_TEXT_COLOR_WHITE);
    }

    private void displayList(PrintStream out){
        for (int i = 0 ; i < gameList.size(); i++){
            out.print(SET_TEXT_COLOR_BLUE);
            out.println((i+1) + ": " + gameList.get(i).gameName());
            out.print(SET_TEXT_COLOR_WHITE + RESET_TEXT_BOLD_FAINT);
            out.print("\tWhite player: ");
            if (gameList.get(i).whiteUsername() == null){
                out.print(SET_TEXT_COLOR_GREEN);
                out.println("OPEN TO JOIN");
                out.print(SET_TEXT_COLOR_WHITE);
            } else {
                out.println(gameList.get(i).whiteUsername());
            }
            out.print("\tBlack player: ");
            if (gameList.get(i).blackUsername() == null){
                out.print(SET_TEXT_COLOR_GREEN);
                out.println("OPEN TO JOIN");
            } else {
                out.println(gameList.get(i).blackUsername());
            }
        }
    }

}
