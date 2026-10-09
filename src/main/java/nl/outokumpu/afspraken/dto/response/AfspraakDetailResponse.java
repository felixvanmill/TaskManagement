package nl.outokumpu.afspraken.dto.response;

import nl.outokumpu.afspraken.dto.data.CapaciteitswisselData;
import nl.outokumpu.afspraken.dto.data.OrderverplaatsingData;
import nl.outokumpu.afspraken.enums.AfspraakStatus;
import nl.outokumpu.afspraken.enums.AfspraakType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AfspraakDetailResponse(
        UUID id,
        String titel,
        String beschrijving,
        String reden,
        String achtergrond,
        String aannames,
        AfspraakType type,
        AfspraakStatus status,
        LocalDate ingangsdatum,
        LocalDate deadline,
        Instant aangemaaktOp,
        Instant laatstGewijzigdOp,
        Instant afgerondOp,
        GebruikerResponse initiatiefnemer,
        List<AfdelingResponse> betrokkenAfdelingen,
        List<GebruikerResponse> betrokkenGebruikers,
        List<GebruikerResponse> goedkeurders,
        List<ProcesstapResponse> processtappen,
        List<BevestigingResponse> bevestigingen,
        List<WijzigingResponse> wijzigingen,
        CapaciteitswisselData capaciteitswissel,
        OrderverplaatsingData orderverplaatsing
) {
}