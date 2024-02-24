package service;

import dataAccess.AuthDAO;
import dataAccess.UserDAO;
import spark.Request;

public record LoginRequest(String username, String password) {
}
