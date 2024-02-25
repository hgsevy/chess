package service.exceptions;

import dataAccess.DataAccessException;

/**
 * When the user requests to do something that is not allowed
 */
public class NoCanDoException extends ServiceException{
    public NoCanDoException() {
        super("Error: already taken", 403);
    }
}
