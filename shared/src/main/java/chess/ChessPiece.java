package chess;

import java.util.ArrayList;
//import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    //private variables
    private final ChessGame.TeamColor color;
    private final ChessPiece.PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        color = pieceColor;
        this.type = type;
    }

    // Copy constructor
    public ChessPiece(ChessPiece original) {
        color = original.color;
        type = original.type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        int row = myPosition.row();
        int col = myPosition.col();

        Collection<ChessMove> answer = new HashSet<>();

        switch (type){
            case KING:
                if (row > 1){ // can move up
                    if (board.isOpenForPiece(new ChessPosition(row-1, col), color)) {
                        answer.add(new ChessMove(myPosition, new ChessPosition(row-1, col), null));
                    }
                    if (col > 1 && board.isOpenForPiece(new ChessPosition(row-1, col-1), color)) { // can move up-left diagonal
                        answer.add(new ChessMove(myPosition, new ChessPosition(row-1, col-1), null));
                    }
                    if (col < 8 && board.isOpenForPiece(new ChessPosition(row-1, col+1), color)) { //can move up-right diagonal
                        answer.add(new ChessMove(myPosition, new ChessPosition(row-1, col + 1), null));
                    }
                }

                if (row < 8){ // can move down
                    if (board.isOpenForPiece(new ChessPosition(row+1, col), color)) {
                        answer.add(new ChessMove(myPosition, new ChessPosition(row+1, col), null));
                    }
                    if (col > 1 && board.isOpenForPiece(new ChessPosition(row+1, col-1), color)) { // can move down-left diagonal
                        answer.add(new ChessMove(myPosition, new ChessPosition(row+1, col-1), null));
                    }
                    if (col < 8 && board.isOpenForPiece(new ChessPosition(row+1, col+1), color)) { //can move down-right diagonal
                        answer.add(new ChessMove(myPosition, new ChessPosition(row+1, col + 1), null));
                    }
                }

                if (col > 1 && board.isOpenForPiece(new ChessPosition(row, col-1), color)) { // can move left
                    answer.add(new ChessMove(myPosition, new ChessPosition(row, col-1), null));
                }
                if (col < 8 && board.isOpenForPiece(new ChessPosition(row, col+1), color)) { //can move right diagonal
                    answer.add(new ChessMove(myPosition, new ChessPosition(row, col + 1), null));
                }

                break;
            case QUEEN:
                // check row left
                answer.addAll(checkSquareMoves(board, myPosition));
                // check diagonals by not breaking before Bishop case
            case BISHOP:
                answer.addAll(checkDiagonalMoves(board, myPosition));
                break;
            case KNIGHT:
                // check one at a time like king
                // 2 up 1 left
                if (row > 2 && col > 1 && board.isOpenForPiece(new ChessPosition(row-2, col-1), color)) { // can move left
                    answer.add(new ChessMove(myPosition, new ChessPosition(row-2, col-1), null));
                }
                // 2 up 1 right
                if (row > 2 && col < 8 && board.isOpenForPiece(new ChessPosition(row-2, col+1), color)) { // can move left
                    answer.add(new ChessMove(myPosition, new ChessPosition(row-2, col+1), null));
                }
                // 2 right 1 up
                if (row > 1 && col < 7 && board.isOpenForPiece(new ChessPosition(row-1, col+2), color)) { // can move left
                    answer.add(new ChessMove(myPosition, new ChessPosition(row-1, col+2), null));
                }
                // 2 right 1 down
                if (row < 8 && col < 7 && board.isOpenForPiece(new ChessPosition(row+1, col+2), color)) { // can move left
                    answer.add(new ChessMove(myPosition, new ChessPosition(row+1, col+2), null));
                }
                // 2 left 1 up
                if (row > 1 && col > 2 && board.isOpenForPiece(new ChessPosition(row-1, col-2), color)) { // can move left
                    answer.add(new ChessMove(myPosition, new ChessPosition(row-1, col-2), null));
                }
                // 2 left 1 down
                if (row < 8 && col > 2 && board.isOpenForPiece(new ChessPosition(row+1, col-2), color)) { // can move left
                    answer.add(new ChessMove(myPosition, new ChessPosition(row+1, col-2), null));
                }
                // 2 down 1 left
                if (row < 7 && col > 1 && board.isOpenForPiece(new ChessPosition(row+2, col-1), color)) { // can move left
                    answer.add(new ChessMove(myPosition, new ChessPosition(row+2, col-1), null));
                }
                // 2 down 1 right
                if (row < 7 && col < 8 && board.isOpenForPiece(new ChessPosition(row+2, col+1), color)) { // can move left
                    answer.add(new ChessMove(myPosition, new ChessPosition(row+2, col+1), null));
                }
                break;
            case ROOK:
                answer.addAll(checkSquareMoves(board, myPosition));
                break;

            case PAWN:
                // black can move down and white can move up??
                ChessGame.TeamColor attackColor;
                int direction;
                int finalRow;
                int startRow;
                if (color == ChessGame.TeamColor.BLACK){
                    attackColor = ChessGame.TeamColor.WHITE;
                    direction = -1;
                    finalRow = 1;
                    startRow = 7;
                }
                else {
                    attackColor = ChessGame.TeamColor.BLACK;
                    direction = 1;
                    finalRow = 8;
                    startRow = 2;
                }

                if (row == finalRow-direction) { // check if piece can be promoted and add all promotion options
                    if (board.getPiece(new ChessPosition(finalRow, col)) == null) {
                        answer.add(new ChessMove(myPosition, new ChessPosition(finalRow, col), PieceType.QUEEN));
                        answer.add(new ChessMove(myPosition, new ChessPosition(finalRow, col), PieceType.BISHOP));
                        answer.add(new ChessMove(myPosition, new ChessPosition(finalRow, col), PieceType.KNIGHT));
                        answer.add(new ChessMove(myPosition, new ChessPosition(finalRow, col), PieceType.ROOK));
                    }
                    if (col > 1 && board.getPiece(new ChessPosition(finalRow, col - 1)) != null && board.getPiece(new ChessPosition(finalRow, col - 1)).color == attackColor) { // attack left
                        answer.add(new ChessMove(myPosition, new ChessPosition(finalRow, col - 1), PieceType.QUEEN));
                        answer.add(new ChessMove(myPosition, new ChessPosition(finalRow, col - 1), PieceType.BISHOP));
                        answer.add(new ChessMove(myPosition, new ChessPosition(finalRow, col - 1), PieceType.KNIGHT));
                        answer.add(new ChessMove(myPosition, new ChessPosition(finalRow, col - 1), PieceType.ROOK));
                    }
                    if (col < 8 && board.getPiece(new ChessPosition(finalRow, col + 1)) != null && board.getPiece(new ChessPosition(finalRow, col + 1)).color == attackColor) { // attack right
                        answer.add(new ChessMove(myPosition, new ChessPosition(finalRow, col + 1), PieceType.QUEEN));
                        answer.add(new ChessMove(myPosition, new ChessPosition(finalRow, col + 1), PieceType.BISHOP));
                        answer.add(new ChessMove(myPosition, new ChessPosition(finalRow, col + 1), PieceType.KNIGHT));
                        answer.add(new ChessMove(myPosition, new ChessPosition(finalRow, col + 1), PieceType.ROOK));
                    }
                } else {
                    if (board.getPiece(new ChessPosition(row+direction, col)) == null) {// move one
                        answer.add(new ChessMove(myPosition, new ChessPosition(row+direction, col), null));
                        // move two if add start position
                        if (row == startRow && board.getPiece(new ChessPosition(row+2*direction, col)) == null) {
                            answer.add(new ChessMove(myPosition, new ChessPosition(row+2*direction, col), null));
                        }
                    }
                    if (col > 1 && board.getPiece(new ChessPosition(row+direction, col - 1)) != null && board.getPiece(new ChessPosition(row+direction, col - 1)).color == attackColor) { // attack left
                        answer.add(new ChessMove(myPosition, new ChessPosition(row+direction, col - 1), null));
                    }
                    if (col < 8 && board.getPiece(new ChessPosition(row+direction, col + 1)) != null && board.getPiece(new ChessPosition(row+direction, col + 1)).color == attackColor) { // attack left
                        answer.add(new ChessMove(myPosition, new ChessPosition(row+direction, col + 1), null));
                    }
                }
                break;
        }
        return answer;

    }

    /**functions to clean up Queen code (because just combination of Bishop and Rook)**/
    private Collection<ChessMove> checkSquareMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> answer = new HashSet<>();

        //check row left
        answer.addAll(checkLine(board, myPosition, 0, -1));
        //check row right
        answer.addAll(checkLine(board, myPosition, 0, 1));
        //check column up
        answer.addAll(checkLine(board, myPosition, -1, 0));
        //check column down
        answer.addAll(checkLine(board, myPosition, 1, 0));

        return answer;
    }

    private Collection<ChessMove> checkDiagonalMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> answer = new HashSet<>();

        //check up left
        answer.addAll(checkLine(board, myPosition, -1, -1));
        //check up right
        answer.addAll(checkLine(board, myPosition, -1, 1));
        //check down left
        answer.addAll(checkLine(board, myPosition, 1, -1));
        //check down right
        answer.addAll(checkLine(board, myPosition, 1, 1));

        return answer;
    }

    /**
    * Function to check to possible moves along a line
     * @param rowDirection if moving up = 1, if moving down = -1, if constant = 0
     * @param colDirection same as rowDirection, but for columns/left and right
    * */
    private Collection<ChessMove> checkLine(ChessBoard board, ChessPosition myPosition, int rowDirection, int colDirection){
        int i = myPosition.row() + rowDirection;
        int j = myPosition.col() + colDirection;

        Collection<ChessMove> answer = new HashSet<>();

        while (i >= 1 && i <= 8 && j >= 1 && j <= 8){
            if (board.isOpenForPiece(new ChessPosition(i, j), color)){
                answer.add(new ChessMove(myPosition, new ChessPosition(i, j), null));
                if (board.getPiece(new ChessPosition(i, j)) != null){
                    //i = 10; // is this needed??
                    break;
                } // breaks out of while loop
                i += rowDirection;
                j += colDirection;
            }
            else {break;}
        }
        return answer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return color == that.color && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type);
    }

    public String toString() {
        String str = "";
        switch(type){
            case KING -> str = "k";
            case QUEEN -> str = "q";
            case BISHOP -> str = "b";
            case KNIGHT -> str = "n";
            case ROOK -> str = "r";
            case PAWN -> str = "p";
        }
        if (color == ChessGame.TeamColor.WHITE){
            str = str.toUpperCase();
        }
        return str;
    }
}
