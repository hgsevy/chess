package ui;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

import static ui.EscapeSequences.*;

public class TerminalBoard {

    private static final String[] horizontalLabels = {"a", "b", "c", "d", "e", "f", "g", "h"};
    private static final String[] verticalLabels = {"1", "2", "3", "4", "5", "6", "7", "8"};


    public static void main(String[] args){
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.print(ERASE_SCREEN);

        ChessGame board = new ChessGame();

        displayBoard(out, board.getBoard().getArray(), false, null);
        out.print(SET_BG_COLOR_BLACK);
        out.println();
        displayBoard(out, board.getBoard().getArray(), true, null);
    }

    public static void displayBoard(PrintStream out, ChessPiece[][] board, boolean isForBlack, Collection<ChessPosition> toHighlight){
        // draw header
        drawHorizontalBoarder(out, isForBlack);

        // draw board
        int i = 7;
        int j = 0;
        int direction = 1;
        if (isForBlack){
            i = 0;
            j = 7;
            direction = -1;
        }

        while (i < 8 && i >= 0){
            drawOutsideSquares(out, verticalLabels[i], 1);
            int jCopy = j;
            while (jCopy < 8 && jCopy >= 0){
                drawSquare(out, board[i][jCopy], (i+jCopy)%2==0, toHighlight!=null && toHighlight.contains(new ChessPosition(i+1, jCopy+1)));
                jCopy += direction;
            }
            drawOutsideSquares(out, verticalLabels[i], 0);
            out.print(SET_BG_COLOR_BLACK);
            out.println();
            i -= direction;
        }

        drawHorizontalBoarder(out, isForBlack);

    }

    private static void drawHorizontalBoarder(PrintStream out, boolean isForBlack){
        out.print(SET_TEXT_COLOR_BLACK);
        out.print(SET_BG_COLOR_LIGHT_GREY);

        out.print(EMPTY + SQUARE_SPACE.repeat(2));

        int i = 0;
        int direction = 1;
        if (isForBlack){
            direction = -1;
            i = 7;
        }
        while (i < 8 && i >= 0){
            drawOutsideSquares(out, horizontalLabels[i], 0);
            i+=direction;
        }

        out.print(EMPTY + SQUARE_SPACE.repeat(2));
        out.print(SET_BG_COLOR_BLACK);
        out.println();
    }

    private static void drawOutsideSquares(PrintStream out, String label, int leftSide){
        out.print(SET_TEXT_COLOR_BLACK);
        out.print(SET_BG_COLOR_LIGHT_GREY);
        out.print("\u200A".repeat(4+leftSide));
        out.print(label);
        out.print("\u200A".repeat(5-leftSide));
    }

    private static void drawSquare(PrintStream out, ChessPiece piece, boolean squareIsBlack, boolean highlight){
        if (squareIsBlack) {
            if (highlight){
                out.print(SET_BG_COLOR_DARK_GREEN);
            } else {
                out.print(SET_BG_COLOR_BLACK);
            }
        }
        else {
            if (highlight){
                out.print(SET_BG_COLOR_GREEN);
            } else {
                out.print(SET_BG_COLOR_WHITE);
            }
        }
        out.print(SQUARE_SPACE);
        if (piece == null){
            out.print(EMPTY);
        }
        else {
            out.print(getPiece(out, piece));
        }
        out.print(SQUARE_SPACE);
    }

    private static String getPiece(PrintStream out, ChessPiece piece) {
        ChessGame.TeamColor color = piece.getTeamColor();
        ChessPiece.PieceType type = piece.getPieceType();

        if (color == ChessGame.TeamColor.BLACK) {
            out.print(SET_TEXT_COLOR_BLUE);
        } else {
            out.print(SET_TEXT_COLOR_MAGENTA);
        }
        if (type == ChessPiece.PieceType.KING) {
            return BLACK_KING;
        } else if (type == ChessPiece.PieceType.QUEEN) {
            return BLACK_QUEEN;
        } else if (type == ChessPiece.PieceType.ROOK) {
            return BLACK_ROOK;
        } else if (type == ChessPiece.PieceType.KNIGHT) {
            return BLACK_KNIGHT;
        } else if (type == ChessPiece.PieceType.BISHOP) {
            return BLACK_BISHOP;
        } else {
            return BLACK_PAWN;
        }
    }

}
