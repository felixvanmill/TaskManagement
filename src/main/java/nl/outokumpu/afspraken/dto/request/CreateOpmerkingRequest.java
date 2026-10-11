package nl.outokumpu.afspraken.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOpmerkingRequest(

        @NotBlank(
                message = "Opmerking mag niet leeg zijn"
        )
        @Size(
                max = 5000,
                message = "Opmerking mag maximaal 5000 tekens bevatten"
        )
        String tekst

) {
}