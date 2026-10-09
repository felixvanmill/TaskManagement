package nl.outokumpu.afspraken.exception;

import nl.outokumpu.afspraken.controller.AfspraakController;
import nl.outokumpu.afspraken.service.AfspraakService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

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
                        .setControllerAdvice(
                                new GlobalExceptionHandler()
                        )
                        .build();
    }

    @Test
    void geeftBadRequestBijServiceFout()
            throws Exception {

        UUID afspraakId =
                UUID.randomUUID();

        when(
                afspraakService.vindAfspraak(
                        afspraakId
                )
        ).thenThrow(
                new IllegalArgumentException(
                        "Afspraak niet gevonden"
                )
        );

        mockMvc.perform(
                        get(
                                "/api/afspraken/{id}",
                                afspraakId
                        )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Bad Request")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Afspraak niet gevonden"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/api/afspraken/"
                                                + afspraakId
                                )
                )
                .andExpect(
                        jsonPath("$.timestamp")
                                .exists()
                );
    }

    @Test
    void geeftValidatiefoutenPerVeld()
            throws Exception {

        UUID gebruikerId =
                UUID.randomUUID();

        /*
         * Vrijwel alle verplichte velden ontbreken.
         */
        String json = """
                {
                  "titel": "",
                  "beschrijving": "",
                  "reden": ""
                }
                """;

        mockMvc.perform(
                        post("/api/afspraken")
                                .header(
                                        "X-Gebruiker-Id",
                                        gebruikerId
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(json)
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Validatie mislukt"
                                )
                )
                .andExpect(
                        jsonPath(
                                "$.veldFouten.titel"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$.veldFouten.beschrijving"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$.veldFouten.reden"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$.veldFouten.type"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$.veldFouten.ingangsdatum"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$.veldFouten.deadline"
                        ).exists()
                );

        verifyNoInteractions(
                afspraakService
        );
    }

    @Test
    void geeftBadRequestWanneerGebruikerHeaderOntbreekt()
            throws Exception {

        String json = """
                {
                  "titel": "Titel",
                  "beschrijving": "Beschrijving",
                  "reden": "Reden",
                  "type": "ALGEMEEN",
                  "ingangsdatum": "2026-10-10",
                  "deadline": "2026-10-31",
                  "verantwoordelijkeId": "%s",
                  "eersteProcesstapNaam": "Beoordelen",
                  "eersteProcesstapDeadline": "2026-10-20",
                  "betrokkeneIds": [],
                  "goedkeurderIds": [],
                  "afdelingIds": []
                }
                """.formatted(
                UUID.randomUUID()
        );

        mockMvc.perform(
                        post("/api/afspraken")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(json)
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Verplichte header ontbreekt: X-Gebruiker-Id"
                                )
                );

        verifyNoInteractions(
                afspraakService
        );
    }

    @Test
    void geeftBadRequestBijOngeldigeEnumwaarde()
            throws Exception {

        mockMvc.perform(
                        get("/api/afspraken/filter")
                                .param(
                                        "status",
                                        "BESTAAT_NIET"
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Ongeldige waarde voor parameter: status"
                                )
                );

        verifyNoInteractions(
                afspraakService
        );
    }
}