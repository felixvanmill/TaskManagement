package nl.outokumpu.afspraken.controller;

import nl.outokumpu.afspraken.enums.Beslissing;
import nl.outokumpu.afspraken.enums.GebruikersRol;
import nl.outokumpu.afspraken.security.GebruikerPrincipal;
import nl.outokumpu.afspraken.service.BevestigingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BevestigingControllerTest {

    private BevestigingService bevestigingService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        bevestigingService =
                mock(BevestigingService.class);

        BevestigingController controller =
                new BevestigingController(
                        bevestigingService
                );

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(controller)
                        .build();
    }

    @Test
    void markeertAfspraakAlsGezien()
            throws Exception {

        UUID afspraakId =
                UUID.randomUUID();

        UUID gebruikerId =
                UUID.randomUUID();

        mockMvc.perform(
                        put(
                                "/api/afspraken/{afspraakId}/bevestiging/gezien",
                                afspraakId
                        )
                                .principal(
                                        authenticatieVoor(
                                                gebruikerId
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                );

        verify(
                bevestigingService
        ).markeerGezien(
                afspraakId,
                gebruikerId
        );
    }

    @Test
    void registreertGoedkeuring()
            throws Exception {

        UUID afspraakId =
                UUID.randomUUID();

        UUID gebruikerId =
                UUID.randomUUID();

        String json = """
                {
                  "beslissing": "GOEDGEKEURD"
                }
                """;

        mockMvc.perform(
                        put(
                                "/api/afspraken/{afspraakId}/bevestiging/beslissing",
                                afspraakId
                        )
                                .principal(
                                        authenticatieVoor(
                                                gebruikerId
                                        )
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
                bevestigingService
        ).registreerBeslissing(
                afspraakId,
                gebruikerId,
                Beslissing.GOEDGEKEURD
        );
    }

    @Test
    void registreertAfwijzing()
            throws Exception {

        UUID afspraakId =
                UUID.randomUUID();

        UUID gebruikerId =
                UUID.randomUUID();

        String json = """
                {
                  "beslissing": "AFGEWEZEN"
                }
                """;

        mockMvc.perform(
                        put(
                                "/api/afspraken/{afspraakId}/bevestiging/beslissing",
                                afspraakId
                        )
                                .principal(
                                        authenticatieVoor(
                                                gebruikerId
                                        )
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
                bevestigingService
        ).registreerBeslissing(
                afspraakId,
                gebruikerId,
                Beslissing.AFGEWEZEN
        );
    }

    private Authentication authenticatieVoor(
            UUID gebruikerId
    ) {

        GebruikerPrincipal principal =
                new GebruikerPrincipal(
                        gebruikerId,
                        "bevestiging@example.com",
                        "$2a$10$testhash",
                        GebruikersRol.GEBRUIKER,
                        true
                );

        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
    }
}