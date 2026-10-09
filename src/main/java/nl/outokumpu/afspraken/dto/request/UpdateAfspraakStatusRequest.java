package nl.outokumpu.afspraken.dto.request;

import jakarta.validation.constraints.NotNull;

import nl.outokumpu.afspraken.enums.AfspraakStatus;

public record UpdateAfspraakStatusRequest(

        @NotNull
        AfspraakStatus status

) {
}