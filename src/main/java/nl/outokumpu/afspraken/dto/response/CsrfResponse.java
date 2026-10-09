package nl.outokumpu.afspraken.dto.response;

public record CsrfResponse(
        String headerNaam,
        String parameterNaam,
        String token
) {
}