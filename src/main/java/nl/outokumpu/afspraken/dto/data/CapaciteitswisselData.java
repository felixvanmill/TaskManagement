package nl.outokumpu.afspraken.dto.data;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CapaciteitswisselData(
        String fabriek,
        String finishType,
        String grade,
        BigDecimal capaciteitVan,
        BigDecimal capaciteitNaar,
        String capaciteitEenheid,
        LocalDate periodeVan,
        LocalDate periodeTot
) {
}