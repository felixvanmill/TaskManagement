package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.response.GebruikerResponse;
import nl.outokumpu.afspraken.entity.Afdeling;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.repository.GebruikerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GebruikerServiceTest {

    private GebruikerRepository gebruikerRepository;
    private GebruikerService gebruikerService;

    @BeforeEach
    void setUp() {
        gebruikerRepository = mock(GebruikerRepository.class);
        gebruikerService = new GebruikerService(gebruikerRepository);
    }

    @Test
    void geeftAlleGebruikersAlsResponseTerug() {

        UUID afdelingId = UUID.randomUUID();
        UUID gebruikerId = UUID.randomUUID();

        Afdeling afdeling = mock(Afdeling.class);
        when(afdeling.getId()).thenReturn(afdelingId);
        when(afdeling.getNaam()).thenReturn("Capacity Planning");

        Gebruiker gebruiker = mock(Gebruiker.class);
        when(gebruiker.getId()).thenReturn(gebruikerId);
        when(gebruiker.getNaam()).thenReturn("Capacity Planner");
        when(gebruiker.getEmail())
                .thenReturn("planner@example.com");
        when(gebruiker.getAfdeling()).thenReturn(afdeling);

        when(gebruikerRepository.findAll())
                .thenReturn(List.of(gebruiker));

        List<GebruikerResponse> resultaat =
                gebruikerService.vindAlleGebruikers();

        assertEquals(1, resultaat.size());

        GebruikerResponse response = resultaat.get(0);

        assertEquals(gebruikerId, response.id());
        assertEquals("Capacity Planner", response.naam());
        assertEquals("planner@example.com", response.email());

        assertEquals(
                afdelingId,
                response.afdeling().id()
        );

        assertEquals(
                "Capacity Planning",
                response.afdeling().naam()
        );

        verify(gebruikerRepository, times(1))
                .findAll();
    }
}