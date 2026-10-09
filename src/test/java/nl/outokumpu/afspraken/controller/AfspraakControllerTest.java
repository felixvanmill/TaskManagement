package nl.outokumpu.afspraken.controller;

import nl.outokumpu.afspraken.dto.request.AfspraakFilterRequest;
import nl.outokumpu.afspraken.enums.AfspraakStatus;
import nl.outokumpu.afspraken.enums.AfspraakType;
import nl.outokumpu.afspraken.enums.GebruikersRol;
import nl.outokumpu.afspraken.security.GebruikerPrincipal;
import nl.outokumpu.afspraken.service.AfspraakService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AfspraakControllerTest {

    private AfspraakService afspraakService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        afspraakService =
                mock(AfspraakService.class);

        AfspraakController controller =
                new AfspraakController(
                        afspraakService
                );

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(controller)
                        .build();
    }

    @Test
    void maaktAfspraakAan() throws Exception {

        UUID gebruikerId =
                UUID.randomUUID();

        UUID verantwoordelijkeId =
                UUID.randomUUID();

        String json = """
                {
                  "titel": "Testafspraak",
                  "beschrijving": "Beschrijving",
                  "reden": "Reden",
                  "achtergrond": "Achtergrond",
                  "aannames": "Aannames",
                  "type": "ALGEMEEN",
                  "ingangsdatum": "2026-10-10",
                  "deadline": "2026-10-31",
                  "verantwoordelijkeId": "%s",
                  "eersteProcesstapNaam": "Beoordelen",
                  "eersteProcesstapDeadline": "2026-10-20",
                  "betrokkeneIds": [],
                  "goedkeurderIds": [],
                  "afdelingIds": [],
                  "capaciteitswissel": null,
                  "orderverplaatsing": null
                }
                """.formatted(
                verantwoordelijkeId
        );

        mockMvc.perform(
                        post("/api/afspraken")
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
                        status().isCreated()
                );

        verify(
                afspraakService
        ).createAfspraak(
                any(),
                eq(gebruikerId)
        );
    }

    @Test
    void haaltAfspraakDetailOp()
            throws Exception {

        UUID afspraakId =
                UUID.randomUUID();

        mockMvc.perform(
                        get(
                                "/api/afspraken/{id}",
                                afspraakId
                        )
                )
                .andExpect(
                        status().isOk()
                );

        verify(
                afspraakService
        ).vindAfspraak(
                afspraakId
        );
    }

    @Test
    void haaltAlleAfsprakenOp()
            throws Exception {

        when(
                afspraakService
                        .vindAlleAfspraken()
        ).thenReturn(
                List.of()
        );

        mockMvc.perform(
                        get("/api/afspraken")
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$")
                                .isArray()
                );

        verify(
                afspraakService
        ).vindAlleAfspraken();
    }

    @Test
    void haaltActieveAfsprakenOp()
            throws Exception {

        when(
                afspraakService
                        .vindActieveAfspraken()
        ).thenReturn(
                List.of()
        );

        mockMvc.perform(
                        get("/api/afspraken/actief")
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$")
                                .isArray()
                );

        verify(
                afspraakService
        ).vindActieveAfspraken();
    }

    @Test
    void haaltArchiefOp()
            throws Exception {

        when(
                afspraakService
                        .vindAfgerondeAfspraken()
        ).thenReturn(
                List.of()
        );

        mockMvc.perform(
                        get("/api/afspraken/archief")
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$")
                                .isArray()
                );

        verify(
                afspraakService
        ).vindAfgerondeAfspraken();
    }

    @Test
    void zoektAfspraken()
            throws Exception {

        when(
                afspraakService
                        .zoekAfspraken(
                                "capaciteit"
                        )
        ).thenReturn(
                List.of()
        );

        mockMvc.perform(
                        get("/api/afspraken/zoeken")
                                .param(
                                        "zoekterm",
                                        "capaciteit"
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
                afspraakService
        ).zoekAfspraken(
                "capaciteit"
        );
    }

    @Test
    void filtertAfspraken()
            throws Exception {

        UUID verantwoordelijkeId =
                UUID.randomUUID();

        UUID afdelingId =
                UUID.randomUUID();

        AfspraakFilterRequest verwachtFilter =
                new AfspraakFilterRequest(
                        AfspraakType.CAPACITEITSWISSEL,
                        AfspraakStatus.IN_BEHANDELING,
                        verantwoordelijkeId,
                        afdelingId,
                        "Tornio",
                        LocalDate.of(
                                2026,
                                10,
                                1
                        ),
                        LocalDate.of(
                                2026,
                                10,
                                31
                        )
                );

        when(
                afspraakService
                        .filterAfspraken(
                                verwachtFilter
                        )
        ).thenReturn(
                List.of()
        );

        mockMvc.perform(
                        get("/api/afspraken/filter")
                                .param(
                                        "type",
                                        "CAPACITEITSWISSEL"
                                )
                                .param(
                                        "status",
                                        "IN_BEHANDELING"
                                )
                                .param(
                                        "verantwoordelijkeId",
                                        verantwoordelijkeId.toString()
                                )
                                .param(
                                        "afdelingId",
                                        afdelingId.toString()
                                )
                                .param(
                                        "fabriek",
                                        "Tornio"
                                )
                                .param(
                                        "periodeVan",
                                        "2026-10-01"
                                )
                                .param(
                                        "periodeTot",
                                        "2026-10-31"
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
                afspraakService
        ).filterAfspraken(
                verwachtFilter
        );
    }

    @Test
    void wijzigtAfspraak()
            throws Exception {

        UUID afspraakId =
                UUID.randomUUID();

        UUID gebruikerId =
                UUID.randomUUID();

        String json = """
                {
                  "titel": "Nieuwe titel",
                  "beschrijving": "Nieuwe beschrijving",
                  "reden": "Nieuwe reden",
                  "achtergrond": "Achtergrond",
                  "aannames": "Aannames",
                  "ingangsdatum": "2026-10-10",
                  "deadline": "2026-11-01",
                  "capaciteitswissel": null,
                  "orderverplaatsing": null
                }
                """;

        mockMvc.perform(
                        put(
                                "/api/afspraken/{id}",
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
                afspraakService
        ).updateAfspraak(
                eq(afspraakId),
                any(),
                eq(gebruikerId)
        );
    }

    @Test
    void wijzigtGoedkeurders()
            throws Exception {

        UUID afspraakId =
                UUID.randomUUID();

        UUID gebruikerId =
                UUID.randomUUID();

        UUID goedkeurderId =
                UUID.randomUUID();

        String json = """
                {
                  "goedkeurderIds": [
                    "%s"
                  ]
                }
                """.formatted(
                goedkeurderId
        );

        mockMvc.perform(
                        put(
                                "/api/afspraken/{id}/goedkeurders",
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
                afspraakService
        ).wijzigGoedkeurders(
                eq(afspraakId),
                any(),
                eq(gebruikerId)
        );
    }

    private Authentication authenticatieVoor(
            UUID gebruikerId
    ) {

        GebruikerPrincipal principal =
                new GebruikerPrincipal(
                        gebruikerId,
                        "user@example.com",
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