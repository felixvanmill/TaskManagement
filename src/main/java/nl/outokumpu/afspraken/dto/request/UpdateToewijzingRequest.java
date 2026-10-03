package nl.outokumpu.afspraken.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record UpdateToewijzingRequest(

        @NotNull
        List<UUID> betrokkeneIds,

        @NotNull
        List<UUID> afdelingIds,

        @NotNull
        UUID processtapId,

        @NotNull
        UUID verantwoordelijkeId
) {
}