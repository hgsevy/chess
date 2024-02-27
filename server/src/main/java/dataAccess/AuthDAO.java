package dataAccess;

public interface AuthDAO extends ParentDAO{
    String createAuthToken(String username);
    String getUsername(String token) throws DataAccessException;
    void deleteAuthToken(String token) throws DataAccessException;


}
