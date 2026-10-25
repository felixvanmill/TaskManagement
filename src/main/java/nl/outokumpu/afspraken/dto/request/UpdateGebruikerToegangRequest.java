package nl.outokumpu.afspraken.dto.request;

import jakarta.validation.constraints.NotNull;
import nl.outokumpu.afspraken.enums.GebruikersRol;

public record UpdateGebruikerToegangRequest(

        @NotNull
        GebruikersRol rol,

        @NotNull
        Boolean actief

) {
}