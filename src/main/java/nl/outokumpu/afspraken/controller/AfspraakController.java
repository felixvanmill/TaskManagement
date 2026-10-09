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
import nl.outokumpu.afspraken.service.AfspraakService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/afspraken")
public class AfspraakController {

    private final AfspraakService afspraakService;

    public AfspraakController(
            AfspraakService afspraakService
    ) {
        this.afspraakService = afspraakService;
    }

    @PostMapping
    public ResponseEntity<AfspraakDetailResponse> maakAfspraak(
            @Valid @RequestBody CreateAfspraakRequest request,
            @RequestHeader("X-Gebruiker-Id") UUID gebruikerId
    ) {

        AfspraakDetailResponse response =
                afspraakService.createAfspraak(
                        request,
                        gebruikerId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AfspraakDetailResponse> vindAfspraak(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                afspraakService.vindAfspraak(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<AfspraakSummaryResponse>>
    vindAlleAfspraken() {

        return ResponseEntity.ok(
                afspraakService.vindAlleAfspraken()
        );
    }

    @GetMapping("/actief")
    public ResponseEntity<List<AfspraakSummaryResponse>>
    vindActieveAfspraken() {

        return ResponseEntity.ok(
                afspraakService.vindActieveAfspraken()
        );
    }

    @GetMapping("/archief")
    public ResponseEntity<List<AfspraakSummaryResponse>>
    vindAfgerondeAfspraken() {

        return ResponseEntity.ok(
                afspraakService.vindAfgerondeAfspraken()
        );
    }

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
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodeVan,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
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

    @PutMapping("/{id}")
    public ResponseEntity<AfspraakDetailResponse> updateAfspraak(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAfspraakRequest request,
            @RequestHeader("X-Gebruiker-Id") UUID gebruikerId
    ) {

        return ResponseEntity.ok(
                afspraakService.updateAfspraak(
                        id,
                        request,
                        gebruikerId
                )
        );
    }

    @PutMapping("/{id}/goedkeurders")
    public ResponseEntity<AfspraakDetailResponse> wijzigGoedkeurders(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateGoedkeurdersRequest request,
            @RequestHeader("X-Gebruiker-Id") UUID gebruikerId
    ) {

        return ResponseEntity.ok(
                afspraakService.wijzigGoedkeurders(
                        id,
                        request,
                        gebruikerId
                )
        );
    }
}