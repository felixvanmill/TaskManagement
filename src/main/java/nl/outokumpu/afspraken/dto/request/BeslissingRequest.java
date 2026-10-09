package nl.outokumpu.afspraken.dto.request;

import jakarta.validation.constraints.NotNull;

import nl.outokumpu.afspraken.enums.Beslissing;

public record BeslissingRequest(

        @NotNull
        Beslissing beslissing

) {
}