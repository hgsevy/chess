package service.exceptions;

import dataAccess.DataAccessException;

/**
 * Indicates the user does not have authorization for their action
 * Used for bad auth keys or incorrect password
 */
public class UnauthorizedException extends ServiceException {
    public UnauthorizedException() {
        super("Error: unauthorized", 401);
    }
}