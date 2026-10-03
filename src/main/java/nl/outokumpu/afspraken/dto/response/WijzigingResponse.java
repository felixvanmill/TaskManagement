package nl.outokumpu.afspraken.dto.response;

import java.time.Instant;
import java.util.UUID;

public record WijzigingResponse(
        UUID id,
        GebruikerResponse gewijzigdDoor,
        Instant gewijzigdOp,
        String onderdeel,
        String oudeWaarde,
        String nieuweWaarde
) {
}