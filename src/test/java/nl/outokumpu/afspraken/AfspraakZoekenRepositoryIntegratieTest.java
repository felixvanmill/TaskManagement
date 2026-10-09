package nl.outokumpu.afspraken;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import nl.outokumpu.afspraken.entity.Afdeling;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.entity.OperationeleAfspraak;
import nl.outokumpu.afspraken.enums.AfspraakStatus;
import nl.outokumpu.afspraken.repository.AfdelingRepository;
import nl.outokumpu.afspraken.repository.GebruikerRepository;
import nl.outokumpu.afspraken.repository.OperationeleAfspraakRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AfspraakZoekenRepositoryIntegratieTest {

    @Autowired
    private OperationeleAfspraakRepository afspraakRepository;

    @Autowired
    private GebruikerRepository gebruikerRepository;

    @Autowired
    private AfdelingRepository afdelingRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void zoektHoofdletterOngevoeligInTitelEnBeschrijving() {

        Gebruiker gebruiker =
                maakGebruiker(
                        "zoek.repository@example.com"
                );

        OperationeleAfspraak titelMatch =
                new OperationeleAfspraak(
                        "Capaciteitsplanning oktober",
                        "Algemene beschrijving",
                        "Reden",
                        null,
                        null,
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31),
                        gebruiker
                );

        OperationeleAfspraak beschrijvingMatch =
                new OperationeleAfspraak(
                        "Productieafspraak",
                        "Nieuwe PLANNING voor productie",
                        "Reden",
                        null,
                        null,
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31),
                        gebruiker
                );

        OperationeleAfspraak geenMatch =
                new OperationeleAfspraak(
                        "Andere afspraak",
                        "Heeft niets met de zoekterm te maken",
                        "Reden",
                        null,
                        null,
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31),
                        gebruiker
                );

        afspraakRepository.save(
                titelMatch
        );

        afspraakRepository.save(
                beschrijvingMatch
        );

        afspraakRepository.saveAndFlush(
                geenMatch
        );

        entityManager.clear();

        List<OperationeleAfspraak> resultaat =
                afspraakRepository
                        .zoekOpTrefwoord(
                                "planning"
                        );

        assertEquals(
                2,
                resultaat.size()
        );

        assertTrue(
                resultaat.stream()
                        .anyMatch(afspraak ->
                                afspraak.getTitel()
                                        .equals(
                                                "Capaciteitsplanning oktober"
                                        )
                        )
        );

        assertTrue(
                resultaat.stream()
                        .anyMatch(afspraak ->
                                afspraak.getTitel()
                                        .equals(
                                                "Productieafspraak"
                                        )
                        )
        );
    }

    @Test
    void vindtOokAfgerondeAfspraakViaZoeken() {

        Gebruiker gebruiker =
                maakGebruiker(
                        "archief.repository@example.com"
                );

        OperationeleAfspraak afspraak =
                new OperationeleAfspraak(
                        "Archiefzoekterm afspraak",
                        "Afgeronde afspraak",
                        "Reden",
                        null,
                        null,
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 9, 30),
                        gebruiker
                );

        afspraak.wijzigStatus(
                AfspraakStatus.AFGEROND
        );

        afspraakRepository.saveAndFlush(
                afspraak
        );

        entityManager.clear();

        List<OperationeleAfspraak> resultaat =
                afspraakRepository
                        .zoekOpTrefwoord(
                                "archiefzoekterm"
                        );

        assertEquals(
                1,
                resultaat.size()
        );

        assertEquals(
                AfspraakStatus.AFGEROND,
                resultaat.get(0)
                        .getStatus()
        );
    }

    private Gebruiker maakGebruiker(
            String email
    ) {

        Afdeling afdeling =
                new Afdeling(
                        "Zoektest afdeling"
                );

        afdelingRepository.saveAndFlush(
                afdeling
        );

        Gebruiker gebruiker =
                new Gebruiker(
                        "Zoek Test Gebruiker",
                        email,
                        afdeling
                );

        return gebruikerRepository.saveAndFlush(
                gebruiker
        );
    }
}