package service;

import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import dataAccess.UserDAO;
import model.UserData;
import service.exceptions.BadInputException;
import service.exceptions.NoCanDoException;
import service.exceptions.UnauthorizedException;
import service.requestsAndResults.LoginRequest;
import service.requestsAndResults.LoginResult;

public class UserService {

    UserDAO userData;
    AuthDAO authData;

    public UserService(UserDAO userData, AuthDAO authData){
        this.userData = userData;
        this.authData = authData;
    }
    public LoginResult login(LoginRequest enteredData) throws UnauthorizedException, BadInputException {
        if (enteredData.username() == null || enteredData.password() == null){
            throw new BadInputException();
        }
        try {
            UserData user = userData.getUser(enteredData.username());
            if (!user.password().equals(enteredData.password())){
                throw new UnauthorizedException();
            }
        } catch (DataAccessException exp1) {
            throw new UnauthorizedException();
        }
        return new LoginResult(enteredData.username(), authData.createAuthToken(enteredData.username()));
    }

    public LoginResult register(UserData newUser) throws NoCanDoException, BadInputException {
        if (newUser.username() == null || newUser.password() == null || newUser.email() == null){
            throw new BadInputException();
        }
        try {
            userData.createUser(newUser);
        } catch (DataAccessException expt1){
            throw new NoCanDoException();
        }
        return new LoginResult(newUser.username(), authData.createAuthToken(newUser.username()));
    }

    public void logout(String token) throws UnauthorizedException{
        try {
            authData.deleteAuthToken(token);
        } catch (DataAccessException expt1){
            throw new UnauthorizedException();
        }
    }
}
