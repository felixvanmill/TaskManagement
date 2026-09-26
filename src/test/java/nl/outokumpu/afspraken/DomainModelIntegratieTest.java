package nl.outokumpu.afspraken;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import nl.outokumpu.afspraken.entity.*;
import nl.outokumpu.afspraken.enums.*;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import jakarta.persistence.PersistenceException;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DomainModelIntegratieTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void slaatAfspraakMetAlleRelatiesOpEnLeestDezeOpnieuwUit() {

        // 1. Afdeling aanmaken en opslaan
        Afdeling afdeling = new Afdeling("Planning");
        entityManager.persist(afdeling);

        // 2. Gebruiker aanmaken en opslaan
        Gebruiker gebruiker = new Gebruiker(
                "Test Gebruiker",
                "test.gebruiker@example.com",
                afdeling
        );

        entityManager.persist(gebruiker);

        // 3. Operationele afspraak aanmaken
        OperationeleAfspraak afspraak = new OperationeleAfspraak(
                "Testafspraak",
                "Beschrijving van de testafspraak",
                "Controleren of de JPA-relaties werken",
                "Achtergrond van de afspraak",
                "Testaanname",
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 20),
                gebruiker
        );

        // 4. Alle vijf relaties toevoegen
        afspraak.voegAfdelingToe(afdeling);

        afspraak.voegBetrokkenheidToe(
                gebruiker,
                BetrokkenRol.GOEDKEURDER
        );

        afspraak.voegProcesstapToe(
                "Impact beoordelen",
                1,
                LocalDate.of(2026, 10, 10),
                gebruiker
        );

        afspraak.voegBevestigingToe(gebruiker);

        afspraak.registreerWijziging(
                gebruiker,
                "deadline",
                "2026-10-15",
                "2026-10-20"
        );

        // 5. Afspraak opslaan
        entityManager.persist(afspraak);

        UUID afspraakId = afspraak.getId();

        // 6. SQL uitvoeren en persistence context leegmaken
        entityManager.flush();
        entityManager.clear();

        // 7. Afspraak opnieuw uit PostgreSQL ophalen
        OperationeleAfspraak opgeslagenAfspraak =
                entityManager.find(
                        OperationeleAfspraak.class,
                        afspraakId
                );

        // 8. Basisgegevens controleren
        assertNotNull(opgeslagenAfspraak);

        assertEquals(
                "Testafspraak",
                opgeslagenAfspraak.getTitel()
        );

        assertEquals(
                AfspraakType.ALGEMEEN,
                opgeslagenAfspraak.getType()
        );

        assertEquals(
                AfspraakStatus.OPEN,
                opgeslagenAfspraak.getStatus()
        );

        assertNotNull(opgeslagenAfspraak.getAangemaaktOp());

        // 9. Alle vijf relaties controleren
        assertEquals(
                1,
                opgeslagenAfspraak.getBetrokkenAfdelingen().size()
        );

        assertEquals(
                1,
                opgeslagenAfspraak.getBetrokkenheden().size()
        );

        assertEquals(
                1,
                opgeslagenAfspraak.getProcesstappen().size()
        );

        assertEquals(
                1,
                opgeslagenAfspraak.getBevestigingen().size()
        );

        assertEquals(
                1,
                opgeslagenAfspraak.getWijzigingen().size()
        );

        // 10. Inhoud van de relaties controleren
        assertEquals(
                BetrokkenRol.GOEDKEURDER,
                opgeslagenAfspraak.getBetrokkenheden()
                        .get(0).getRol()
        );

        assertEquals(
                "Impact beoordelen",
                opgeslagenAfspraak.getProcesstappen()
                        .get(0).getNaam()
        );

        assertEquals(
                ProcesstapStatus.NIET_GESTART,
                opgeslagenAfspraak.getProcesstappen()
                        .get(0).getStatus()
        );

        assertEquals(
                Beslissing.GEEN,
                opgeslagenAfspraak.getBevestigingen()
                        .get(0).getBeslissing()
        );

        assertEquals(
                "deadline",
                opgeslagenAfspraak.getWijzigingen()
                        .get(0).getOnderdeel()
        );

        assertNotNull(
                opgeslagenAfspraak.getWijzigingen()
                        .get(0).getGewijzigdOp()
        );
    }


    @Test
    void slaatSubtypenCorrectOpMetJoinedInheritance() {

        // 1. Testgegevens aanmaken
        Afdeling afdeling = new Afdeling("Capacity Planning");
        entityManager.persist(afdeling);

        Gebruiker gebruiker = new Gebruiker(
                "Test Planner",
                "planner@example.com",
                afdeling
        );

        entityManager.persist(gebruiker);

        // 2. Capaciteitswissel aanmaken
        Capaciteitswissel wissel = new Capaciteitswissel(
                "Capaciteitswissel test",
                "Capaciteit tussen periodes verschuiven",
                "Betere capaciteitsverdeling",
                null,
                null,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 20),
                gebruiker,
                "Fabriek A",
                "2B",
                "304",
                new BigDecimal("100.000"),
                new BigDecimal("120.000"),
                "ton",
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 15)
        );

        // 3. Orderverplaatsing aanmaken
        Orderverplaatsing verplaatsing = new Orderverplaatsing(
                "Orderverplaatsing test",
                "Order verplaatsen naar andere fabriek",
                "Beschikbare capaciteit benutten",
                null,
                null,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 20),
                gebruiker,
                "Fabriek A",
                "Fabriek B",
                "2B",
                new BigDecimal("450.000"),
                LocalDate.of(2026, 10, 12)
        );

        // 4. Beide objecten opslaan
        entityManager.persist(wissel);
        entityManager.persist(verplaatsing);

        UUID wisselId = wissel.getId();
        UUID verplaatsingId = verplaatsing.getId();

        entityManager.flush();
        entityManager.clear();

        // 5. Ophalen via de basisklasse
        OperationeleAfspraak opgehaaldeWissel =
                entityManager.find(
                        OperationeleAfspraak.class,
                        wisselId
                );

        OperationeleAfspraak opgehaaldeVerplaatsing =
                entityManager.find(
                        OperationeleAfspraak.class,
                        verplaatsingId
                );

        // 6. Controleren of Hibernate de juiste subtypen herkent
        Capaciteitswissel echteWissel = assertInstanceOf(
                Capaciteitswissel.class,
                opgehaaldeWissel
        );

        Orderverplaatsing echteVerplaatsing = assertInstanceOf(
                Orderverplaatsing.class,
                opgehaaldeVerplaatsing
        );

        // 7. Algemene gegevens controleren
        assertEquals(
                AfspraakType.CAPACITEITSWISSEL,
                echteWissel.getType()
        );

        assertEquals(
                AfspraakType.ORDERVERPLAATSING,
                echteVerplaatsing.getType()
        );

        assertEquals(AfspraakStatus.OPEN, echteWissel.getStatus());
        assertEquals(AfspraakStatus.OPEN, echteVerplaatsing.getStatus());

        // 8. Specifieke gegevens controleren
        assertEquals("Fabriek A", echteWissel.getFabriek());

        assertEquals(
                new BigDecimal("120.000"),
                echteWissel.getCapaciteitNaar()
        );

        assertEquals(
                "Fabriek B",
                echteVerplaatsing.getNaarFabriek()
        );

        assertEquals(
                new BigDecimal("450.000"),
                echteVerplaatsing.getTotaalVolumeTons()
        );

        assertEquals(
                UitvoeringsStatus.NIET_UITGEVOERD,
                echteVerplaatsing.getUitvoeringsstatus()
        );

        // 9. Controleren of de specifieke tabellen records bevatten
        Number aantalWissels = (Number) entityManager
                .createNativeQuery(
                        "SELECT COUNT(*) FROM capaciteitswissels WHERE id = :id"
                )
                .setParameter("id", wisselId)
                .getSingleResult();

        Number aantalVerplaatsingen = (Number) entityManager
                .createNativeQuery(
                        "SELECT COUNT(*) FROM orderverplaatsingen WHERE id = :id"
                )
                .setParameter("id", verplaatsingId)
                .getSingleResult();

        assertEquals(1L, aantalWissels.longValue());
        assertEquals(1L, aantalVerplaatsingen.longValue());
    }


    @Test
    void voorkomtDubbeleProcesstapVolgordeInDatabase() {

        // 1. Afdeling en gebruiker aanmaken
        Afdeling afdeling = new Afdeling("Planning");
        entityManager.persist(afdeling);

        Gebruiker gebruiker = new Gebruiker(
                "Test Planner",
                "planner.constraint@example.com",
                afdeling
        );

        entityManager.persist(gebruiker);

        // 2. Operationele afspraak aanmaken
        OperationeleAfspraak afspraak = new OperationeleAfspraak(
                "Test dubbele processtappen",
                "Test voor de unieke volgorde",
                "Databaseconstraint controleren",
                null,
                null,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 20),
                gebruiker
        );

        entityManager.persist(afspraak);
        entityManager.flush();

        // 3. Twee processtappen met dezelfde volgorde maken
        Processtap eersteStap = new Processtap(
                "Impact beoordelen",
                1,
                LocalDate.of(2026, 10, 10),
                afspraak,
                gebruiker
        );

        Processtap tweedeStap = new Processtap(
                "Planning controleren",
                1,
                LocalDate.of(2026, 10, 15),
                afspraak,
                gebruiker
        );

        // 4. Beide rechtstreeks via EntityManager opslaan
        // Hiermee omzeilen we bewust de Java-controle.
        entityManager.persist(eersteStap);
        entityManager.persist(tweedeStap);

        // 5. PostgreSQL moet de dubbele combinatie weigeren
        assertThrows(
                PersistenceException.class,
                () -> entityManager.flush()
        );
    }


    @Test
    void voorkomtDubbeleBevestigingVoorDezelfdeGebruiker() {

        // 1. Afdeling en gebruiker aanmaken
        Afdeling afdeling = new Afdeling("Planning");
        entityManager.persist(afdeling);

        Gebruiker gebruiker = new Gebruiker(
                "Test Goedkeurder",
                "goedkeurder.constraint@example.com",
                afdeling
        );

        entityManager.persist(gebruiker);

        // 2. Operationele afspraak aanmaken
        OperationeleAfspraak afspraak = new OperationeleAfspraak(
                "Test dubbele bevestigingen",
                "Controleren van unieke bevestigingen",
                "Databaseconstraint testen",
                null,
                null,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 20),
                gebruiker
        );

        entityManager.persist(afspraak);
        entityManager.flush();

        // 3. Twee bevestigingen voor dezelfde gebruiker en afspraak
        Bevestiging eersteBevestiging = new Bevestiging(
                afspraak,
                gebruiker
        );

        Bevestiging tweedeBevestiging = new Bevestiging(
                afspraak,
                gebruiker
        );

        // 4. Rechtstreeks opslaan via EntityManager
        // Hiermee omzeilen we bewust voegBevestigingToe().
        entityManager.persist(eersteBevestiging);
        entityManager.persist(tweedeBevestiging);

        // 5. PostgreSQL moet de dubbele combinatie weigeren
        assertThrows(
                PersistenceException.class,
                () -> entityManager.flush()
        );
    }


    @Test
    void voorkomtDubbeleBetrokkenheidMetDezelfdeRol() {

        // 1. Afdeling en gebruiker aanmaken
        Afdeling afdeling = new Afdeling("Planning");
        entityManager.persist(afdeling);

        Gebruiker gebruiker = new Gebruiker(
                "Test Betrokkene",
                "betrokkene.constraint@example.com",
                afdeling
        );

        entityManager.persist(gebruiker);

        // 2. Operationele afspraak aanmaken
        OperationeleAfspraak afspraak = new OperationeleAfspraak(
                "Test dubbele betrokkenheid",
                "Controleren van unieke gebruikersrollen",
                "Databaseconstraint testen",
                null,
                null,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 20),
                gebruiker
        );

        entityManager.persist(afspraak);
        entityManager.flush();

        // 3. Dezelfde gebruiker tweemaal dezelfde rol geven
        Betrokkenheid eersteBetrokkenheid = new Betrokkenheid(
                afspraak,
                gebruiker,
                BetrokkenRol.BETROKKENE
        );

        Betrokkenheid tweedeBetrokkenheid = new Betrokkenheid(
                afspraak,
                gebruiker,
                BetrokkenRol.BETROKKENE
        );

        // 4. Rechtstreeks opslaan om Java-validatie te omzeilen
        entityManager.persist(eersteBetrokkenheid);
        entityManager.persist(tweedeBetrokkenheid);

        // 5. PostgreSQL moet de dubbele combinatie weigeren
        assertThrows(
                PersistenceException.class,
                () -> entityManager.flush()
        );
    }


    @Test
    void staatTweeVerschillendeRollenVoorDezelfdeGebruikerToe() {

        // 1. Afdeling aanmaken
        Afdeling afdeling = new Afdeling("Planning");
        entityManager.persist(afdeling);

        // 2. Gebruiker aanmaken
        Gebruiker gebruiker = new Gebruiker(
                "Test Gebruiker",
                "dubbele.rol@example.com",
                afdeling
        );

        entityManager.persist(gebruiker);

        UUID gebruikerId = gebruiker.getId();

        // 3. Operationele afspraak aanmaken
        OperationeleAfspraak afspraak = new OperationeleAfspraak(
                "Afspraak met meerdere rollen",
                "Testen of een gebruiker meerdere rollen mag hebben",
                "Correcte werking van betrokkenheden controleren",
                null,
                null,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 20),
                gebruiker
        );

        // 4. Dezelfde gebruiker twee verschillende rollen geven
        afspraak.voegBetrokkenheidToe(
                gebruiker,
                BetrokkenRol.BETROKKENE
        );

        afspraak.voegBetrokkenheidToe(
                gebruiker,
                BetrokkenRol.GOEDKEURDER
        );

        // 5. Opslaan en opnieuw ophalen
        entityManager.persist(afspraak);

        UUID afspraakId = afspraak.getId();

        entityManager.flush();
        entityManager.clear();

        OperationeleAfspraak opgeslagenAfspraak =
                entityManager.find(
                        OperationeleAfspraak.class,
                        afspraakId
                );

        // 6. Controleren of beide rollen zijn opgeslagen
        assertNotNull(opgeslagenAfspraak);

        assertEquals(
                2,
                opgeslagenAfspraak.getBetrokkenheden().size()
        );

        boolean isBetrokkene = opgeslagenAfspraak
                .getBetrokkenheden()
                .stream()
                .anyMatch(b ->
                        b.getRol() == BetrokkenRol.BETROKKENE
                                && gebruikerId.equals(b.getGebruiker().getId())
                );

        boolean isGoedkeurder = opgeslagenAfspraak
                .getBetrokkenheden()
                .stream()
                .anyMatch(b ->
                        b.getRol() == BetrokkenRol.GOEDKEURDER
                                && gebruikerId.equals(b.getGebruiker().getId())
                );

        assertTrue(isBetrokkene);
        assertTrue(isGoedkeurder);
    }





}
