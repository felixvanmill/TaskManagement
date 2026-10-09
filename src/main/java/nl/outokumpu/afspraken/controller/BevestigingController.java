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

@RestController
@RequestMapping("/api/afspraken/{afspraakId}/bevestiging")
public class BevestigingController {

    private final BevestigingService bevestigingService;

    public BevestigingController(
            BevestigingService bevestigingService
    ) {
        this.bevestigingService =
                bevestigingService;
    }

    @PutMapping("/gezien")
    public ResponseEntity<BevestigingResponse> markeerGezien(
            @PathVariable UUID afspraakId,
            Authentication authentication
    ) {

        UUID gebruikerId =
                SecurityGebruiker.gebruikerId(
                        authentication
                );

        return ResponseEntity.ok(
                bevestigingService.markeerGezien(
                        afspraakId,
                        gebruikerId
                )
        );
    }

    @PutMapping("/beslissing")
    public ResponseEntity<BevestigingResponse> registreerBeslissing(
            @PathVariable UUID afspraakId,
            @Valid @RequestBody BeslissingRequest request,
            Authentication authentication
    ) {

        UUID gebruikerId =
                SecurityGebruiker.gebruikerId(
                        authentication
                );

        return ResponseEntity.ok(
                bevestigingService.registreerBeslissing(
                        afspraakId,
                        gebruikerId,
                        request.beslissing()
                )
        );
    }
}