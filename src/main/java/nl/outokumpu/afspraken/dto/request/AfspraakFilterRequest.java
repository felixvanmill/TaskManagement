package nl.outokumpu.afspraken.dto.request;

import nl.outokumpu.afspraken.enums.AfspraakStatus;
import nl.outokumpu.afspraken.enums.AfspraakType;

import java.time.LocalDate;
import java.util.UUID;

public record AfspraakFilterRequest(

        AfspraakType type,

        AfspraakStatus status,

        UUID verantwoordelijkeId,

        UUID afdelingId,

        String fabriek,

        LocalDate periodeVan,

        LocalDate periodeTot

) {
}