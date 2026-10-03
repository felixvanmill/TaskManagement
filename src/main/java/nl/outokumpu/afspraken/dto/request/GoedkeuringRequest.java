package nl.outokumpu.afspraken.dto.request;

import jakarta.validation.constraints.NotNull;

import nl.outokumpu.afspraken.enums.Beslissing;

public record GoedkeuringRequest(

        @NotNull
        Boolean gezien,

        @NotNull
        Beslissing beslissing
) {
}