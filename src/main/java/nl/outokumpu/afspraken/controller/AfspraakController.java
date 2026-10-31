package nl.outokumpu.afspraken.controller;

import jakarta.validation.Valid;

import nl.outokumpu.afspraken.dto.request.AfspraakFilterRequest;
import nl.outokumpu.afspraken.dto.request.CreateAfspraakRequest;
import nl.outokumpu.afspraken.dto.request.UpdateAfspraakRequest;
import nl.outokumpu.afspraken.dto.request.UpdateGoedkeurdersRequest;
import nl.outokumpu.afspraken.dto.response.AfspraakDetailResponse;
import nl.outokumpu.afspraken.dto.response.AfspraakSummaryResponse;
import nl.outokumpu.afspraken.enums.AfspraakStatus;
import nl.outokumpu.afspraken.enums.AfspraakType;
import nl.outokumpu.afspraken.security.SecurityGebruiker;
import nl.outokumpu.afspraken.service.AfspraakService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Exposes agreement creation, retrieval, search and updates, delegating business permissions to the service.
 */
@RestController
@RequestMapping("/api/afspraken")
public class AfspraakController {

    private final AfspraakService afspraakService;

    public AfspraakController(
            AfspraakService afspraakService
    ) {
        this.afspraakService =
                afspraakService;
    }

    /**
     * Creates an agreement with the authenticated user as its initiator.
     * The initiator's identity is taken from the security context rather than the request body.
     *
     * @param request the validated agreement details and initial assignments
     * @param authentication the authenticated identity supplied by Spring Security
     * @return HTTP 201 with the created agreement details
     */
    @PostMapping
    public ResponseEntity<AfspraakDetailResponse> maakAfspraak(
            @Valid @RequestBody CreateAfspraakRequest request,
            Authentication authentication
    ) {

        UUID gebruikerId =
                SecurityGebruiker.gebruikerId(
                        authentication
                );

        AfspraakDetailResponse response =
                afspraakService.createAfspraak(
                        request,
                        gebruikerId
                );

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AfspraakDetailResponse> vindAfspraak(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                afspraakService.vindAfspraak(
                        id
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<AfspraakSummaryResponse>>
    vindAlleAfspraken() {

        return ResponseEntity.ok(
                afspraakService
                        .vindAlleAfspraken()
        );
    }

    @GetMapping("/actief")
    public ResponseEntity<List<AfspraakSummaryResponse>>
    vindActieveAfspraken() {

        return ResponseEntity.ok(
                afspraakService
                        .vindActieveAfspraken()
        );
    }

    @GetMapping("/archief")
    public ResponseEntity<List<AfspraakSummaryResponse>>
    vindAfgerondeAfspraken() {

        return ResponseEntity.ok(
                afspraakService
                        .vindAfgerondeAfspraken()
        );
    }

    /**
     * Searches agreement text with a trimmed, case-insensitive keyword match.
     *
     * @param zoekterm the required, non-blank search term
     * @return matching agreement summaries, most recently modified first
     */
    @GetMapping("/zoeken")
    public ResponseEntity<List<AfspraakSummaryResponse>>
    zoekAfspraken(
            @RequestParam String zoekterm
    ) {

        return ResponseEntity.ok(
                afspraakService.zoekAfspraken(
                        zoekterm
                )
        );
    }

    /**
     * Combines optional filters; omitted criteria do not restrict the results.
     * Dates match overlapping agreement intervals, and the responsible-user criterion
     * matches any process step rather than only the current step.
     *
     * @param type the agreement type, or null to include all types
     * @param status the agreement status, or null to include all statuses
     * @param verantwoordelijkeId the responsible user of any process step, or null
     * @param afdelingId an involved department, or null
     * @param fabriek a case-insensitive factory-name fragment, or null
     * @param periodeVan the inclusive start of the overlap window, or null
     * @param periodeTot the inclusive end of the overlap window, or null
     * @return matching agreement summaries, most recently modified first
     */
    @GetMapping("/filter")
    public ResponseEntity<List<AfspraakSummaryResponse>>
    filterAfspraken(

            @RequestParam(required = false)
            AfspraakType type,

            @RequestParam(required = false)
            AfspraakStatus status,

            @RequestParam(required = false)
            UUID verantwoordelijkeId,

            @RequestParam(required = false)
            UUID afdelingId,

            @RequestParam(required = false)
            String fabriek,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate periodeVan,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate periodeTot
    ) {

        AfspraakFilterRequest filter =
                new AfspraakFilterRequest(
                        type,
                        status,
                        verantwoordelijkeId,
                        afdelingId,
                        fabriek,
                        periodeVan,
                        periodeTot
                );

        return ResponseEntity.ok(
                afspraakService.filterAfspraken(
                        filter
                )
        );
    }

    /**
     * Updates editable agreement details on behalf of the authenticated user.
     * The service permits only the initiator or current responsible user to edit,
     * and rejects requests that do not change any editable values.
     *
     * @param id the agreement to update
     * @param request the validated replacement values
     * @param authentication the identity used for the permission check and change history
     * @return the updated agreement details
     */
    @PutMapping("/{id}")
    public ResponseEntity<AfspraakDetailResponse> updateAfspraak(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAfspraakRequest request,
            Authentication authentication
    ) {

        UUID gebruikerId =
                SecurityGebruiker.gebruikerId(
                        authentication
                );

        return ResponseEntity.ok(
                afspraakService.updateAfspraak(
                        id,
                        request,
                        gebruikerId
                )
        );
    }

    /**
     * Replaces the agreement's approver set; the service restricts this to its initiator.
     * Existing confirmations remain available when approvers are removed.
     *
     * @param id the agreement whose approvers are being replaced
     * @param request the complete set of desired approver IDs
     * @param authentication the identity used for the initiator check
     * @return the agreement details with the resulting approver set
     */
    @PutMapping("/{id}/goedkeurders")
    public ResponseEntity<AfspraakDetailResponse> wijzigGoedkeurders(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateGoedkeurdersRequest request,
            Authentication authentication
    ) {

        UUID gebruikerId =
                SecurityGebruiker.gebruikerId(
                        authentication
                );

        return ResponseEntity.ok(
                afspraakService.wijzigGoedkeurders(
                        id,
                        request,
                        gebruikerId
                )
        );
    }
}