package dataAccess;

import java.util.ArrayList;
import java.util.UUID;

import model.AuthData;

public class MemoryAuthDAO  implements AuthDAO{

    ArrayList<AuthData> database;

    public MemoryAuthDAO(){
        database = new ArrayList<>();
    }

    public String createAuthToken(String username) {
        String token = UUID.randomUUID().toString();
        database.add(new AuthData(token, username));
        return token;
    }

    public void getAuth(String token) throws DataAccessException{
        for(AuthData user : database){
            if (user.authToken().equals(token)){
                return;
            }
        }
        throw new DataAccessException("auth token does not exist");
    }

    public String getUsername(String token) throws DataAccessException{
        for(AuthData user : database){
            if (user.authToken().equals(token)){
                return user.username();
            }
        }
        throw new DataAccessException("auth token does not exist");
    }

    public void deleteAuthToken(String token) throws DataAccessException{
        for (int i = 0; i < database.size(); i++){
            if (database.get(i).authToken().equals(token)){
                database.remove(i);
                return;
            }
        }
        throw new DataAccessException("auth Token never existed");

    }

    public void clear() {
        database.clear();
    }
}
