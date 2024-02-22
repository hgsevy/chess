package dataAccess;

import model.AuthData;
import model.UserData;

import java.util.HashSet;

public class MemoryUserDAO implements UserDAO {
    HashSet<UserData> database;
    MemoryUserDAO(){
        database = new HashSet<>();
    }

    public void clear() {
        database = new HashSet<>();
    }

    public void createUser(UserData user) throws DataAccessException {
        for(UserData person: database){
            if (person.email().equals(user.email())){
                throw new DataAccessException("email already used by another user");
            }
            if (person.username().equals(user.username())){
                throw new DataAccessException("username already used by another user");
            }
        }
        database.add(user);
    }

    public UserData getUser(String username) throws DataAccessException {
        for(UserData person: database){
            if (person.username().equals(username)){
                return person;
            }
        }
        throw new DataAccessException("username does not exist");
    }
}
