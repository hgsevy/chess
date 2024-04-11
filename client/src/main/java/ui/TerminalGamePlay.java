package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import clientAPI.WSClient;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.RESET_TEXT_BOLD_FAINT;

public class TerminalGamePlay {
    private final ChessGame.TeamColor color;
    private ChessGame game;
    private final PrintStream out;

    private WSClient ws;

    public TerminalGamePlay(PrintStream out, ChessGame.TeamColor color, WSClient ws){
        this.out = out;
        this.color = color;
        this.ws = ws;
    }

    public void runThis(){
        out.print(ERASE_SCREEN);
        out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + SET_TEXT_BOLD);
        out.println("Welcome to the game"); //TODO: can make it say game name?
        out.print(SET_BG_COLOR_DARK_GREY + RESET_TEXT_BOLD_FAINT);
        Scanner scanner = new Scanner(System.in);
        String line = "help";
        do{
            if (line.contains("help")) {
                helpDisplay();
            } else if (line.equals("redraw")) {
                TerminalBoard.displayBoard(out, game.getBoard().getArray(), color != null && color == ChessGame.TeamColor.BLACK, null);
            } else if (line.contains("highlight")) {
                try {
                    highlight(line);
                } catch (BadInputException e1) {
                    errorDisplay(out);
                    out.println(e1.getMessage());
                }
            } else if (color != null && (game == null || !game.isOver())) { // not an observer
                if (line.contains("move")) {
                    try {
                        makeMove(line);
                    } catch (BadInputException | InvalidMoveException e1) {
                        errorDisplay(out);
                        out.println(e1.getMessage());
                    }
                } else if (line.contains("resign")) {
                    resign();
                }
            } else {
                errorDisplay(out);
                out.println("Unrecognised command");
            }

            printPrompt(out);
            line = scanner.nextLine();
        } while (!line.equals("leave"));
        try{
            ws.leave();
        } catch (Exception e1){
            out.print("Trouble removing websocket connection");
        }
    }

    private void helpDisplay(){
        helpHelper(out, "help", new String[]{}, "displays possible commands and explanations");
        helpHelper(out, "redraw", new String[]{}, "redraws the chess board");
        helpHelper(out, "leave", new String[]{}, "exits gameplay and returns to post-login menu");
        if (color!=null && (game==null||!game.isOver())){
            helpHelper(out, "move", new String[]{"START COL (letter)", "START ROW (num)", "END COL (letter)", "END ROW (num)"}, "moves the piece at the given start position to the given end position if valid");
            helpHelper(out, "resign", new String[]{}, "forfeits the game and exists gameplay");
        }
        helpHelper(out, "highlight", new String[]{"COL (letter)", "ROW (num)"}, "draws the board with all possible end positions for the piece at the given start position highlighted");

        out.println();
    }

    //These are the functions used by the notification helper
    public void displayError(String error){
        out.println();
        errorDisplay(out);
        out.print(error);
        printPrompt(out);
    }

    public void displayNotification(String notification){
        out.println();
        out.print(notification);
        printPrompt(out);
    }

    public void loadGame(ChessGame game){
        out.println();
        this.game = game;
        TerminalBoard.displayBoard(out, game.getBoard().getArray(), color!=null && color == ChessGame.TeamColor.BLACK,null);
        printPrompt(out);
    }

    private void highlight(String line) throws BadInputException {
        String[] words = line.split(" ");
        if (words.length != 3){
            throw new BadInputException("invalid entry (hint: need space between row and col)");
        }
        try{
            if(Integer.parseInt(words[2]) > 8 || Integer.parseInt(words[2]) < 1){
                throw new BadInputException("invalid row input");
            }
        } catch (NumberFormatException e1){
            throw new BadInputException("invalid row input");
        }
        Collection<ChessMove> moves = game.validMoves(new ChessPosition(Integer.parseInt(words[2]), convertLetterToCol(words[1])));
        Collection<ChessPosition> ends = new HashSet<>();
        if (moves != null) {
            for (ChessMove move : moves) {
                ends.add(move.end());
            }
        }
        TerminalBoard.displayBoard(out, game.getBoard().getArray(), color!=null && color == ChessGame.TeamColor.BLACK, ends);
    }

    private void makeMove(String line) throws BadInputException, InvalidMoveException {
        String[] words = line.split(" ");
        if (words.length != 5){
            throw new BadInputException("invalid entry (hint: need space between row and col)");
        }
        try{
            if(Integer.parseInt(words[2]) > 8 || Integer.parseInt(words[2]) < 1){
                throw new BadInputException("invalid row input for start");
            }
            if(Integer.parseInt(words[4]) > 8 || Integer.parseInt(words[4]) < 1){
                throw new BadInputException("invalid row input for end");
            }
        } catch (NumberFormatException e1){
            throw new BadInputException("invalid row input");
        }
        ChessMove move = new ChessMove(new ChessPosition(Integer.parseInt(words[2]), convertLetterToCol(words[1])), new ChessPosition(Integer.parseInt(words[4]), convertLetterToCol(words[3])), null);
        try {
            ws.makeMove(move);
        } catch (Exception e1){
            System.out.print("exception line 126");
        }
    }

    private void resign(){
        out.print("Are you sure you want to resign? Type \"Yes\" if you wish to continue");
        printPrompt(out);
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        if (line.equals("Yes")) {
            try {
                ws.resign();
            } catch (Exception e1) {
                System.out.print("exception line 139");
            }
        }
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

    private int convertLetterToCol(String letter) throws BadInputException {
        if (letter.equalsIgnoreCase("a")){
            return 1;
        } else if (letter.equalsIgnoreCase("b")){
            return 2;
        } else if (letter.equalsIgnoreCase("c")){
            return 3;
        } else if (letter.equalsIgnoreCase("d")){
            return 4;
        } else if (letter.equalsIgnoreCase("e")){
            return 5;
        } else if (letter.equalsIgnoreCase("f")){
            return 6;
        } else if (letter.equalsIgnoreCase("g")){
            return 7;
        } else if (letter.equalsIgnoreCase("h")){
            return 8;
        }
        throw new BadInputException("invalid col entry");
    }

    private static void errorDisplay(PrintStream out) {
        out.print(SET_BG_COLOR_RED);
        out.print(SET_TEXT_COLOR_WHITE);
    }

    private void printPrompt(PrintStream out){
        out.print(SET_BG_COLOR_DARK_GREY);
        out.print(SET_TEXT_COLOR_WHITE);
        out.print(" >>> ");
        out.flush();
    }

}
