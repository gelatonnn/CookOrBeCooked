package utils;

public class InvalidActionException extends RuntimeException {
    public InvalidActionException(String msg) {
        super(msg);
    }
}