package service;

import dataAccess.AuthDAO;
import dataAccess.UserDAO;
import spark.Request;

public class RegisterRequest extends Request {
    String username;
    String password;
    String email;

    public RegisterRequest(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
}
