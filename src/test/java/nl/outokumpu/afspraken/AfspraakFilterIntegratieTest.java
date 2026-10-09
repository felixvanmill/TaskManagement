package nl.outokumpu.afspraken;

import nl.outokumpu.afspraken.dto.request.AfspraakFilterRequest;
import nl.outokumpu.afspraken.dto.response.AfspraakSummaryResponse;
import nl.outokumpu.afspraken.entity.Afdeling;
import nl.outokumpu.afspraken.entity.Capaciteitswissel;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.entity.Orderverplaatsing;
import nl.outokumpu.afspraken.enums.AfspraakStatus;
import nl.outokumpu.afspraken.enums.AfspraakType;
import nl.outokumpu.afspraken.repository.AfdelingRepository;
import nl.outokumpu.afspraken.repository.GebruikerRepository;
import nl.outokumpu.afspraken.repository.OperationeleAfspraakRepository;
import nl.outokumpu.afspraken.service.AfspraakService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AfspraakFilterIntegratieTest {

    @Autowired
    private AfspraakService afspraakService;

    @Autowired
    private OperationeleAfspraakRepository afspraakRepository;

    @Autowired
    private GebruikerRepository gebruikerRepository;

    @Autowired
    private AfdelingRepository afdelingRepository;

    @Test
    void combineertTypeStatusVerantwoordelijkeAfdelingFabriekEnPeriode() {

        Afdeling planning =
                afdelingRepository.saveAndFlush(
                        new Afdeling(
                                "Filter Planning"
                        )
                );

        Afdeling andereAfdeling =
                afdelingRepository.saveAndFlush(
                        new Afdeling(
                                "Andere afdeling"
                        )
                );

        Gebruiker verantwoordelijke =
                gebruikerRepository.saveAndFlush(
                        new Gebruiker(
                                "Filter Verantwoordelijke",
                                "filter.verantwoordelijke@example.com",
                                planning
                        )
                );

        Gebruiker andereGebruiker =
                gebruikerRepository.saveAndFlush(
                        new Gebruiker(
                                "Andere Gebruiker",
                                "filter.andere@example.com",
                                andereAfdeling
                        )
                );

        Capaciteitswissel match =
                new Capaciteitswissel(
                        "Match",
                        "Moet worden gevonden",
                        "Filtertest",
                        null,
                        null,
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31),
                        verantwoordelijke,
                        "Tornio",
                        "2B",
                        "304L",
                        new BigDecimal("100.000"),
                        new BigDecimal("125.000"),
                        "ton",
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31)
                );

        match.wijzigStatus(
                AfspraakStatus.IN_BEHANDELING
        );

        match.voegAfdelingToe(
                planning
        );

        match.voegProcesstapToe(
                "Uitvoeren",
                1,
                LocalDate.of(2026, 10, 20),
                verantwoordelijke
        );

        afspraakRepository.saveAndFlush(
                match
        );

        Capaciteitswissel geenMatch =
                new Capaciteitswissel(
                        "Geen match",
                        "Andere afspraak",
                        "Filtertest",
                        null,
                        null,
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31),
                        andereGebruiker,
                        "Avesta",
                        "2B",
                        "304L",
                        new BigDecimal("100.000"),
                        new BigDecimal("125.000"),
                        "ton",
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31)
                );

        geenMatch.voegAfdelingToe(
                andereAfdeling
        );

        geenMatch.voegProcesstapToe(
                "Uitvoeren",
                1,
                LocalDate.of(2026, 10, 20),
                andereGebruiker
        );

        afspraakRepository.saveAndFlush(
                geenMatch
        );

        AfspraakFilterRequest filter =
                new AfspraakFilterRequest(
                        AfspraakType.CAPACITEITSWISSEL,
                        AfspraakStatus.IN_BEHANDELING,
                        verantwoordelijke.getId(),
                        planning.getId(),
                        "tornio",
                        LocalDate.of(2026, 10, 10),
                        LocalDate.of(2026, 10, 20)
                );

        List<AfspraakSummaryResponse> resultaat =
                afspraakService.filterAfspraken(
                        filter
                );

        assertEquals(
                1,
                resultaat.size()
        );
    }

    @Test
    void filtertOrderverplaatsingOpHerkomstOfBestemmingsfabriek() {

        Afdeling afdeling =
                afdelingRepository.saveAndFlush(
                        new Afdeling(
                                "Order Filter"
                        )
                );

        Gebruiker gebruiker =
                gebruikerRepository.saveAndFlush(
                        new Gebruiker(
                                "Order Filter Gebruiker",
                                "order.filter@example.com",
                                afdeling
                        )
                );

        Orderverplaatsing order =
                new Orderverplaatsing(
                        "Order naar Avesta",
                        "Beschrijving",
                        "Filtertest",
                        null,
                        null,
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31),
                        gebruiker,
                        "Tornio",
                        "Avesta",
                        "2B",
                        new BigDecimal("100.000"),
                        LocalDate.of(2026, 10, 20)
                );

        afspraakRepository.saveAndFlush(
                order
        );

        AfspraakFilterRequest filter =
                new AfspraakFilterRequest(
                        null,
                        null,
                        null,
                        null,
                        "avesta",
                        null,
                        null
                );

        List<AfspraakSummaryResponse> resultaat =
                afspraakService.filterAfspraken(
                        filter
                );

        assertEquals(
                1,
                resultaat.size()
        );
    }

    @Test
    void periodeFilterGebruiktOverlap() {

        Afdeling afdeling =
                afdelingRepository.saveAndFlush(
                        new Afdeling(
                                "Periode Filter"
                        )
                );

        Gebruiker gebruiker =
                gebruikerRepository.saveAndFlush(
                        new Gebruiker(
                                "Periode Filter Gebruiker",
                                "periode.filter@example.com",
                                afdeling
                        )
                );

        Capaciteitswissel afspraak =
                new Capaciteitswissel(
                        "Overlappende afspraak",
                        "Beschrijving",
                        "Filtertest",
                        null,
                        null,
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 20),
                        gebruiker,
                        "Tornio",
                        "2B",
                        "304L",
                        new BigDecimal("100.000"),
                        new BigDecimal("125.000"),
                        "ton",
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 20)
                );

        afspraakRepository.saveAndFlush(
                afspraak
        );

        AfspraakFilterRequest filter =
                new AfspraakFilterRequest(
                        null,
                        null,
                        null,
                        null,
                        null,
                        LocalDate.of(2026, 10, 15),
                        LocalDate.of(2026, 10, 25)
                );

        List<AfspraakSummaryResponse> resultaat =
                afspraakService.filterAfspraken(
                        filter
                );

        assertEquals(
                1,
                resultaat.size()
        );
    }
}