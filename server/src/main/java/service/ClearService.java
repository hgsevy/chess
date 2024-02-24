package service;

import dataAccess.AuthDAO;
import dataAccess.GameDAO;
import dataAccess.UserDAO;

public class ClearService {

    UserDAO userData;
    AuthDAO authData;
    GameDAO gameData;

    public ClearService(UserDAO userData, AuthDAO authData, GameDAO gameData){
        this.userData = userData;
        this.authData = authData;
        this.gameData = gameData;
    }
    public void clear(){
        userData.clear();
        authData.clear();
        gameData.clear();

    }
}
