package nl.outokumpu.afspraken.controller;

import nl.outokumpu.afspraken.dto.response.GebruikerResponse;
import nl.outokumpu.afspraken.service.GebruikerService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/gebruikers")
public class GebruikerController {

    private final GebruikerService gebruikerService;

    public GebruikerController(
            GebruikerService gebruikerService
    ) {
        this.gebruikerService = gebruikerService;
    }

    @GetMapping
    public ResponseEntity<List<GebruikerResponse>>
    vindAlleGebruikers() {

        return ResponseEntity.ok(
                gebruikerService.vindAlleGebruikers()
        );
    }
}