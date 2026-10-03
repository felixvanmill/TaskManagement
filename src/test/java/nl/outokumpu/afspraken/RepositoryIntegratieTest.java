package nl.outokumpu.afspraken;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import nl.outokumpu.afspraken.entity.Afdeling;
import nl.outokumpu.afspraken.entity.Capaciteitswissel;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.entity.OperationeleAfspraak;

import nl.outokumpu.afspraken.enums.AfspraakType;

import nl.outokumpu.afspraken.repository.AfdelingRepository;
import nl.outokumpu.afspraken.repository.GebruikerRepository;
import nl.outokumpu.afspraken.repository.OperationeleAfspraakRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RepositoryIntegratieTest {

    @Autowired
    private AfdelingRepository afdelingRepository;

    @Autowired
    private GebruikerRepository gebruikerRepository;

    @Autowired
    private OperationeleAfspraakRepository operationeleAfspraakRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void slaatAfdelingOpEnHaaltDezeOpViaRepository() {

        Afdeling afdeling = new Afdeling("Planning");

        Afdeling opgeslagenAfdeling =
                afdelingRepository.saveAndFlush(afdeling);

        UUID id = opgeslagenAfdeling.getId();

        assertNotNull(id);

        Afdeling opgehaaldeAfdeling =
                afdelingRepository.findById(id)
                        .orElseThrow();

        assertEquals(
                "Planning",
                opgehaaldeAfdeling.getNaam()
        );
    }

    @Test
    void slaatGebruikerOpEnHaaltDezeOpViaRepository() {

        Afdeling afdeling = new Afdeling("Capacity Planning");
        afdelingRepository.saveAndFlush(afdeling);

        Gebruiker gebruiker = new Gebruiker(
                "Test Planner",
                "planner.repository@example.com",
                afdeling
        );

        Gebruiker opgeslagenGebruiker =
                gebruikerRepository.saveAndFlush(gebruiker);

        UUID id = opgeslagenGebruiker.getId();

        assertNotNull(id);

        Gebruiker opgehaaldeGebruiker =
                gebruikerRepository.findById(id)
                        .orElseThrow();

        assertEquals(
                "Test Planner",
                opgehaaldeGebruiker.getNaam()
        );

        assertEquals(
                "planner.repository@example.com",
                opgehaaldeGebruiker.getEmail()
        );

        assertEquals(
                "Capacity Planning",
                opgehaaldeGebruiker.getAfdeling().getNaam()
        );
    }

    @Test
    void slaatCapaciteitswisselOpEnHaaltDezeViaBasisRepositoryOp() {

        Afdeling afdeling = new Afdeling("Capacity Planning");
        afdelingRepository.saveAndFlush(afdeling);

        Gebruiker gebruiker = new Gebruiker(
                "Capacity Planner",
                "capacity.repository@example.com",
                afdeling
        );

        gebruikerRepository.saveAndFlush(gebruiker);

        Capaciteitswissel wissel = new Capaciteitswissel(
                "Capaciteitswissel repositorytest",
                "Testen van opslag via de basisrepository",
                "Controleren van JOINED inheritance",
                null,
                null,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 20),
                gebruiker,
                "Fabriek A",
                "2B",
                "304",
                new BigDecimal("100.000"),
                new BigDecimal("125.000"),
                "ton",
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 15)
        );

        OperationeleAfspraak opgeslagenAfspraak =
                operationeleAfspraakRepository.saveAndFlush(wissel);

        UUID id = opgeslagenAfspraak.getId();

        assertNotNull(id);

        // Zorg dat findById daadwerkelijk opnieuw uit de database leest
        entityManager.clear();

        OperationeleAfspraak opgehaaldeAfspraak =
                operationeleAfspraakRepository.findById(id)
                        .orElseThrow();

        Capaciteitswissel opgehaaldeWissel = assertInstanceOf(
                Capaciteitswissel.class,
                opgehaaldeAfspraak
        );

        assertEquals(
                "Capaciteitswissel repositorytest",
                opgehaaldeWissel.getTitel()
        );

        assertEquals(
                AfspraakType.CAPACITEITSWISSEL,
                opgehaaldeWissel.getType()
        );

        assertEquals(
                new BigDecimal("125.000"),
                opgehaaldeWissel.getCapaciteitNaar()
        );

        assertEquals(
                "Fabriek A",
                opgehaaldeWissel.getFabriek()
        );
    }



}