
package nl.outokumpu.afspraken.controller;

import nl.outokumpu.afspraken.dto.response.NotificatieAantalResponse;
import nl.outokumpu.afspraken.dto.response.NotificatieResponse;
import nl.outokumpu.afspraken.security.SecurityGebruiker;
import nl.outokumpu.afspraken.service.NotificatieService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notificaties")
public class NotificatieController {

    private final NotificatieService notificatieService;

    public NotificatieController(
            NotificatieService notificatieService
    ) {
        this.notificatieService = notificatieService;
    }

    @GetMapping
    public ResponseEntity<List<NotificatieResponse>> getNotificaties(
            Authentication authentication
    ) {
        UUID gebruikerId =
                SecurityGebruiker.gebruikerId(authentication);

        return ResponseEntity.ok(
                notificatieService.vindActieveNotificaties(gebruikerId)
        );
    }

    @GetMapping("/ongelezen-aantal")
    public ResponseEntity<NotificatieAantalResponse> getOngelezenAantal(
            Authentication authentication
    ) {
        UUID gebruikerId =
                SecurityGebruiker.gebruikerId(authentication);

        long aantal =
                notificatieService.telOngelezenNotificaties(gebruikerId);

        return ResponseEntity.ok(
                new NotificatieAantalResponse(aantal)
        );
    }

    @PutMapping("/{id}/gelezen")
    public ResponseEntity<Void> markeerGelezen(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UUID gebruikerId =
                SecurityGebruiker.gebruikerId(authentication);

        notificatieService.markeerGelezen(id, gebruikerId);

        return ResponseEntity.noContent().build();
    }
}
