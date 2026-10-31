package nl.outokumpu.afspraken.exception;

/**
 * Signals an authentication failure that the REST exception handler reports as HTTP 401.
 */
public class AuthenticatieException extends RuntimeException {

    public AuthenticatieException(String message) {
        super(message);
    }
}