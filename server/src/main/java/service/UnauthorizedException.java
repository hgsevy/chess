package service;

import dataAccess.DataAccessException;

/**
 * Indicates the user does not have authorization for their action
 * Used for bad auth keys or incorrect password
 */
public class UnauthorizedException extends DataAccessException {
    public UnauthorizedException(String message) {
        super(message);
    }
}