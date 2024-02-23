package dataAccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

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

    public void joinGame(String username, int gameID, ChessGame.TeamColor color) throws DataAccessException { //FIXME: HELP WHY ERRORS
        GameData dataToRemove;
        GameData newData;
        for(int i = 0; i < database.size(); i++){
            if(database.get(i).gameID() == gameID){
                if(color == ChessGame.TeamColor.BLACK){
                    if (database.get(i).blackUsername() != null){
                        throw new DataAccessException("spot already taken");
                    }
                    GameData oldData = database.get(i);
                    database.remove(i);
                    database.add(new GameData(oldData.gameID(), oldData.whiteUsername(), username, oldData.gameName(), oldData.game()));
                    return;
                }
                else {
                    if (database.get(i).whiteUsername() != null){
                        throw new DataAccessException("spot already taken");
                    }
                    GameData oldData = database.get(i);
                    database.remove(i);
                    database.add(new GameData(oldData.gameID(), username, oldData.blackUsername(), oldData.gameName(), oldData.game()));
                    return;
                }
            }
        }
        throw new DataAccessException("game does not exist");

    }

    public Collection<GameData> listGames() {
        return database;
    }

    public void updateGame(int gameID, ChessGame game) throws DataAccessException{
        GameData dataToRemove;
        GameData newData;
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
