package service;

import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import dataAccess.UserDAO;
import model.UserData;
import spark.Request;
import spark.Response;

public class UserService {

    UserDAO userData;
    AuthDAO authData;

    public UserService(UserDAO userData, AuthDAO authData){
        this.userData = userData;
        this.authData = authData;
    }
    public LoginResult login(LoginRequest enteredData) throws DataAccessException, UnauthorizedException {
        UserData user = userData.getUser(enteredData.username());
        if (!user.password().equals(enteredData.password())){
            throw new UnauthorizedException("incorrect password");
        }
        return new LoginResult(enteredData.username(), authData.createAuthToken(enteredData.username()));
    }

    public LoginResult register(UserData newUser) throws DataAccessException{
        userData.createUser(newUser);
        return new LoginResult(newUser.username(), authData.createAuthToken(newUser.username()));
    }

    public void logout(String token) throws DataAccessException{
        authData.deleteAuthToken(token);
    }
}
