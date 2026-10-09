package nl.outokumpu.afspraken.controller;

import nl.outokumpu.afspraken.dto.response.AfdelingResponse;
import nl.outokumpu.afspraken.service.AfdelingService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/afdelingen")
public class AfdelingController {

    private final AfdelingService afdelingService;

    public AfdelingController(
            AfdelingService afdelingService
    ) {
        this.afdelingService = afdelingService;
    }

    @GetMapping
    public ResponseEntity<List<AfdelingResponse>>
    vindAlleAfdelingen() {

        return ResponseEntity.ok(
                afdelingService.vindAlleAfdelingen()
        );
    }
}