package nl.outokumpu.afspraken.controller;

import nl.outokumpu.afspraken.dto.request.UpdateToewijzingRequest;
import nl.outokumpu.afspraken.enums.AfspraakStatus;
import nl.outokumpu.afspraken.enums.UitvoeringsStatus;
import nl.outokumpu.afspraken.service.WorkflowService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkflowControllerTest {

    private WorkflowService workflowService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        workflowService =
                mock(WorkflowService.class);

        WorkflowController controller =
                new WorkflowController(
                        workflowService
                );

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(controller)
                        .build();
    }

    @Test
    void wijzigtToewijzing() throws Exception {

        UUID afspraakId =
                UUID.randomUUID();

        UUID gebruikerId =
                UUID.randomUUID();

        UUID processtapId =
                UUID.randomUUID();

        UUID verantwoordelijkeId =
                UUID.randomUUID();

        UUID betrokkeneId =
                UUID.randomUUID();

        UUID afdelingId =
                UUID.randomUUID();

        String json = """
                {
                  "betrokkeneIds": [
                    "%s"
                  ],
                  "afdelingIds": [
                    "%s"
                  ],
                  "processtapId": "%s",
                  "verantwoordelijkeId": "%s"
                }
                """.formatted(
                betrokkeneId,
                afdelingId,
                processtapId,
                verantwoordelijkeId
        );

        mockMvc.perform(
                        put(
                                "/api/afspraken/{id}/toewijzing",
                                afspraakId
                        )
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
                        status().isOk()
                );

        verify(
                workflowService
        ).updateToewijzing(
                eq(afspraakId),
                any(UpdateToewijzingRequest.class),
                eq(gebruikerId)
        );
    }

    @Test
    void startProcesstap() throws Exception {

        UUID processtapId =
                UUID.randomUUID();

        UUID gebruikerId =
                UUID.randomUUID();

        mockMvc.perform(
                        put(
                                "/api/processtappen/{id}/starten",
                                processtapId
                        )
                                .header(
                                        "X-Gebruiker-Id",
                                        gebruikerId
                                )
                )
                .andExpect(
                        status().isOk()
                );

        verify(
                workflowService
        ).startProcesstap(
                processtapId,
                gebruikerId
        );
    }

    @Test
    void rondProcesstapAf() throws Exception {

        UUID processtapId =
                UUID.randomUUID();

        UUID gebruikerId =
                UUID.randomUUID();

        mockMvc.perform(
                        put(
                                "/api/processtappen/{id}/afronden",
                                processtapId
                        )
                                .header(
                                        "X-Gebruiker-Id",
                                        gebruikerId
                                )
                )
                .andExpect(
                        status().isOk()
                );

        verify(
                workflowService
        ).rondProcesstapAf(
                processtapId,
                gebruikerId
        );
    }

    @Test
    void wijzigtAfspraakStatus() throws Exception {

        UUID afspraakId =
                UUID.randomUUID();

        UUID gebruikerId =
                UUID.randomUUID();

        String json = """
                {
                  "status": "IN_BEHANDELING"
                }
                """;

        mockMvc.perform(
                        put(
                                "/api/afspraken/{id}/status",
                                afspraakId
                        )
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
                        status().isOk()
                );

        verify(
                workflowService
        ).wijzigAfspraakStatus(
                afspraakId,
                AfspraakStatus.IN_BEHANDELING,
                gebruikerId
        );
    }

    @Test
    void wijzigtUitvoeringsstatus() throws Exception {

        UUID afspraakId =
                UUID.randomUUID();

        UUID gebruikerId =
                UUID.randomUUID();

        String json = """
                {
                  "status": "IN_UITVOERING"
                }
                """;

        mockMvc.perform(
                        put(
                                "/api/afspraken/{id}/uitvoeringsstatus",
                                afspraakId
                        )
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
                        status().isOk()
                );

        verify(
                workflowService
        ).wijzigUitvoeringsstatus(
                afspraakId,
                UitvoeringsStatus.IN_UITVOERING,
                gebruikerId
        );
    }
}