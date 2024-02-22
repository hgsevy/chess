package dataAccess;

import model.UserData;

public interface UserDAO extends ParentDAO{
    public void clear();
    public void createUser(UserData User) throws DataAccessException;
    public UserData getUser(String username) throws DataAccessException;

}
