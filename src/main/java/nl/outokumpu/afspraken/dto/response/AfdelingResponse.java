package nl.outokumpu.afspraken.dto.response;

import java.util.UUID;

public record AfdelingResponse(
        UUID id,
        String naam
) {
}