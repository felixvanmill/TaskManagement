package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.request.UpdateGebruikerToegangRequest;
import nl.outokumpu.afspraken.dto.response.GebruikerBeheerResponse;
import nl.outokumpu.afspraken.dto.response.GebruikerResponse;
import nl.outokumpu.afspraken.entity.Afdeling;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.enums.GebruikersRol;
import nl.outokumpu.afspraken.mapper.AfspraakMapper;
import nl.outokumpu.afspraken.repository.GebruikerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GebruikerServiceTest {

    private GebruikerRepository gebruikerRepository;
    private GebruikerService gebruikerService;

    @BeforeEach
    void setUp() {

        gebruikerRepository =
                mock(GebruikerRepository.class);

        gebruikerService =
                new GebruikerService(
                        gebruikerRepository,
                        new AfspraakMapper()
                );
    }

    @Test
    void geeftAlleGebruikersAlsResponseTerug() {

        UUID afdelingId =
                UUID.randomUUID();

        UUID gebruikerId =
                UUID.randomUUID();

        Afdeling afdeling =
                mock(Afdeling.class);

        when(
                afdeling.getId()
        ).thenReturn(
                afdelingId
        );

        when(
                afdeling.getNaam()
        ).thenReturn(
                "Capacity Planning"
        );

        Gebruiker gebruiker =
                mock(Gebruiker.class);

        when(
                gebruiker.getId()
        ).thenReturn(
                gebruikerId
        );

        when(
                gebruiker.getNaam()
        ).thenReturn(
                "Capacity Planner"
        );

        when(
                gebruiker.getEmail()
        ).thenReturn(
                "planner@example.com"
        );

        when(
                gebruiker.getAfdeling()
        ).thenReturn(
                afdeling
        );

        when(
                gebruikerRepository.findAll()
        ).thenReturn(
                List.of(
                        gebruiker
                )
        );

        List<GebruikerResponse> resultaat =
                gebruikerService
                        .vindAlleGebruikers();

        assertEquals(
                1,
                resultaat.size()
        );

        GebruikerResponse response =
                resultaat.get(0);

        assertEquals(
                gebruikerId,
                response.id()
        );

        assertEquals(
                "Capacity Planner",
                response.naam()
        );

        assertEquals(
                "planner@example.com",
                response.email()
        );

        assertEquals(
                afdelingId,
                response.afdeling().id()
        );
    }

    @Test
    void geeftBeheerinformatieTerug() {

        UUID afdelingId =
                UUID.randomUUID();

        Afdeling afdeling =
                mock(Afdeling.class);

        when(
                afdeling.getId()
        ).thenReturn(
                afdelingId
        );

        when(
                afdeling.getNaam()
        ).thenReturn(
                "Capacity Planning"
        );

        Gebruiker gebruiker =
                new Gebruiker(
                        "Admin",
                        "admin@example.com",
                        afdeling
                );

        gebruiker.configureerToegang(
                "$2a$10$hash",
                GebruikersRol.BEHEERDER,
                true
        );

        when(
                gebruikerRepository.findAll()
        ).thenReturn(
                List.of(
                        gebruiker
                )
        );

        List<GebruikerBeheerResponse> resultaat =
                gebruikerService
                        .vindAlleGebruikersVoorBeheer();

        assertEquals(
                1,
                resultaat.size()
        );

        assertEquals(
                GebruikersRol.BEHEERDER,
                resultaat.get(0).rol()
        );

        assertTrue(
                resultaat.get(0).actief()
        );

        assertTrue(
                resultaat.get(0)
                        .lokaleToegang()
        );
    }

    @Test
    void beheerderKanRolEnToegangWijzigen() {

        UUID gebruikerId =
                UUID.randomUUID();

        UUID afdelingId =
                UUID.randomUUID();

        Afdeling afdeling =
                mock(Afdeling.class);

        when(
                afdeling.getId()
        ).thenReturn(
                afdelingId
        );

        when(
                afdeling.getNaam()
        ).thenReturn(
                "Capacity Planning"
        );

        Gebruiker gebruiker =
                new Gebruiker(
                        "Gebruiker",
                        "user@example.com",
                        afdeling
                );

        when(
                gebruikerRepository.findById(
                        gebruikerId
                )
        ).thenReturn(
                Optional.of(
                        gebruiker
                )
        );

        when(
                gebruikerRepository.save(
                        gebruiker
                )
        ).thenReturn(
                gebruiker
        );

        UpdateGebruikerToegangRequest request =
                new UpdateGebruikerToegangRequest(
                        GebruikersRol.BEHEERDER,
                        false
                );

        GebruikerBeheerResponse response =
                gebruikerService.wijzigToegang(
                        gebruikerId,
                        request
                );

        assertEquals(
                GebruikersRol.BEHEERDER,
                gebruiker.getRol()
        );

        assertFalse(
                gebruiker.isActief()
        );

        assertEquals(
                GebruikersRol.BEHEERDER,
                response.rol()
        );

        assertFalse(
                response.actief()
        );

        verify(
                gebruikerRepository
        ).save(
                gebruiker
        );
    }

    @Test
    void onbekendeGebruikerWordtGeweigerd() {

        UUID gebruikerId =
                UUID.randomUUID();

        when(
                gebruikerRepository.findById(
                        gebruikerId
                )
        ).thenReturn(
                Optional.empty()
        );

        UpdateGebruikerToegangRequest request =
                new UpdateGebruikerToegangRequest(
                        GebruikersRol.GEBRUIKER,
                        true
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                gebruikerService
                                        .wijzigToegang(
                                                gebruikerId,
                                                request
                                        )
                );

        assertEquals(
                "Gebruiker niet gevonden",
                exception.getMessage()
        );
    }
}