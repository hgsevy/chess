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
    private boolean whiteKingMoved;
    private boolean whiteLeftRookMoved;
    private boolean whiteRightRookMoved;
    private boolean blackKingMoved;
    private boolean blackLeftRookMoved;
    private boolean blackRightRookMoved;


    public ChessGame() {
        board = new ChessBoard();
        turn = TeamColor.WHITE;
        whiteKingMoved = false;
        whiteLeftRookMoved = false;
        whiteRightRookMoved = false;
        blackKingMoved = false;
        blackLeftRookMoved = false;
        blackRightRookMoved = false;
    }

    // copy constructor
    public ChessGame(ChessGame original) {
        board = new ChessBoard(original.board);
        turn = original.turn;
        whiteKingMoved = original.whiteKingMoved;
        whiteLeftRookMoved = original.whiteLeftRookMoved;
        whiteRightRookMoved = original.whiteRightRookMoved;
        blackKingMoved = original.blackKingMoved;
        blackLeftRookMoved = original.blackLeftRookMoved;
        blackRightRookMoved = original.blackRightRookMoved;
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
        moves.addAll(checkCastleMove(startPosition));
        return moves;
    }

    private Collection<ChessMove> checkCastleMove(ChessPosition position){
        Collection<ChessMove> moves = new ArrayList<>();
        boolean validMove;
        if (position.equals(new ChessPosition(8, 5)) && !blackKingMoved){
            if (!blackLeftRookMoved){
                // check that all spaces between are blank
                validMove = true;
                for (int i = 2; i < 5; i++){
                    if (board.getPiece(new ChessPosition(8, i)) != null){
                        validMove = false;
                    }
                }
                if (validMove) {
                    // check to make sure no one is in check
                    ChessGame trialGame = new ChessGame(this);
                    trialGame.makeMoveToCheckMove(new ChessMove(new ChessPosition(8, 5), new ChessPosition(8, 3), null));
                    trialGame.makeMoveToCheckMove(new ChessMove(new ChessPosition(8, 1), new ChessPosition(8, 4), null));
                    if (!trialGame.isInCheck(TeamColor.BLACK)){
                        Collection<ChessMove> opposingMoves = trialGame.allOtherTeamMoves(TeamColor.WHITE);
                        for(ChessMove move : opposingMoves){
                            if (move.end().equals(new ChessPosition(8,3))){
                                validMove = false;
                            }
                        }
                        if (validMove){
                            moves.add(new ChessMove(new ChessPosition(8, 5), new ChessPosition(8, 3), null));
                        }
                    }

                }
            }
            if (!blackRightRookMoved){
                // check to make sure no one is in check
                validMove = true;
                for (int i = 6; i < 7; i++){
                    if (board.getPiece(new ChessPosition(8, i)) != null){
                        validMove = false;
                    }
                }
                if (validMove) {
                    // check to make sure no one is in check
                    ChessGame trialGame = new ChessGame(this);
                    trialGame.makeMoveToCheckMove(new ChessMove(new ChessPosition(8, 5), new ChessPosition(8, 7), null));
                    trialGame.makeMoveToCheckMove(new ChessMove(new ChessPosition(8, 8), new ChessPosition(8, 6), null));
                    if (!trialGame.isInCheck(TeamColor.BLACK)){
                        Collection<ChessMove> opposingMoves = trialGame.allOtherTeamMoves(TeamColor.WHITE);
                        for(ChessMove move : opposingMoves){
                            if (move.end().equals(new ChessPosition(8,6))){
                                validMove = false;
                            }
                        }
                        if (validMove){
                            moves.add(new ChessMove(new ChessPosition(8, 5), new ChessPosition(8, 7), null));
                        }
                    }

                }
            }
        } else if (position.equals(new ChessPosition(1, 5)) && !whiteKingMoved){
            if (!whiteLeftRookMoved){
                // check that all spaces between are blank
                validMove = true;
                for (int i = 2; i < 5; i++){
                    if (board.getPiece(new ChessPosition(1, i)) != null){
                        validMove = false;
                    }
                }
                if (validMove) {
                    // check to make sure no one is in check
                    ChessGame trialGame = new ChessGame(this);
                    trialGame.makeMoveToCheckMove(new ChessMove(new ChessPosition(1, 5), new ChessPosition(1, 3), null));
                    trialGame.makeMoveToCheckMove(new ChessMove(new ChessPosition(1, 1), new ChessPosition(1, 4), null));
                    if (!trialGame.isInCheck(TeamColor.WHITE)){
                        Collection<ChessMove> opposingMoves = trialGame.allOtherTeamMoves(TeamColor.BLACK);
                        for(ChessMove move : opposingMoves){
                            if (move.end().equals(new ChessPosition(1,3))){
                                validMove = false;
                            }
                        }
                        if (validMove){
                            moves.add(new ChessMove(new ChessPosition(1, 5), new ChessPosition(1, 3), null));
                        }
                    }

                }
            }
            if (!whiteRightRookMoved){
                // check to make sure no one is in check
                validMove = true;
                for (int i = 6; i <= 7; i++){
                    if (board.getPiece(new ChessPosition(1, i)) != null){
                        validMove = false;
                    }
                }
                if (validMove) {
                    // check to make sure no one is in check
                    ChessGame trialGame = new ChessGame(this);
                    trialGame.makeMoveToCheckMove(new ChessMove(new ChessPosition(1, 5), new ChessPosition(1, 7), null));
                    trialGame.makeMoveToCheckMove(new ChessMove(new ChessPosition(1, 8), new ChessPosition(1, 6), null));
                    if (!trialGame.isInCheck(TeamColor.WHITE)){
                        Collection<ChessMove> opposingMoves = trialGame.allOtherTeamMoves(TeamColor.BLACK);
                        for(ChessMove move : opposingMoves){
                            if (move.end().equals(new ChessPosition(1,6))){
                                validMove = false;
                            }
                        }
                        if (validMove){
                            moves.add(new ChessMove(new ChessPosition(1, 5), new ChessPosition(1, 7), null));
                        }
                    }

                }
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
                if (board.getPiece(move.start()).getPieceType() == ChessPiece.PieceType.KING && (move.start().col() - move.end().col() == 2) || move.start().col() - move.end().col() == -2){
                    makeCastleMove(move);
                } else {
                    updateBools(board.getPiece(move.start()), move.start());
                    makeMoveToCheckMove(move);
                }
                switchTeamTurn();
                return;
            }
        }
        throw new InvalidMoveException();
    }

    private void updateBools(ChessPiece piece, ChessPosition start){
        if (start.row() == 8 && piece.getTeamColor() == TeamColor.BLACK){
            if (piece.getPieceType() == ChessPiece.PieceType.KING){
                blackKingMoved = true;
            }
            else if (piece.getPieceType() == ChessPiece.PieceType.ROOK){
                if (start.col() == 1) {
                    blackLeftRookMoved = true;
                }
                else if (start.col() == 8){
                    blackRightRookMoved = true;
                }
            }
        }
        else if (start.row() == 1 && piece.getTeamColor() == TeamColor.WHITE){
            if (piece.getPieceType() == ChessPiece.PieceType.KING){
                whiteKingMoved = true;
            }
            else if (piece.getPieceType() == ChessPiece.PieceType.ROOK){
                if (start.col() == 1) {
                    whiteLeftRookMoved = true;
                }
                else if (start.col() == 8){
                    whiteRightRookMoved = true;
                }
            }
        }
    }

    private void makeCastleMove(ChessMove move){
        if(move.start().row() == 8){
            if (move.end().col() == 3){
                board.addPiece(move.end(), board.getPiece(move.start()));
                board.addPiece(new ChessPosition(8, 4), board.getPiece(new ChessPosition(8,1)));
                board.addPiece(move.start(), null);
                board.addPiece(new ChessPosition(8,1), null);
                blackLeftRookMoved = true;
            } else {
                board.addPiece(move.end(), board.getPiece(move.start()));
                board.addPiece(new ChessPosition(8, 6), board.getPiece(new ChessPosition(8,8)));
                board.addPiece(move.start(), null);
                board.addPiece(new ChessPosition(8,8), null);
                blackRightRookMoved = true;
            }
            blackKingMoved = true;
        } else {
            if (move.end().col() == 3){
                board.addPiece(move.end(), board.getPiece(move.start()));
                board.addPiece(new ChessPosition(1, 4), board.getPiece(new ChessPosition(1,1)));
                board.addPiece(move.start(), null);
                board.addPiece(new ChessPosition(1,1), null);
                whiteLeftRookMoved = true;
            } else {
                board.addPiece(move.end(), board.getPiece(move.start()));
                board.addPiece(new ChessPosition(1, 6), board.getPiece(new ChessPosition(1,8)));
                board.addPiece(move.start(), null);
                board.addPiece(new ChessPosition(1,8), null);
                whiteRightRookMoved = true;
            }
            whiteKingMoved = true;
        }
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
        Collection<ChessMove> moves = allValidTeamMoves(teamColor);
        return moves.isEmpty() && isInCheck(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        Collection<ChessMove> moves = allValidTeamMoves(teamColor);
        return moves.isEmpty() && !isInCheck(teamColor);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;

        whiteKingMoved = false;
        whiteLeftRookMoved = false;
        whiteRightRookMoved = false;
        blackKingMoved = false;
        blackLeftRookMoved = false;
        blackRightRookMoved = false;
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
