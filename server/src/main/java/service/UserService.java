package service;

import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import dataAccess.UserDAO;
import model.UserData;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import service.exceptions.BadInputException;
import service.exceptions.NoCanDoException;
import service.exceptions.UnauthorizedException;
import model.requestsAndResults.LoginRequest;
import model.requestsAndResults.LoginResult;

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
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        try {
            UserData user = userData.getUser(enteredData.username());
            if (!encoder.matches(enteredData.password(), user.password())){
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
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode(newUser.password());
        UserData hashedUser = new UserData(newUser.username(), hashedPassword, newUser.email());
        try {
            userData.createUser(hashedUser);
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
