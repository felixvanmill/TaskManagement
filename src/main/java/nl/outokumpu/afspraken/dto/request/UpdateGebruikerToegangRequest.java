package nl.outokumpu.afspraken.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import nl.outokumpu.afspraken.enums.GebruikersRol;

public record UpdateGebruikerToegangRequest(

        @NotNull
        GebruikersRol rol,

        @NotNull
        Boolean actief,

        /*
         * Optioneel.
         * Alleen invullen wanneer een nieuw lokaal
         * wachtwoord ingesteld moet worden.
         */
        @Size(min = 12, max = 100)
        String nieuwWachtwoord

) {
}