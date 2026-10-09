package nl.outokumpu.afspraken.controller;

import nl.outokumpu.afspraken.dto.response.AfdelingResponse;
import nl.outokumpu.afspraken.dto.response.GebruikerResponse;
import nl.outokumpu.afspraken.service.GebruikerService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GebruikerControllerTest {

    private GebruikerService gebruikerService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        gebruikerService =
                mock(GebruikerService.class);

        GebruikerController controller =
                new GebruikerController(
                        gebruikerService
                );

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(controller)
                        .build();
    }

    @Test
    void haaltAlleGebruikersOp() throws Exception {

        UUID afdelingId =
                UUID.randomUUID();

        UUID eersteGebruikerId =
                UUID.randomUUID();

        UUID tweedeGebruikerId =
                UUID.randomUUID();

        AfdelingResponse afdeling =
                new AfdelingResponse(
                        afdelingId,
                        "Capacity Planning"
                );

        GebruikerResponse eersteGebruiker =
                new GebruikerResponse(
                        eersteGebruikerId,
                        "Jan Jansen",
                        "jan.jansen@example.com",
                        afdeling
                );

        GebruikerResponse tweedeGebruiker =
                new GebruikerResponse(
                        tweedeGebruikerId,
                        "Piet de Vries",
                        "piet.devries@example.com",
                        afdeling
                );

        when(
                gebruikerService.vindAlleGebruikers()
        ).thenReturn(
                List.of(
                        eersteGebruiker,
                        tweedeGebruiker
                )
        );

        mockMvc.perform(
                        get("/api/gebruikers")
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        content()
                                .contentTypeCompatibleWith(
                                        "application/json"
                                )
                )
                .andExpect(
                        jsonPath("$[0].id")
                                .value(
                                        eersteGebruikerId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$[0].naam")
                                .value(
                                        "Jan Jansen"
                                )
                )
                .andExpect(
                        jsonPath("$[0].email")
                                .value(
                                        "jan.jansen@example.com"
                                )
                )
                .andExpect(
                        jsonPath("$[0].afdeling.id")
                                .value(
                                        afdelingId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$[0].afdeling.naam")
                                .value(
                                        "Capacity Planning"
                                )
                )
                .andExpect(
                        jsonPath("$[1].id")
                                .value(
                                        tweedeGebruikerId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$[1].naam")
                                .value(
                                        "Piet de Vries"
                                )
                );

        verify(
                gebruikerService
        ).vindAlleGebruikers();

        verifyNoMoreInteractions(
                gebruikerService
        );
    }

    @Test
    void geeftLegeLijstTerugWanneerGeenGebruikersBestaan()
            throws Exception {

        when(
                gebruikerService.vindAlleGebruikers()
        ).thenReturn(
                List.of()
        );

        mockMvc.perform(
                        get("/api/gebruikers")
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$")
                                .isEmpty()
                );

        verify(
                gebruikerService
        ).vindAlleGebruikers();
    }
}