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

/**
 * Exposes workflow and assignment changes, using the authenticated identity for service-level authorization.
 */
@RestController
@RequestMapping("/api")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    /**
     * Replaces participants and departments and assigns a responsible user to a selected process step.
     * The service permits only the initiator or current responsible user to make this change
     * and verifies that the selected step belongs to the agreement.
     *
     * @param id the agreement whose assignments are being changed
     * @param request the validated target assignments and selected process step
     * @param authentication the identity used for authorization and change history
     * @return the agreement details with the updated assignments
     */
    @PutMapping("/afspraken/{id}/toewijzing")
    public ResponseEntity<AfspraakDetailResponse> updateToewijzing(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateToewijzingRequest request,
            Authentication authentication) {
        UUID gebruikerId = SecurityGebruiker.gebruikerId(authentication);

        return ResponseEntity.ok(workflowService.updateToewijzing(id, request, gebruikerId));
    }

    /**
     * Starts a not-yet-started process step; the service restricts this to that step's responsible user.
     *
     * @param id the process step to start
     * @param authentication the identity used for the responsibility check
     * @return the containing agreement's updated details
     */
    @PutMapping("/processtappen/{id}/starten")
    public ResponseEntity<AfspraakDetailResponse> startProcesstap(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID gebruikerId = SecurityGebruiker.gebruikerId(authentication);

        return ResponseEntity.ok(workflowService.startProcesstap(id, gebruikerId));
    }

    /**
     * Completes an in-progress process step; the service restricts this to that step's responsible user.
     * Completing a step does not automatically complete the agreement or start another step.
     *
     * @param id the process step to complete
     * @param authentication the identity used for the responsibility check
     * @return the containing agreement's updated details
     */
    @PutMapping("/processtappen/{id}/afronden")
    public ResponseEntity<AfspraakDetailResponse> rondProcesstapAf(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID gebruikerId = SecurityGebruiker.gebruikerId(authentication);

        return ResponseEntity.ok(workflowService.rondProcesstapAf(id, gebruikerId));
    }

    /**
     * Changes the agreement status; the service restricts this to the current responsible user.
     * That user belongs to the first unfinished step, or the last step when all are complete.
     *
     * @param id the agreement to update
     * @param request the desired agreement status
     * @param authentication the identity used for authorization and change history
     * @return the updated agreement details
     */
    @PutMapping("/afspraken/{id}/status")
    public ResponseEntity<AfspraakDetailResponse> wijzigAfspraakStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAfspraakStatusRequest request,
            Authentication authentication) {
        UUID gebruikerId = SecurityGebruiker.gebruikerId(authentication);

        return ResponseEntity.ok(workflowService.wijzigAfspraakStatus(id, request.status(), gebruikerId));
    }

    /**
     * Changes an order relocation's execution status independently of its agreement status.
     * The service requires an order relocation and authorizes only the current responsible user.
     *
     * @param id the order relocation agreement to update
     * @param request the desired execution status
     * @param authentication the identity used for authorization and change history
     * @return the updated agreement details
     */
    @PutMapping("/afspraken/{id}/uitvoeringsstatus")
    public ResponseEntity<AfspraakDetailResponse> wijzigUitvoeringsstatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUitvoeringsstatusRequest request,
            Authentication authentication) {
        UUID gebruikerId = SecurityGebruiker.gebruikerId(authentication);

        return ResponseEntity.ok(workflowService.wijzigUitvoeringsstatus(id, request.status(), gebruikerId));
    }
}