package nl.outokumpu.afspraken.exception;

/**
 * Signals a business permission denial, reported as HTTP 403 rather than a validation error.
 */
public class ForbiddenOperationException extends IllegalArgumentException {

    public ForbiddenOperationException(String message) {
        super(message);
    }
}