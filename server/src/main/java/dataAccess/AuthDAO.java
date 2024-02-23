package dataAccess;

import model.AuthData;
import model.UserData;

public interface AuthDAO extends ParentDAO{
    public String createAuthToken(String username);
    public void getAuth(String token) throws DataAccessException;
    public String getUsername(String token) throws DataAccessException;
    public void deleteAuthToken(String token);


}
