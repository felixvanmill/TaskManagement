package nl.outokumpu.afspraken.controller;

import nl.outokumpu.afspraken.dto.response.AfdelingResponse;
import nl.outokumpu.afspraken.service.AfdelingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AfdelingControllerTest {

    private AfdelingService afdelingService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        afdelingService =
                mock(AfdelingService.class);

        AfdelingController controller =
                new AfdelingController(
                        afdelingService
                );

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(controller)
                        .build();
    }

    @Test
    void haaltAlleAfdelingenOp() throws Exception {

        UUID planningId =
                UUID.randomUUID();

        UUID productieId =
                UUID.randomUUID();

        AfdelingResponse planning =
                new AfdelingResponse(
                        planningId,
                        "Capacity Planning"
                );

        AfdelingResponse productie =
                new AfdelingResponse(
                        productieId,
                        "Productie"
                );

        when(
                afdelingService.vindAlleAfdelingen()
        ).thenReturn(
                List.of(
                        planning,
                        productie
                )
        );

        mockMvc.perform(
                        get("/api/afdelingen")
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
                                        planningId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$[0].naam")
                                .value(
                                        "Capacity Planning"
                                )
                )
                .andExpect(
                        jsonPath("$[1].id")
                                .value(
                                        productieId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$[1].naam")
                                .value(
                                        "Productie"
                                )
                );

        verify(
                afdelingService
        ).vindAlleAfdelingen();

        verifyNoMoreInteractions(
                afdelingService
        );
    }

    @Test
    void geeftLegeLijstTerugWanneerGeenAfdelingenBestaan()
            throws Exception {

        when(
                afdelingService.vindAlleAfdelingen()
        ).thenReturn(
                List.of()
        );

        mockMvc.perform(
                        get("/api/afdelingen")
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
                        jsonPath("$")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$")
                                .isEmpty()
                );

        verify(
                afdelingService
        ).vindAlleAfdelingen();
    }
}