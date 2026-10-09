package nl.outokumpu.afspraken.controller;

import jakarta.validation.Valid;

import nl.outokumpu.afspraken.dto.request.UpdateGebruikerToegangRequest;
import nl.outokumpu.afspraken.dto.response.GebruikerBeheerResponse;
import nl.outokumpu.afspraken.dto.response.GebruikerResponse;
import nl.outokumpu.afspraken.service.GebruikerService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gebruikers")
public class GebruikerController {

    private final GebruikerService gebruikerService;

    public GebruikerController(
            GebruikerService gebruikerService
    ) {
        this.gebruikerService =
                gebruikerService;
    }

    @GetMapping
    public ResponseEntity<List<GebruikerResponse>>
    vindAlleGebruikers() {

        return ResponseEntity.ok(
                gebruikerService
                        .vindAlleGebruikers()
        );
    }

    @GetMapping("/beheer")
    public ResponseEntity<List<GebruikerBeheerResponse>>
    vindAlleGebruikersVoorBeheer() {

        return ResponseEntity.ok(
                gebruikerService
                        .vindAlleGebruikersVoorBeheer()
        );
    }

    @PutMapping("/{id}/toegang")
    public ResponseEntity<GebruikerBeheerResponse>
    wijzigToegang(
            @PathVariable UUID id,
            @Valid
            @RequestBody
            UpdateGebruikerToegangRequest request
    ) {

        return ResponseEntity.ok(
                gebruikerService.wijzigToegang(
                        id,
                        request
                )
        );
    }
}