package service.exceptions;

import dataAccess.DataAccessException;
import service.exceptions.ServiceException;

/**
 * Indicates the user does not have authorization for their action
 * Used for bad auth keys or incorrect password
 */
public class BadInputException extends ServiceException {
    public BadInputException() {
        super("Error: bad request", 400);
    }
}
