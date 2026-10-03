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

import nl.outokumpu.afspraken.entity.Processtap;
import nl.outokumpu.afspraken.enums.ProcesstapStatus;
import nl.outokumpu.afspraken.repository.ProcesstapRepository;

import nl.outokumpu.afspraken.entity.Bevestiging;
import nl.outokumpu.afspraken.enums.Beslissing;
import nl.outokumpu.afspraken.repository.BevestigingRepository;

import nl.outokumpu.afspraken.entity.Wijziging;
import nl.outokumpu.afspraken.repository.WijzigingRepository;

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

    @Autowired
    private ProcesstapRepository processtapRepository;

    @Autowired
    private BevestigingRepository bevestigingRepository;

    @Autowired
    private WijzigingRepository wijzigingRepository;

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

    @Test
    void slaatProcesstapOpEnHaaltDezeOpViaRepository() {

        // 1. Afdeling aanmaken
        Afdeling afdeling = new Afdeling("Planning");
        afdelingRepository.saveAndFlush(afdeling);

        // 2. Gebruiker aanmaken
        Gebruiker gebruiker = new Gebruiker(
                "Workflow Planner",
                "workflow.repository@example.com",
                afdeling
        );

        gebruikerRepository.saveAndFlush(gebruiker);

        // 3. Afspraak aanmaken
        OperationeleAfspraak afspraak = new OperationeleAfspraak(
                "Workflow repositorytest",
                "Afspraak voor het testen van ProcesstapRepository",
                "Repositorylaag controleren",
                null,
                null,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                gebruiker
        );

        operationeleAfspraakRepository.saveAndFlush(afspraak);

        // 4. Processtap aanmaken
        Processtap processtap = new Processtap(
                "Impact beoordelen",
                1,
                LocalDate.of(2026, 10, 10),
                afspraak,
                gebruiker
        );

        Processtap opgeslagenProcesstap =
                processtapRepository.saveAndFlush(processtap);

        UUID id = opgeslagenProcesstap.getId();

        assertNotNull(id);

        // 5. Persistence context leegmaken
        entityManager.clear();

        // 6. Processtap opnieuw ophalen
        Processtap opgehaaldeProcesstap =
                processtapRepository.findById(id)
                        .orElseThrow();

        // 7. Gegevens controleren
        assertEquals(
                "Impact beoordelen",
                opgehaaldeProcesstap.getNaam()
        );

        assertEquals(
                1,
                opgehaaldeProcesstap.getVolgorde()
        );

        assertEquals(
                ProcesstapStatus.NIET_GESTART,
                opgehaaldeProcesstap.getStatus()
        );

        assertEquals(
                "Workflow repositorytest",
                opgehaaldeProcesstap.getAfspraak().getTitel()
        );

        assertEquals(
                "Workflow Planner",
                opgehaaldeProcesstap.getVerantwoordelijke().getNaam()
        );
    }

    @Test
    void slaatBevestigingOpEnHaaltDezeOpViaRepository() {

        Afdeling afdeling = new Afdeling("Planning");
        afdelingRepository.saveAndFlush(afdeling);

        Gebruiker gebruiker = new Gebruiker(
                "Test Goedkeurder",
                "bevestiging.repository@example.com",
                afdeling
        );

        gebruikerRepository.saveAndFlush(gebruiker);

        OperationeleAfspraak afspraak = new OperationeleAfspraak(
                "Bevestiging repositorytest",
                "Afspraak voor het testen van BevestigingRepository",
                "Repositorylaag controleren",
                null,
                null,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                gebruiker
        );

        operationeleAfspraakRepository.saveAndFlush(afspraak);

        Bevestiging bevestiging = new Bevestiging(
                afspraak,
                gebruiker
        );

        Bevestiging opgeslagenBevestiging =
                bevestigingRepository.saveAndFlush(bevestiging);

        UUID id = opgeslagenBevestiging.getId();

        assertNotNull(id);

        entityManager.clear();

        Bevestiging opgehaaldeBevestiging =
                bevestigingRepository.findById(id)
                        .orElseThrow();

        assertFalse(opgehaaldeBevestiging.isGezien());

        assertEquals(
                Beslissing.GEEN,
                opgehaaldeBevestiging.getBeslissing()
        );

        assertEquals(
                "Bevestiging repositorytest",
                opgehaaldeBevestiging.getAfspraak().getTitel()
        );

        assertEquals(
                "Test Goedkeurder",
                opgehaaldeBevestiging.getGebruiker().getNaam()
        );
    }

    @Test
    void slaatWijzigingOpEnHaaltDezeOpViaRepository() {

        Afdeling afdeling = new Afdeling("Planning");
        afdelingRepository.saveAndFlush(afdeling);

        Gebruiker gebruiker = new Gebruiker(
                "Test Planner",
                "wijziging.repository@example.com",
                afdeling
        );

        gebruikerRepository.saveAndFlush(gebruiker);

        OperationeleAfspraak afspraak = new OperationeleAfspraak(
                "Wijziging repositorytest",
                "Afspraak voor het testen van WijzigingRepository",
                "Repositorylaag controleren",
                null,
                null,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                gebruiker
        );

        operationeleAfspraakRepository.saveAndFlush(afspraak);

        Wijziging wijziging = new Wijziging(
                afspraak,
                gebruiker,
                "deadline",
                "2026-10-20",
                "2026-10-31"
        );

        Wijziging opgeslagenWijziging =
                wijzigingRepository.saveAndFlush(wijziging);

        UUID id = opgeslagenWijziging.getId();

        assertNotNull(id);
        assertNotNull(opgeslagenWijziging.getGewijzigdOp());

        entityManager.clear();

        Wijziging opgehaaldeWijziging =
                wijzigingRepository.findById(id)
                        .orElseThrow();

        assertEquals(
                "deadline",
                opgehaaldeWijziging.getOnderdeel()
        );

        assertEquals(
                "2026-10-20",
                opgehaaldeWijziging.getOudeWaarde()
        );

        assertEquals(
                "2026-10-31",
                opgehaaldeWijziging.getNieuweWaarde()
        );

        assertNotNull(
                opgehaaldeWijziging.getGewijzigdOp()
        );

        assertEquals(
                "Wijziging repositorytest",
                opgehaaldeWijziging.getAfspraak().getTitel()
        );

        assertEquals(
                "Test Planner",
                opgehaaldeWijziging.getGewijzigdDoor().getNaam()
        );
    }



}