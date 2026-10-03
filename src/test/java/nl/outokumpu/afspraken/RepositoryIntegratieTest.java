package nl.outokumpu.afspraken;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import nl.outokumpu.afspraken.entity.Afdeling;
import nl.outokumpu.afspraken.entity.Capaciteitswissel;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.entity.OperationeleAfspraak;

import java.util.List;

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

    @Test
    void vindtGebruikerOpEmailOngeachtHoofdletters() {

        Afdeling afdeling = new Afdeling("Planning");
        afdelingRepository.saveAndFlush(afdeling);

        Gebruiker gebruiker = new Gebruiker(
                "Email Test",
                "planner@example.com",
                afdeling
        );

        gebruikerRepository.saveAndFlush(gebruiker);

        entityManager.clear();

        Gebruiker gevondenGebruiker =
                gebruikerRepository
                        .findByEmailIgnoreCase("PLANNER@EXAMPLE.COM")
                        .orElseThrow();

        assertEquals(
                "Email Test",
                gevondenGebruiker.getNaam()
        );

        assertEquals(
                "planner@example.com",
                gevondenGebruiker.getEmail()
        );
    }

    @Test
    void vindtProcesstappenPerAfspraakInJuisteVolgorde() {

        Afdeling afdeling = new Afdeling("Planning");
        afdelingRepository.saveAndFlush(afdeling);

        Gebruiker gebruiker = new Gebruiker(
                "Proces Planner",
                "procesvolgorde.repository@example.com",
                afdeling
        );

        gebruikerRepository.saveAndFlush(gebruiker);

        OperationeleAfspraak afspraak = new OperationeleAfspraak(
                "Procesvolgorde repositorytest",
                "Testen van processtappen per afspraak",
                "Volgorde van workflow controleren",
                null,
                null,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                gebruiker
        );

        operationeleAfspraakRepository.saveAndFlush(afspraak);

        Processtap tweedeStap = new Processtap(
                "Goedkeuren",
                2,
                LocalDate.of(2026, 10, 15),
                afspraak,
                gebruiker
        );

        Processtap eersteStap = new Processtap(
                "Beoordelen",
                1,
                LocalDate.of(2026, 10, 10),
                afspraak,
                gebruiker
        );

        // Bewust in omgekeerde volgorde opslaan
        processtapRepository.save(tweedeStap);
        processtapRepository.saveAndFlush(eersteStap);

        UUID afspraakId = afspraak.getId();

        entityManager.clear();

        List<Processtap> processtappen =
                processtapRepository
                        .findByAfspraakIdOrderByVolgordeAsc(afspraakId);

        assertEquals(2, processtappen.size());

        assertEquals(
                1,
                processtappen.get(0).getVolgorde()
        );

        assertEquals(
                "Beoordelen",
                processtappen.get(0).getNaam()
        );

        assertEquals(
                2,
                processtappen.get(1).getVolgorde()
        );

        assertEquals(
                "Goedkeuren",
                processtappen.get(1).getNaam()
        );
    }

    @Test
    void vindtAlleBevestigingenVanEenAfspraak() {

        Afdeling afdeling = new Afdeling("Planning");
        afdelingRepository.saveAndFlush(afdeling);

        Gebruiker initiatiefnemer = new Gebruiker(
                "Initiatiefnemer",
                "initiatiefnemer.bevestiging@example.com",
                afdeling
        );

        Gebruiker eersteGoedkeurder = new Gebruiker(
                "Eerste Goedkeurder",
                "goedkeurder1@example.com",
                afdeling
        );

        Gebruiker tweedeGoedkeurder = new Gebruiker(
                "Tweede Goedkeurder",
                "goedkeurder2@example.com",
                afdeling
        );

        gebruikerRepository.save(initiatiefnemer);
        gebruikerRepository.save(eersteGoedkeurder);
        gebruikerRepository.saveAndFlush(tweedeGoedkeurder);

        OperationeleAfspraak afspraak = new OperationeleAfspraak(
                "Bevestigingen repositorytest",
                "Testen van meerdere bevestigingen per afspraak",
                "Goedkeuringsproces controleren",
                null,
                null,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                initiatiefnemer
        );

        operationeleAfspraakRepository.saveAndFlush(afspraak);

        Bevestiging eersteBevestiging = new Bevestiging(
                afspraak,
                eersteGoedkeurder
        );

        Bevestiging tweedeBevestiging = new Bevestiging(
                afspraak,
                tweedeGoedkeurder
        );

        bevestigingRepository.save(eersteBevestiging);
        bevestigingRepository.saveAndFlush(tweedeBevestiging);

        UUID afspraakId = afspraak.getId();

        entityManager.clear();

        List<Bevestiging> bevestigingen =
                bevestigingRepository.findByAfspraakId(afspraakId);

        assertEquals(2, bevestigingen.size());

        assertTrue(
                bevestigingen.stream()
                        .anyMatch(bevestiging ->
                                bevestiging.getGebruiker()
                                        .getEmail()
                                        .equals("goedkeurder1@example.com"))
        );

        assertTrue(
                bevestigingen.stream()
                        .anyMatch(bevestiging ->
                                bevestiging.getGebruiker()
                                        .getEmail()
                                        .equals("goedkeurder2@example.com"))
        );
    }

    @Test
    void vindtWijzigingenVanEenAfspraakInChronologischeVolgorde() {

        Afdeling afdeling = new Afdeling("Planning");
        afdelingRepository.saveAndFlush(afdeling);

        Gebruiker gebruiker = new Gebruiker(
                "Historie Planner",
                "historie.repository@example.com",
                afdeling
        );

        gebruikerRepository.saveAndFlush(gebruiker);

        OperationeleAfspraak afspraak = new OperationeleAfspraak(
                "Historie repositorytest",
                "Testen van wijzigingshistorie per afspraak",
                "Chronologische historie controleren",
                null,
                null,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                gebruiker
        );

        operationeleAfspraakRepository.saveAndFlush(afspraak);

        Wijziging eersteWijziging = new Wijziging(
                afspraak,
                gebruiker,
                "deadline",
                "2026-10-20",
                "2026-10-25"
        );

        wijzigingRepository.saveAndFlush(eersteWijziging);

        Wijziging tweedeWijziging = new Wijziging(
                afspraak,
                gebruiker,
                "deadline",
                "2026-10-25",
                "2026-10-31"
        );

        wijzigingRepository.saveAndFlush(tweedeWijziging);

        UUID afspraakId = afspraak.getId();

        entityManager.clear();

        List<Wijziging> wijzigingen =
                wijzigingRepository
                        .findByAfspraakIdOrderByGewijzigdOpAsc(afspraakId);

        assertEquals(2, wijzigingen.size());

        assertEquals(
                "2026-10-20",
                wijzigingen.get(0).getOudeWaarde()
        );

        assertEquals(
                "2026-10-25",
                wijzigingen.get(0).getNieuweWaarde()
        );

        assertEquals(
                "2026-10-25",
                wijzigingen.get(1).getOudeWaarde()
        );

        assertEquals(
                "2026-10-31",
                wijzigingen.get(1).getNieuweWaarde()
        );

        assertFalse(
                wijzigingen.get(0)
                        .getGewijzigdOp()
                        .isAfter(wijzigingen.get(1).getGewijzigdOp())
        );
    }



}