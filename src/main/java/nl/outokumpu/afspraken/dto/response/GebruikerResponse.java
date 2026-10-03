package nl.outokumpu.afspraken.dto.response;

import java.util.UUID;

public record GebruikerResponse(
        UUID id,
        String naam,
        String email,
        AfdelingResponse afdeling
) {
}