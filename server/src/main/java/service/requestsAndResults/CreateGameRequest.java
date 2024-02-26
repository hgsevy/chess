package service.requestsAndResults;

public record CreateGameRequest(String authToken, String gameName) {
}
