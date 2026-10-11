package nl.outokumpu.afspraken.dto.response;

import java.time.Instant;
import java.util.UUID;

public record OpmerkingResponse(
        UUID id,
        GebruikerResponse auteur,
        String tekst,
        Instant aangemaaktOp
) {
}