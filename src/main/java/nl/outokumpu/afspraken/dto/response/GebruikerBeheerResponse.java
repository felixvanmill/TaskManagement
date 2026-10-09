package nl.outokumpu.afspraken.dto.response;

import nl.outokumpu.afspraken.enums.GebruikersRol;

import java.util.UUID;

public record GebruikerBeheerResponse(
        UUID id,
        String naam,
        String email,
        GebruikersRol rol,
        boolean actief,
        boolean lokaleToegang,
        AfdelingResponse afdeling
) {
}