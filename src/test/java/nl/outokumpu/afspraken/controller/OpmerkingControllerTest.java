package nl.outokumpu.afspraken.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import nl.outokumpu.afspraken.dto.request.CreateOpmerkingRequest;
import nl.outokumpu.afspraken.dto.response.AfdelingResponse;
import nl.outokumpu.afspraken.dto.response.GebruikerResponse;
import nl.outokumpu.afspraken.dto.response.OpmerkingResponse;

import nl.outokumpu.afspraken.enums.GebruikersRol;

import nl.outokumpu.afspraken.security.GebruikerPrincipal;

import nl.outokumpu.afspraken.service.OpmerkingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OpmerkingControllerTest {

    private OpmerkingService opmerkingService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        opmerkingService =
                mock(
                        OpmerkingService.class
                );

        OpmerkingController controller =
                new OpmerkingController(
                        opmerkingService
                );

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(
                                controller
                        )
                        .build();

        objectMapper =
                new ObjectMapper();
    }

    @Test
    void haaltOpmerkingenVanAfspraakOp()
            throws Exception {

        UUID afspraakId =
                UUID.randomUUID();

        UUID gebruikerId =
                UUID.randomUUID();

        OpmerkingResponse response =
                new OpmerkingResponse(
                        UUID.randomUUID(),
                        new GebruikerResponse(
                                gebruikerId,
                                "Felix",
                                "felix@example.com",
                                new AfdelingResponse(
                                        UUID.randomUUID(),
                                        "Planning"
                                )
                        ),
                        "Eerste opmerking",
                        Instant.parse(
                                "2026-10-11T08:00:00Z"
                        )
                );

        when(
                opmerkingService
                        .vindOpmerkingen(
                                afspraakId
                        )
        ).thenReturn(
                List.of(
                        response
                )
        );

        mockMvc.perform(
                        get(
                                "/api/afspraken/{afspraakId}/opmerkingen",
                                afspraakId
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$[0].tekst")
                                .value(
                                        "Eerste opmerking"
                                )
                )
                .andExpect(
                        jsonPath("$[0].auteur.naam")
                                .value(
                                        "Felix"
                                )
                );
    }

    @Test
    void voegtOpmerkingToeMetIngelogdeGebruiker()
            throws Exception {

        UUID afspraakId =
                UUID.randomUUID();

        UUID gebruikerId =
                UUID.randomUUID();

        CreateOpmerkingRequest request =
                new CreateOpmerkingRequest(
                        "Nieuwe terugkoppeling"
                );

        OpmerkingResponse response =
                new OpmerkingResponse(
                        UUID.randomUUID(),
                        new GebruikerResponse(
                                gebruikerId,
                                "Felix",
                                "felix@example.com",
                                new AfdelingResponse(
                                        UUID.randomUUID(),
                                        "Planning"
                                )
                        ),
                        "Nieuwe terugkoppeling",
                        Instant.now()
                );

        when(
                opmerkingService
                        .voegOpmerkingToe(
                                eq(afspraakId),
                                eq(gebruikerId),
                                any(
                                        CreateOpmerkingRequest.class
                                )
                        )
        ).thenReturn(
                response
        );

        mockMvc.perform(
                        post(
                                "/api/afspraken/{afspraakId}/opmerkingen",
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
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        request
                                                )
                                )
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        jsonPath("$.tekst")
                                .value(
                                        "Nieuwe terugkoppeling"
                                )
                )
                .andExpect(
                        jsonPath("$.auteur.email")
                                .value(
                                        "felix@example.com"
                                )
                );

        verify(
                opmerkingService
        ).voegOpmerkingToe(
                eq(afspraakId),
                eq(gebruikerId),
                any(
                        CreateOpmerkingRequest.class
                )
        );
    }

    @Test
    void legeOpmerkingGeeftBadRequest()
            throws Exception {

        UUID afspraakId =
                UUID.randomUUID();

        UUID gebruikerId =
                UUID.randomUUID();

        String json = """
                {
                  "tekst": "   "
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/afspraken/{afspraakId}/opmerkingen",
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
                        status().isBadRequest()
                );

        verifyNoInteractions(
                opmerkingService
        );
    }

    private Authentication authenticatieVoor(
            UUID gebruikerId
    ) {

        GebruikerPrincipal principal =
                new GebruikerPrincipal(
                        gebruikerId,
                        "felix@example.com",
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