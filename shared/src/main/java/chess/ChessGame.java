package chess;

import java.util.ArrayList;
import java.util.Collection;

import static java.util.Collections.addAll;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */

public class ChessGame {

    private ChessBoard board;
    private TeamColor turn;

    public ChessGame() {
        board = new ChessBoard();
        turn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        turn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /** TODO: see if this needs to check for check
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        if (board.getPiece(startPosition) == null){
            return null;
        }
        return board.getPiece(startPosition).pieceMoves(board, startPosition);
    }

    /** TODO
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        TeamColor opposing;
        if (teamColor == TeamColor.BLACK){
            opposing = TeamColor.WHITE;
        }
        else {opposing = TeamColor.BLACK;}

        Collection<ChessMove> opposingMoves = allTeamMoves(opposing);
        ChessPosition kingPosition = getKingPosition(teamColor);
        for (ChessMove move : opposingMoves){
            if (move.end().equals(kingPosition)){
                return true;
            }
        }
        return false;
    }

    private Collection<ChessMove> allTeamMoves(TeamColor teamColor){
        Collection<ChessMove> allTheMoves = new ArrayList<>();
        for (int i = 1; i <= 8; i++){
            for (int j = 1; j <= 8; j++){
                if (board.getPiece(new ChessPosition(i,j)).getTeamColor() == teamColor){
                    Collection<ChessMove> someMoves = board.getPiece(new ChessPosition(i,j)).pieceMoves(board, new ChessPosition(i,j));
                    allTheMoves.addAll(someMoves);
                }
            }
        }
        return allTheMoves;
    }

    private ChessPosition getKingPosition(TeamColor color){
        ChessPiece kingToFind = new ChessPiece(color, ChessPiece.PieceType.KING);
        for (int i = 1; i <= 8; i++){
            for (int j = 1; j <= 8; j++){
                if (board.getPiece(new ChessPosition(i,j)).equals(kingToFind)){
                    return new ChessPosition(i,j);
                }
            }
        }
        return null;
    }

    /** TODO
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /** TODO
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
