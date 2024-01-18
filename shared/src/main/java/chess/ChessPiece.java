package chess;

import java.util.ArrayList;
import java.util.Collection;
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
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        int i; // error codes about redeclaration in switch are freaking me out, so doing this way
        int j;

        Collection<ChessMove> answer = new ArrayList<>();

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
                i = col-1;
                while (i >= 1){
                    if (board.isOpenForPiece(new ChessPosition(row, i), color)){
                        answer.add(new ChessMove(myPosition, new ChessPosition(row, i), null));
                        i--;
                    }
                    else {i = 0;} // breaks out of while loop
                }
                // check row right
                i = col+1;
                while (i <= 8){
                    if (board.isOpenForPiece(new ChessPosition(row, i), color)){
                        answer.add(new ChessMove(myPosition, new ChessPosition(row, i), null));
                        i++;
                    }
                    else {i = 9;} // breaks out of while loop
                }
                // check col up
                i = row - 1;
                while (i >= 1){
                    if (board.isOpenForPiece(new ChessPosition(i, col), color)){
                        answer.add(new ChessMove(myPosition, new ChessPosition(i, col), null));
                        i--;
                    }
                    else {i = 0;} // breaks out of while loop
                }
                // check col down
                i = col+1;
                while (i <= 8){
                    if (board.isOpenForPiece(new ChessPosition(i, col), color)){
                        answer.add(new ChessMove(myPosition, new ChessPosition(i, col), null));
                        i++;
                    }
                    else {i = 8;} // breaks out of while loop
                }
                // check diagonals by not breaking before Bishop case
            case BISHOP:
                // check up-left diagonal
                i = row - 1;
                j = col - 1;
                while (i >= 1 && j >= 1){
                    if (board.isOpenForPiece(new ChessPosition(i, j), color)){
                        answer.add(new ChessMove(myPosition, new ChessPosition(i, j), null));
                        i--;
                        j--;
                    }
                    else{i = 0;}
                }
                // check up-right diagonal
                i = row - 1;
                j = col + 1;
                while (i >= 1 && j <= 8){
                    if (board.isOpenForPiece(new ChessPosition(i, j), color)){
                        answer.add(new ChessMove(myPosition, new ChessPosition(i, j), null));
                        i--;
                        j++;
                    }
                    else{i = 0;}
                }
                // check down-left diagonal
                i = row + 1;
                j = col - 1;
                while (i <= 8 && j >= 1){
                    if (board.isOpenForPiece(new ChessPosition(i, j), color)){
                        answer.add(new ChessMove(myPosition, new ChessPosition(i, j), null));
                        i++;
                        j--;
                    }
                    else{i = 9;}
                }
                // check down-right diagonal
                i = row + 1;
                j = col + 1;
                while (i <= 8 && j <= 8){
                    if (board.isOpenForPiece(new ChessPosition(i, j), color)){
                        answer.add(new ChessMove(myPosition, new ChessPosition(i, j), null));
                        i++;
                        j++;
                    }
                    else{i = 9;}
                }
                break;
            case KNIGHT:// TODO
                // check one at a time like king??
                throw new RuntimeException("Not implemented");
            case ROOK:
                // check row left
                // check row left
                i = col - 1;
                while (i >= 1){
                    if (board.isOpenForPiece(new ChessPosition(row, i), color)){
                        answer.add(new ChessMove(myPosition, new ChessPosition(row, i), null));
                        i--;
                    }
                    else {i = 0;} // breaks out of while loop
                }
                // check row right
                i = col+1;
                while (i <= 8){
                    if (board.isOpenForPiece(new ChessPosition(row, i), color)){
                        answer.add(new ChessMove(myPosition, new ChessPosition(row, i), null));
                        i++;
                    }
                    else {i = 9;} // breaks out of while loop
                }
                // check col up
                i = row - 1;
                while (i >= 1){
                    if (board.isOpenForPiece(new ChessPosition(i, col), color)){
                        answer.add(new ChessMove(myPosition, new ChessPosition(i, col), null));
                        i--;
                    }
                    else {i = 0;} // breaks out of while loop
                }
                // check col down
                i = col + 1;
                while (i <= 8){
                    if (board.isOpenForPiece(new ChessPosition(i, col), color)){
                        answer.add(new ChessMove(myPosition, new ChessPosition(i, col), null));
                        i++;
                    }
                    else {i = 8;} // breaks out of while loop
                }

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
                    if (col > 1 && board.getPiece(new ChessPosition(finalRow, col - 1)).color == attackColor) { // attack left
                        answer.add(new ChessMove(myPosition, new ChessPosition(finalRow, col - 1), PieceType.QUEEN));
                        answer.add(new ChessMove(myPosition, new ChessPosition(finalRow, col - 1), PieceType.BISHOP));
                        answer.add(new ChessMove(myPosition, new ChessPosition(finalRow, col - 1), PieceType.KNIGHT));
                        answer.add(new ChessMove(myPosition, new ChessPosition(finalRow, col - 1), PieceType.ROOK));
                    }
                    if (col < 8 && board.getPiece(new ChessPosition(finalRow, col + 1)).color == attackColor) { // attack right
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
                    if (col > 1 && board.getPiece(new ChessPosition(row+direction, col - 1)).color == attackColor) { // attack left
                        answer.add(new ChessMove(myPosition, new ChessPosition(row+direction, col - 1), null));
                    }
                    if (col < 8 && board.getPiece(new ChessPosition(row+direction, col + 1)).color == attackColor) { // attack left
                        answer.add(new ChessMove(myPosition, new ChessPosition(row - 1, col + 1), null));
                    }
                }
                break;
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
}
