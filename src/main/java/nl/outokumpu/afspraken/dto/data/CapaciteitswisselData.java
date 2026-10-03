package nl.outokumpu.afspraken.dto.data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CapaciteitswisselData(

        @NotBlank
        @Size(max = 100)
        String fabriek,

        @NotBlank
        @Size(max = 100)
        String finishType,

        @NotBlank
        @Size(max = 100)
        String grade,

        @NotNull
        BigDecimal capaciteitVan,

        @NotNull
        BigDecimal capaciteitNaar,

        @NotBlank
        @Size(max = 50)
        String capaciteitEenheid,

        @NotNull
        LocalDate periodeVan,

        @NotNull
        LocalDate periodeTot
) {
}