package nl.outokumpu.afspraken.exception;

public class AuthenticatieException
        extends RuntimeException {

    public AuthenticatieException(
            String message
    ) {
        super(message);
    }
}