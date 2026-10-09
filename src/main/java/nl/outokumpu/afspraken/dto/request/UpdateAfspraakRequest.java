package nl.outokumpu.afspraken.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import nl.outokumpu.afspraken.dto.data.CapaciteitswisselData;
import nl.outokumpu.afspraken.dto.data.OrderverplaatsingData;

import java.time.LocalDate;

public record UpdateAfspraakRequest(

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
        LocalDate ingangsdatum,

        @NotNull
        LocalDate deadline,

        @Valid
        CapaciteitswisselData capaciteitswissel,

        @Valid
        OrderverplaatsingData orderverplaatsing
) {
}