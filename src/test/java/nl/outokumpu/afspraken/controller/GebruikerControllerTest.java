package nl.outokumpu.afspraken.controller;

import nl.outokumpu.afspraken.dto.response.AfdelingResponse;
import nl.outokumpu.afspraken.dto.response.GebruikerResponse;
import nl.outokumpu.afspraken.service.GebruikerService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Test
    void haaltGebruikersVoorBeheerOp()
            throws Exception {

        when(
                gebruikerService
                        .vindAlleGebruikersVoorBeheer()
        ).thenReturn(
                List.of()
        );

        mockMvc.perform(
                        get(
                                "/api/gebruikers/beheer"
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$")
                                .isArray()
                );

        verify(
                gebruikerService
        ).vindAlleGebruikersVoorBeheer();
    }

    @Test
    void wijzigtGebruikerToegang()
            throws Exception {

        UUID gebruikerId =
                UUID.randomUUID();

        String json = """
            {
              "rol": "BEHEERDER",
              "actief": true,
              "nieuwWachtwoord": "NieuwWachtwoord123!"
            }
            """;

        mockMvc.perform(
                        put(
                                "/api/gebruikers/{id}/toegang",
                                gebruikerId
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(json)
                )
                .andExpect(
                        status().isOk()
                );

        verify(
                gebruikerService
        ).wijzigToegang(
                eq(gebruikerId),
                any()
        );
    }
}