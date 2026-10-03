package nl.outokumpu.afspraken.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import nl.outokumpu.afspraken.dto.data.CapaciteitswisselData;
import nl.outokumpu.afspraken.dto.data.OrderverplaatsingData;
import nl.outokumpu.afspraken.enums.AfspraakType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateAfspraakRequest(

        @NotBlank
        @Size(max = 255)
        String titel,

        @NotBlank
        @Size(max = 5000)
        String beschrijving,

        @NotBlank
        @Size(max = 2000)
        String reden,

        @Size(max = 5000)
        String achtergrond,

        @Size(max = 5000)
        String aannames,

        @NotNull
        AfspraakType type,

        @NotNull
        LocalDate ingangsdatum,

        @NotNull
        LocalDate deadline,

        @NotNull
        UUID verantwoordelijkeId,

        @NotBlank
        @Size(max = 255)
        String eersteProcesstapNaam,

        @NotNull
        LocalDate eersteProcesstapDeadline,

        @NotNull
        List<UUID> betrokkeneIds,

        @NotNull
        List<UUID> goedkeurderIds,

        @NotNull
        List<UUID> afdelingIds,

        @Valid
        CapaciteitswisselData capaciteitswissel,

        @Valid
        OrderverplaatsingData orderverplaatsing
) {
}