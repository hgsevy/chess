package dataAccess;

import model.UserData;

public class SQLUserDAO implements UserDAO{

    private DatabaseManager database;

    public SQLUserDAO(){
        database = new DatabaseManager();
    }

    @Override
    public void clear() {

    }

    @Override
    public void createUser(UserData user) throws DataAccessException {

    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }
}