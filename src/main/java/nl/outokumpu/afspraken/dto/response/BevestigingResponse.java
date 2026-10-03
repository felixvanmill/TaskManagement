package nl.outokumpu.afspraken.dto.response;

import nl.outokumpu.afspraken.enums.Beslissing;

import java.time.Instant;
import java.util.UUID;

public record BevestigingResponse(
        UUID id,
        GebruikerResponse gebruiker,
        boolean gezien,
        Instant gezienOp,
        Beslissing beslissing,
        Instant beslistOp
) {
}