package nl.outokumpu.afspraken.controller;

import jakarta.validation.Valid;

import nl.outokumpu.afspraken.dto.request.CreateOpmerkingRequest;
import nl.outokumpu.afspraken.dto.response.OpmerkingResponse;

import nl.outokumpu.afspraken.security.SecurityGebruiker;

import nl.outokumpu.afspraken.service.OpmerkingService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/afspraken/{afspraakId}/opmerkingen")
public class OpmerkingController {

    private final OpmerkingService opmerkingService;

    public OpmerkingController(
            OpmerkingService opmerkingService
    ) {
        this.opmerkingService =
                opmerkingService;
    }

    @GetMapping
    public ResponseEntity<List<OpmerkingResponse>>
    vindOpmerkingen(
            @PathVariable UUID afspraakId
    ) {

        return ResponseEntity.ok(
                opmerkingService
                        .vindOpmerkingen(
                                afspraakId
                        )
        );
    }

    @PostMapping
    public ResponseEntity<OpmerkingResponse>
    voegOpmerkingToe(
            @PathVariable UUID afspraakId,
            @Valid
            @RequestBody
            CreateOpmerkingRequest request,
            Authentication authentication
    ) {

        UUID auteurId =
                SecurityGebruiker
                        .gebruikerId(
                                authentication
                        );

        OpmerkingResponse response =
                opmerkingService
                        .voegOpmerkingToe(
                                afspraakId,
                                auteurId,
                                request
                        );

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        response
                );
    }
}