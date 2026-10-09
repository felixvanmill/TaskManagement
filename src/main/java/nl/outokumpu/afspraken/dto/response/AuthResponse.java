package nl.outokumpu.afspraken.dto.response;

import nl.outokumpu.afspraken.enums.GebruikersRol;

import java.util.UUID;

public record AuthResponse(
        UUID id,
        String naam,
        String email,
        GebruikersRol rol,
        AfdelingResponse afdeling
) {
}