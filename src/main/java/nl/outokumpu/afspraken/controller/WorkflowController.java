package nl.outokumpu.afspraken.controller;

import jakarta.validation.Valid;

import nl.outokumpu.afspraken.dto.request.UpdateAfspraakStatusRequest;
import nl.outokumpu.afspraken.dto.request.UpdateToewijzingRequest;
import nl.outokumpu.afspraken.dto.request.UpdateUitvoeringsstatusRequest;
import nl.outokumpu.afspraken.dto.response.AfspraakDetailResponse;
import nl.outokumpu.afspraken.security.SecurityGebruiker;
import nl.outokumpu.afspraken.service.WorkflowService;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(
            WorkflowService workflowService
    ) {
        this.workflowService =
                workflowService;
    }

    @PutMapping("/afspraken/{id}/toewijzing")
    public ResponseEntity<AfspraakDetailResponse> updateToewijzing(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateToewijzingRequest request,
            Authentication authentication
    ) {

        UUID gebruikerId =
                SecurityGebruiker.gebruikerId(
                        authentication
                );

        return ResponseEntity.ok(
                workflowService.updateToewijzing(
                        id,
                        request,
                        gebruikerId
                )
        );
    }

    @PutMapping("/processtappen/{id}/starten")
    public ResponseEntity<AfspraakDetailResponse> startProcesstap(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        UUID gebruikerId =
                SecurityGebruiker.gebruikerId(
                        authentication
                );

        return ResponseEntity.ok(
                workflowService.startProcesstap(
                        id,
                        gebruikerId
                )
        );
    }

    @PutMapping("/processtappen/{id}/afronden")
    public ResponseEntity<AfspraakDetailResponse> rondProcesstapAf(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        UUID gebruikerId =
                SecurityGebruiker.gebruikerId(
                        authentication
                );

        return ResponseEntity.ok(
                workflowService.rondProcesstapAf(
                        id,
                        gebruikerId
                )
        );
    }

    @PutMapping("/afspraken/{id}/status")
    public ResponseEntity<AfspraakDetailResponse> wijzigAfspraakStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAfspraakStatusRequest request,
            Authentication authentication
    ) {

        UUID gebruikerId =
                SecurityGebruiker.gebruikerId(
                        authentication
                );

        return ResponseEntity.ok(
                workflowService.wijzigAfspraakStatus(
                        id,
                        request.status(),
                        gebruikerId
                )
        );
    }

    @PutMapping("/afspraken/{id}/uitvoeringsstatus")
    public ResponseEntity<AfspraakDetailResponse> wijzigUitvoeringsstatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUitvoeringsstatusRequest request,
            Authentication authentication
    ) {

        UUID gebruikerId =
                SecurityGebruiker.gebruikerId(
                        authentication
                );

        return ResponseEntity.ok(
                workflowService.wijzigUitvoeringsstatus(
                        id,
                        request.status(),
                        gebruikerId
                )
        );
    }
}