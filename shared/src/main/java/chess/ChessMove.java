package chess;

//import java.util.Objects;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */

public record ChessMove(ChessPosition start, ChessPosition end, ChessPiece.PieceType promotion){}

