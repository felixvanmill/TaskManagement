package nl.outokumpu.afspraken.dto.response;

import nl.outokumpu.afspraken.enums.AfspraakStatus;
import nl.outokumpu.afspraken.enums.AfspraakType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AfspraakSummaryResponse(
        UUID id,
        String titel,
        AfspraakType type,
        AfspraakStatus status,
        LocalDate ingangsdatum,
        LocalDate deadline,
        GebruikerResponse initiatiefnemer,
        Instant laatstGewijzigdOp
) {
}