package nl.outokumpu.afspraken.dto.request;

import jakarta.validation.constraints.NotNull;

import nl.outokumpu.afspraken.enums.UitvoeringsStatus;

public record UpdateUitvoeringsstatusRequest(

        @NotNull
        UitvoeringsStatus status

) {
}