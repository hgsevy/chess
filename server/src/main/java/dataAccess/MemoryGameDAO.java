package dataAccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;

public class MemoryGameDAO implements GameDAO{

    ArrayList<GameData> database;
    int gameNumber;

    public MemoryGameDAO(){
        database = new ArrayList<>();
        gameNumber = 1;
    }

    public int createGame(String name) {
        database.add(new GameData(gameNumber,null, null, name, new ChessGame()));
        return gameNumber++;
    }

    public void joinGame(String username, int gameID, ChessGame.TeamColor color) throws DataAccessException {
        for(int i = 0; i < database.size(); i++){
            if(database.get(i).gameID() == gameID){
                if (color == null){
                    return; // this is where we would create a list of non-participating watchers
                }
                else if(color == ChessGame.TeamColor.BLACK){
                    if (database.get(i).blackUsername() != null && !database.get(i).blackUsername().equals(username)){
                        throw new DataAccessException("spot already taken");
                    }
                    GameData oldData = database.get(i);
                    database.remove(i);
                    database.add(new GameData(oldData.gameID(), oldData.whiteUsername(), username, oldData.gameName(), oldData.game()));
                    return;
                }
                else if(color == ChessGame.TeamColor.WHITE ) {
                    if (database.get(i).whiteUsername() != null && !database.get(i).whiteUsername().equals(username)){
                        throw new DataAccessException("spot already taken");
                    }
                    GameData oldData = database.get(i);
                    database.remove(i);
                    database.add(new GameData(oldData.gameID(), username, oldData.blackUsername(), oldData.gameName(), oldData.game()));
                    return;
                }
                throw new DataAccessException("color does not exist");
            }
        }
        throw new DataAccessException("game does not exist");

    }

    public ArrayList<GameData> listGames() {
        return database;
    }

    public void updateGame(int gameID, ChessGame game) throws DataAccessException{
        for(int i = 0; i < database.size(); i++){
            if(database.get(i).gameID() == gameID){
                GameData oldData = database.get(i);
                database.remove(i);
                database.add(new GameData(oldData.gameID(), oldData.whiteUsername(), oldData.blackUsername(), oldData.gameName(), game));
                return;
            }
        }
        throw new DataAccessException("game does not exist");
    }

    public void clear() {
        database.clear();
    }
}
