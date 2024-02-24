package dataAccess;

import model.UserData;

public interface UserDAO extends ParentDAO{
    void createUser(UserData User) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;

}
