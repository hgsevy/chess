package chess;

import java.util.ArrayList;
import java.util.Collection;

//import static java.util.Collections.addAll;

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

    // copy constructor
    public ChessGame(ChessGame original) {
        board = new ChessBoard(original.board);
        turn = original.turn;
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

    private void switchTeamTurn(){
        if (turn == TeamColor.BLACK){
            turn = TeamColor.WHITE;
        } else {turn = TeamColor.BLACK;}
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
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
        TeamColor team = board.getPiece(startPosition).getTeamColor();
        Collection<ChessMove> moves = new ArrayList<>();
        Collection<ChessMove> possibleMoves = board.getPiece(startPosition).pieceMoves(board, startPosition);
        for (ChessMove move : possibleMoves){
            ChessGame trialGame = new ChessGame(this);
            trialGame.makeMoveToCheckMove(move);
            if(!trialGame.isInCheck(team)){
                moves.add(move);
            }

        }
        return moves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (board.getPiece(move.start()) == null){
            throw new InvalidMoveException();
        }
        if (board.getPiece(move.start()).getTeamColor() != turn){
            throw new InvalidMoveException();
        }
        Collection<ChessMove> possibilities = validMoves(move.start());
        for (ChessMove possibleMove : possibilities){
            if (possibleMove.equals(move)){
                makeMoveToCheckMove(move);
                switchTeamTurn();
                return;
            }
        }
        throw new InvalidMoveException();
    }

    private void makeMoveToCheckMove(ChessMove move) {
        if (move.promotion() != null){
            board.addPiece(move.end(), new ChessPiece(board.getPiece(move.start()).getTeamColor(), move.promotion()));
        } else {
            board.addPiece(move.end(), board.getPiece(move.start()));
        }
        board.addPiece(move.start(), null);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        if(getKingPosition(teamColor) == null){
            return false;
        }
        TeamColor opposing;
        if (teamColor == TeamColor.BLACK){
            opposing = TeamColor.WHITE;
        }
        else {opposing = TeamColor.BLACK;}

        Collection<ChessMove> opposingMoves = allOtherTeamMoves(opposing);
        ChessPosition kingPosition = getKingPosition(teamColor);
        for (ChessMove move : opposingMoves){
            if (move.end().equals(kingPosition)){
                return true;
            }
        }
        return false;
    }

    private Collection<ChessMove> allOtherTeamMoves(TeamColor teamColor){
        Collection<ChessMove> allTheMoves = new ArrayList<>();
        for (int i = 1; i <= 8; i++){
            for (int j = 1; j <= 8; j++){
                if (board.getPiece(new ChessPosition(i,j)) != null && board.getPiece(new ChessPosition(i,j)).getTeamColor() == teamColor){
                    Collection<ChessMove> someMoves = board.getPiece(new ChessPosition(i,j)).pieceMoves(board, new ChessPosition(i,j));
                    allTheMoves.addAll(someMoves);
                }
            }
        }
        return allTheMoves;
    }

    private Collection<ChessMove> allValidTeamMoves(TeamColor teamColor){
        Collection<ChessMove> allTheMoves = new ArrayList<>();
        for (int i = 1; i <= 8; i++){
            for (int j = 1; j <= 8; j++){
                if (board.getPiece(new ChessPosition(i,j)) != null && board.getPiece(new ChessPosition(i,j)).getTeamColor() == teamColor){
                    Collection<ChessMove> someMoves = validMoves(new ChessPosition(i, j));
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
                if (board.getPiece(new ChessPosition(i,j)) != null && board.getPiece(new ChessPosition(i,j)).equals(kingToFind)){
                    return new ChessPosition(i,j);
                }
            }
        }
        return null;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        /*
        if(getKingPosition(teamColor) == null){
            return false;
        }
        */
        Collection<ChessMove> moves = allValidTeamMoves(teamColor);
        return moves.isEmpty();
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return isInCheckmate(teamColor) && turn == teamColor;
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
