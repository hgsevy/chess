package chess;

import java.util.Collection;
import java.util.HashSet;

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
    private ChessMove lastMove;


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

    /**
     * Copy Constructor
     */
    public ChessGame(ChessGame original) {
        board = new ChessBoard(original.board);
        turn = original.turn;
        whiteKingMoved = original.whiteKingMoved;
        whiteLeftRookMoved = original.whiteLeftRookMoved;
        whiteRightRookMoved = original.whiteRightRookMoved;
        blackKingMoved = original.blackKingMoved;
        blackLeftRookMoved = original.blackLeftRookMoved;
        blackRightRookMoved = original.blackRightRookMoved;
        lastMove = original.lastMove;
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
     * Used at the end of a move to change whose turn it is
     */
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
        Collection<ChessMove> moves = new HashSet<>();
        Collection<ChessMove> possibleMoves = board.getPiece(startPosition).pieceMoves(board, startPosition);
        for (ChessMove move : possibleMoves){
            ChessGame trialGame = new ChessGame(this);
            trialGame.makeMoveToCheckMove(move);
            if(!trialGame.isInCheck(team)){
                moves.add(move);
            }

        }
        moves.addAll(checkCastleMove(startPosition));
        moves.addAll(checkEnPassantMove(startPosition));

        return moves;
    }

    /* FOLLOWING TWO METHODS ARE TO CHECK OT SEE IF ANY SPECIAL MOVES APPLY AND CAN BE MADE */

    /**
     * If the entered position is that of a king, checks to see if valid castle moves
     * @return all valid castling moves, indicated by a king moving two spaces
     */
    private Collection<ChessMove> checkCastleMove(ChessPosition position){
        Collection<ChessMove> moves = new HashSet<>();
        if (board.getPiece(position).getTeamColor() == TeamColor.WHITE && !whiteKingMoved && !isInCheck(TeamColor.WHITE)) {
            if (!whiteLeftRookMoved) {
                ChessMove move = castleHelper(TeamColor.WHITE, true);
                if (move != null) {
                    moves.add(move);
                }
            }
            if (!whiteRightRookMoved){
                ChessMove move = castleHelper(TeamColor.WHITE, false);
                if (move != null) {
                    moves.add(move);
                }
            }
        }
        else if (board.getPiece(position).getTeamColor() == TeamColor.BLACK && !blackKingMoved && !isInCheck(TeamColor.BLACK)) {
            if (!blackLeftRookMoved) {
                ChessMove move = castleHelper(TeamColor.BLACK, true);
                if (move != null) {
                    moves.add(move);
                }
            }
            if (!blackRightRookMoved){
                ChessMove move = castleHelper(TeamColor.BLACK, false);
                if (move != null) {
                    moves.add(move);
                }
            }
        }
        return moves;

    }

    private ChessMove castleHelper(TeamColor color, boolean rookIsLeft){
        // set up
        int row = 1;
        int rook = 8;
        int kingToRookDirection = 1;
        TeamColor opposing = TeamColor.BLACK;
        if (color == TeamColor.BLACK){
            row = 8;
            opposing = TeamColor.WHITE;
        }
        if (rookIsLeft){
            rook = 1;
            kingToRookDirection = -1;
        }

        // see if all spots between are blank
        int i = 5+kingToRookDirection;
        while (i < 8 && i > 1){
            if (board.getPiece(new ChessPosition(row, i)) != null){
                return null;
            }
            i+=kingToRookDirection;
        }
        // check to make sure no one is in check
        ChessGame trialGame = new ChessGame(this);
        trialGame.makeMoveToCheckMove(new ChessMove(new ChessPosition(row, 5), new ChessPosition(row, 5+(2*kingToRookDirection)), null));
        trialGame.makeMoveToCheckMove(new ChessMove(new ChessPosition(row, rook), new ChessPosition(row, 5+kingToRookDirection), null));
        if (!trialGame.isInCheck(color)){
            Collection<ChessMove> opposingMoves = trialGame.allOtherTeamMoves(opposing);
            for(ChessMove move : opposingMoves){
                if (move.end().equals(new ChessPosition(row,5+kingToRookDirection))){
                    return null;
                }
            }
            return new ChessMove(new ChessPosition(row, 5), new ChessPosition(row, 5+(2*kingToRookDirection)), null);

        }
        return null;
    }

    /**
     *If the entered position is that of a pawn, see if allowed to capture a pawn that last moved two spots
     * @return all valid en passant moves, indicated by a pawn moving diagonally to an empty space.
     */
    private Collection<ChessMove> checkEnPassantMove(ChessPosition position){
        Collection<ChessMove> moves =  new HashSet<>();
        if (lastMove != null && board.getPiece(position).getPieceType() == ChessPiece.PieceType.PAWN && // if you are moving a pawn
                board.getPiece(position).getTeamColor() == turn && // it is your turn (makes sure you don't capture own pawn)
                board.getPiece(lastMove.end()).getPieceType() == ChessPiece.PieceType.PAWN && // the last piece moves was a pawn
                ((lastMove.end().row() == 4 && lastMove.start().row() == 2) || (lastMove.end().row() == 5 && lastMove.start().row() == 7)) && // the lst move was moving the pawn two spaces
                position.row() == lastMove.end().row()) // this pawn end in the correct row to capture by en passant
        {
            int direction;
            if (turn == TeamColor.BLACK){
                direction = -1;
            } else {direction = 1;}

            if (position.col() - lastMove.end().col() == 1){
                moves.add(new ChessMove(position, new ChessPosition(position.row() + direction, position.col()-1), null));
            }
            else if (position.col() - lastMove.end().col() == -1){
                moves.add(new ChessMove(position, new ChessPosition(position.row() + direction, position.col()+1), null));
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
                }
                else if (board.getPiece(move.start()).getPieceType() == ChessPiece.PieceType.PAWN && move.start().col() != move.end().col() && board.getPiece(move.end()) == null){
                    makeMoveToCheckMove(move);
                    board.addPiece(new ChessPosition(move.start().row(), move.end().col()), null);
                }
                else {
                    updateBools(board.getPiece(move.start()), move.start());
                    makeMoveToCheckMove(move);
                }
                switchTeamTurn();
                lastMove = move;
                return;
            }
        }
        throw new InvalidMoveException();
    }

    /**
     * Used in make move to change the booleans used to see if castling moves are valid
     * @param piece what just moved
     * @param start where the piece was, used to know which rook moved
     */
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

    /**
     * Used in trial games to know if moves put king in check without seeing if move is valid
     * (because we don't know what is valid when the function is used)
     */
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

    /**@return all valid moves for a given team*/
    private Collection<ChessMove> allValidTeamMoves(TeamColor teamColor){
        Collection<ChessMove> allTheMoves = new HashSet<>();
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

    /**Similar to all team moves, but doesn't check to see if moves are valid*/
    private Collection<ChessMove> allOtherTeamMoves(TeamColor teamColor){
        Collection<ChessMove> allTheMoves = new HashSet<>();
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

    /**
     * Used when checking for check
     * @return ChessPosition for given team's king
     */
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
     * no valid moves and is not in check
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
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
