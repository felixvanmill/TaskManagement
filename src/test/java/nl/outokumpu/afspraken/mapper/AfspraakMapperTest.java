package nl.outokumpu.afspraken.mapper;

import nl.outokumpu.afspraken.dto.response.AfspraakDetailResponse;
import nl.outokumpu.afspraken.dto.response.AfdelingResponse;
import nl.outokumpu.afspraken.dto.response.GebruikerResponse;
import nl.outokumpu.afspraken.entity.Afdeling;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.entity.OperationeleAfspraak;
import nl.outokumpu.afspraken.enums.AfspraakType;
import nl.outokumpu.afspraken.enums.Beslissing;
import nl.outokumpu.afspraken.enums.BetrokkenRol;
import nl.outokumpu.afspraken.enums.ProcesstapStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AfspraakMapperTest {

    private AfspraakMapper afspraakMapper;

    @BeforeEach
    void setUp() {
        afspraakMapper = new AfspraakMapper();
    }

    @Test
    void zetAlgemeneAfspraakOmNaarDetailResponse() {

        Afdeling planning =
                new Afdeling("Capacity Planning");

        Afdeling productie =
                new Afdeling("Productie");

        Gebruiker initiatiefnemer =
                new Gebruiker(
                        "Initiatiefnemer",
                        "initiatiefnemer@example.com",
                        planning
                );

        Gebruiker verantwoordelijke =
                new Gebruiker(
                        "Verantwoordelijke",
                        "verantwoordelijke@example.com",
                        productie
                );

        Gebruiker betrokkene =
                new Gebruiker(
                        "Betrokkene",
                        "betrokkene@example.com",
                        productie
                );

        Gebruiker goedkeurder =
                new Gebruiker(
                        "Goedkeurder",
                        "goedkeurder@example.com",
                        planning
                );

        OperationeleAfspraak afspraak =
                new OperationeleAfspraak(
                        "Operationele afspraak",
                        "Beschrijving",
                        "Reden",
                        "Achtergrond",
                        "Aanname",
                        LocalDate.of(2026, 10, 3),
                        LocalDate.of(2026, 10, 31),
                        initiatiefnemer
                );

        afspraak.voegAfdelingToe(productie);

        afspraak.voegBetrokkenheidToe(
                betrokkene,
                BetrokkenRol.BETROKKENE
        );

        afspraak.voegBetrokkenheidToe(
                goedkeurder,
                BetrokkenRol.GOEDKEURDER
        );

        afspraak.voegProcesstapToe(
                "Eerste beoordeling",
                1,
                LocalDate.of(2026, 10, 15),
                verantwoordelijke
        );

        afspraak.voegBevestigingToe(
                goedkeurder
        );

        AfspraakDetailResponse response =
                afspraakMapper.naarDetailResponse(
                        afspraak
                );

        assertEquals(
                "Operationele afspraak",
                response.titel()
        );

        assertEquals(
                AfspraakType.ALGEMEEN,
                response.type()
        );

        assertEquals(
                "Initiatiefnemer",
                response.initiatiefnemer().naam()
        );

        assertEquals(
                1,
                response.betrokkenAfdelingen().size()
        );

        assertEquals(
                "Productie",
                response.betrokkenAfdelingen()
                        .get(0)
                        .naam()
        );

        assertEquals(
                1,
                response.betrokkenGebruikers().size()
        );

        assertEquals(
                "Betrokkene",
                response.betrokkenGebruikers()
                        .get(0)
                        .naam()
        );

        assertEquals(
                1,
                response.goedkeurders().size()
        );

        assertEquals(
                "Goedkeurder",
                response.goedkeurders()
                        .get(0)
                        .naam()
        );

        assertEquals(
                1,
                response.processtappen().size()
        );

        assertEquals(
                "Eerste beoordeling",
                response.processtappen()
                        .get(0)
                        .naam()
        );

        assertEquals(
                ProcesstapStatus.NIET_GESTART,
                response.processtappen()
                        .get(0)
                        .status()
        );

        assertEquals(
                "Verantwoordelijke",
                response.processtappen()
                        .get(0)
                        .verantwoordelijke()
                        .naam()
        );

        assertEquals(
                1,
                response.bevestigingen().size()
        );

        assertEquals(
                "Goedkeurder",
                response.bevestigingen()
                        .get(0)
                        .gebruiker()
                        .naam()
        );

        assertFalse(
                response.bevestigingen()
                        .get(0)
                        .gezien()
        );

        assertEquals(
                Beslissing.GEEN,
                response.bevestigingen()
                        .get(0)
                        .beslissing()
        );

        assertTrue(
                response.wijzigingen().isEmpty()
        );

        assertNull(
                response.capaciteitswissel()
        );

        assertNull(
                response.orderverplaatsing()
        );
    }

    @Test
    void gebruikersmappingVereistEenAfdeling() {
        Gebruiker gebruiker = new Gebruiker("Felix", "felix@example.com", null);

        assertThrows(
                NullPointerException.class,
                () -> afspraakMapper.naarGebruikerResponse(gebruiker)
        );
    }

    @Test
    void gebruikersmappingVereistEenGebruiker() {
        assertThrows(
                NullPointerException.class,
                () -> afspraakMapper.naarGebruikerResponse(null)
        );
        assertThrows(
                NullPointerException.class,
                () -> afspraakMapper.naarGebruikerResponse(null, null)
        );
    }

    @Test
    void gebruikersmappingKanEenOntbrekendeAfdelingresponseBehouden() {
        UUID gebruikerId = UUID.randomUUID();
        Gebruiker gebruiker = mock(Gebruiker.class);
        when(gebruiker.getId()).thenReturn(gebruikerId);
        when(gebruiker.getNaam()).thenReturn("Felix");
        when(gebruiker.getEmail()).thenReturn("felix@example.com");

        GebruikerResponse resultaat = afspraakMapper.naarGebruikerResponse(gebruiker, null);

        assertEquals(gebruikerId, resultaat.id());
        assertEquals("Felix", resultaat.naam());
        assertEquals("felix@example.com", resultaat.email());
        assertNull(resultaat.afdeling());
    }

    @Test
    void gebruikersmappingBehoudtDeMeegegevenAfdelingresponse() {
        Gebruiker gebruiker = new Gebruiker("Felix", "felix@example.com", null);
        AfdelingResponse afdeling = new AfdelingResponse(UUID.randomUUID(), "Planning");

        GebruikerResponse resultaat = afspraakMapper.naarGebruikerResponse(gebruiker, afdeling);

        assertEquals(gebruiker.getId(), resultaat.id());
        assertEquals("Felix", resultaat.naam());
        assertEquals("felix@example.com", resultaat.email());
        assertSame(afdeling, resultaat.afdeling());
    }
}