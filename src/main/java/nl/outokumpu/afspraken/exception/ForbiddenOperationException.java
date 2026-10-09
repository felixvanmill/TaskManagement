package nl.outokumpu.afspraken.exception;

public class ForbiddenOperationException
        extends IllegalArgumentException {

    public ForbiddenOperationException(
            String message
    ) {
        super(message);
    }
}