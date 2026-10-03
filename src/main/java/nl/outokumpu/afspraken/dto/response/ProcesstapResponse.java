package nl.outokumpu.afspraken.dto.response;

import nl.outokumpu.afspraken.enums.ProcesstapStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProcesstapResponse(
        UUID id,
        String naam,
        int volgorde,
        ProcesstapStatus status,
        LocalDate deadline,
        Instant gestartOp,
        Instant afgerondOp,
        GebruikerResponse verantwoordelijke
) {
}