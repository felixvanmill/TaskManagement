package nl.outokumpu.afspraken.dto.data;

import nl.outokumpu.afspraken.enums.UitvoeringsStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderverplaatsingData(
        String vanFabriek,
        String naarFabriek,
        String finishType,
        BigDecimal totaalVolumeTons,
        LocalDate gewensteVerplaatsingsdatum,
        UitvoeringsStatus uitvoeringsstatus
) {
}