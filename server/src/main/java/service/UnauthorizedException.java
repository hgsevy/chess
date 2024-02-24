package service;

/**
 * Indicates the user does not have authorization for their action
 * Used for bad auth keys or incorrect password
 */
public class UnauthorizedException extends Exception{
    public UnauthorizedException(String message) {
        super(message);
    }
}