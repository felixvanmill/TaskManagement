package nl.outokumpu.afspraken.controller;

import jakarta.validation.Valid;

import nl.outokumpu.afspraken.dto.request.BeslissingRequest;
import nl.outokumpu.afspraken.dto.response.BevestigingResponse;
import nl.outokumpu.afspraken.security.SecurityGebruiker;
import nl.outokumpu.afspraken.service.BevestigingService;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Exposes read acknowledgements and approval decisions for the authenticated user.
 */
@RestController
@RequestMapping("/api/afspraken/{afspraakId}/bevestiging")
public class BevestigingController {

    private final BevestigingService bevestigingService;

    public BevestigingController(BevestigingService bevestigingService) {
        this.bevestigingService = bevestigingService;
    }

    /**
     * Acknowledges the agreement using the authenticated user's existing confirmation.
     * The service rejects repeated acknowledgements; the approver role is not required.
     *
     * @param afspraakId the agreement being acknowledged
     * @param authentication the identity whose confirmation is updated
     * @return the updated read confirmation
     */
    @PutMapping("/gezien")
    public ResponseEntity<BevestigingResponse> markeerGezien(
            @PathVariable UUID afspraakId,
            Authentication authentication) {
        UUID gebruikerId = SecurityGebruiker.gebruikerId(authentication);

        return ResponseEntity.ok(bevestigingService.markeerGezien(afspraakId, gebruikerId));
    }

    /**
     * Records an approval or rejection for the authenticated user.
     * The service requires a designated approver and rejects an unchanged decision.
     *
     * @param afspraakId the agreement being reviewed
     * @param request the desired approval or rejection
     * @param authentication the identity used for the approver check and confirmation lookup
     * @return the updated confirmation with its decision timestamp
     */
    @PutMapping("/beslissing")
    public ResponseEntity<BevestigingResponse> registreerBeslissing(
            @PathVariable UUID afspraakId,
            @Valid @RequestBody BeslissingRequest request,
            Authentication authentication) {
        UUID gebruikerId = SecurityGebruiker.gebruikerId(authentication);

        return ResponseEntity.ok(
                bevestigingService.registreerBeslissing(afspraakId, gebruikerId, request.beslissing()));
    }
}