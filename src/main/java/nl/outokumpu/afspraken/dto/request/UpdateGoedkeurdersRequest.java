package nl.outokumpu.afspraken.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record UpdateGoedkeurdersRequest(

        @NotNull
        List<UUID> goedkeurderIds

) {
}