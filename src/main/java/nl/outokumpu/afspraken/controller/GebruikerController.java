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

/**
 * Exposes user reference data and administrator-only account management through the service layer.
 */
@RestController
@RequestMapping("/api/gebruikers")
public class GebruikerController {

    private final GebruikerService gebruikerService;

    public GebruikerController(GebruikerService gebruikerService) {
        this.gebruikerService = gebruikerService;
    }

    @GetMapping
    public ResponseEntity<List<GebruikerResponse>> vindAlleGebruikers() {
        return ResponseEntity.ok(gebruikerService.vindAlleGebruikers());
    }

    /**
     * Returns account-management details, including active flags and local-login availability.
     * Administrator access is enforced by the service's method-level security.
     *
     * @return all users with their administrative account details
     */
    @GetMapping("/beheer")
    public ResponseEntity<List<GebruikerBeheerResponse>> vindAlleGebruikersVoorBeheer() {
        return ResponseEntity.ok(gebruikerService.vindAlleGebruikersVoorBeheer());
    }

    /**
     * Updates an account's role and active flag without changing its password or department.
     * Administrator access is enforced by the service's method-level security.
     *
     * @param id the account to update
     * @param request the validated role and active flag
     * @return the updated administrative account details
     */
    @PutMapping("/{id}/toegang")
    public ResponseEntity<GebruikerBeheerResponse> wijzigToegang(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateGebruikerToegangRequest request) {
        return ResponseEntity.ok(gebruikerService.wijzigToegang(id, request));
    }
}