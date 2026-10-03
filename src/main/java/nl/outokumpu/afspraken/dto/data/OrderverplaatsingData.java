package nl.outokumpu.afspraken.dto.data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

import nl.outokumpu.afspraken.enums.UitvoeringsStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderverplaatsingData(

        @NotBlank
        @Size(max = 100)
        String vanFabriek,

        @NotBlank
        @Size(max = 100)
        String naarFabriek,

        @NotBlank
        @Size(max = 100)
        String finishType,

        @NotNull
        BigDecimal totaalVolumeTons,

        @NotNull
        LocalDate gewensteVerplaatsingsdatum,

        @Null
        UitvoeringsStatus uitvoeringsstatus
) {
}