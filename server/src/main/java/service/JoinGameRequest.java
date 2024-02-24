package service;

import chess.ChessGame;
import dataAccess.AuthDAO;
import dataAccess.GameDAO;
import spark.Request;

public record JoinGameRequest (String token, ChessGame.TeamColor color, int gameID) {
}
