package service.requestsAndResults;

import chess.ChessGame;

public record JoinGameRequest (String token, ChessGame.TeamColor color, int gameID) {
}
