package dataAccess;

public interface AuthDAO extends ParentDAO{
    String createAuthToken(String username);
    void getAuth(String token) throws DataAccessException; //never used but leaving here in case I remember why
    String getUsername(String token) throws DataAccessException;
    void deleteAuthToken(String token) throws DataAccessException;


}
