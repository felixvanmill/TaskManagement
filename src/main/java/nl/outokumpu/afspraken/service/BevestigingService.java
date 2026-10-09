package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.response.AfdelingResponse;
import nl.outokumpu.afspraken.dto.response.BevestigingResponse;
import nl.outokumpu.afspraken.dto.response.GebruikerResponse;
import nl.outokumpu.afspraken.entity.Afdeling;
import nl.outokumpu.afspraken.entity.Bevestiging;
import nl.outokumpu.afspraken.entity.Betrokkenheid;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.enums.Beslissing;
import nl.outokumpu.afspraken.enums.BetrokkenRol;
import nl.outokumpu.afspraken.repository.BevestigingRepository;
import nl.outokumpu.afspraken.exception.ForbiddenOperationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class BevestigingService {

    private final BevestigingRepository bevestigingRepository;

    public BevestigingService(
            BevestigingRepository bevestigingRepository
    ) {
        this.bevestigingRepository =
                bevestigingRepository;
    }

    public BevestigingResponse markeerGezien(
            UUID afspraakId,
            UUID gebruikerId
    ) {

        Bevestiging bevestiging =
                vindBevestiging(
                        afspraakId,
                        gebruikerId
                );

        bevestiging.markeerGezien();

        Bevestiging opgeslagenBevestiging =
                bevestigingRepository.save(
                        bevestiging
                );

        return naarResponse(
                opgeslagenBevestiging
        );
    }

    public BevestigingResponse registreerBeslissing(
            UUID afspraakId,
            UUID gebruikerId,
            Beslissing beslissing
    ) {

        Bevestiging bevestiging =
                vindBevestiging(
                        afspraakId,
                        gebruikerId
                );

        controleerGoedkeurder(
                bevestiging,
                gebruikerId
        );

        bevestiging.registreerBeslissing(
                beslissing
        );

        Bevestiging opgeslagenBevestiging =
                bevestigingRepository.save(
                        bevestiging
                );

        return naarResponse(
                opgeslagenBevestiging
        );
    }

    private Bevestiging vindBevestiging(
            UUID afspraakId,
            UUID gebruikerId
    ) {

        return bevestigingRepository
                .findByAfspraakIdAndGebruikerId(
                        afspraakId,
                        gebruikerId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Bevestiging niet gevonden"
                        )
                );
    }

    private void controleerGoedkeurder(
            Bevestiging bevestiging,
            UUID gebruikerId
    ) {

        boolean isGoedkeurder =
                bevestiging.getAfspraak()
                        .getBetrokkenheden()
                        .stream()
                        .anyMatch(
                                betrokkenheid ->
                                        isGoedkeurder(
                                                betrokkenheid,
                                                gebruikerId
                                        )
                        );

        if (!isGoedkeurder) {
            throw new ForbiddenOperationException(
                    "Alleen een aangewezen goedkeurder kan een beslissing registreren"
            );
        }
    }

    private boolean isGoedkeurder(
            Betrokkenheid betrokkenheid,
            UUID gebruikerId
    ) {

        Gebruiker gebruiker =
                betrokkenheid.getGebruiker();

        return betrokkenheid.getRol()
                == BetrokkenRol.GOEDKEURDER
                && gebruikerId != null
                && gebruikerId.equals(
                gebruiker.getId()
        );
    }

    private BevestigingResponse naarResponse(
            Bevestiging bevestiging
    ) {

        Gebruiker gebruiker =
                bevestiging.getGebruiker();

        AfdelingResponse afdelingResponse =
                naarAfdelingResponse(
                        gebruiker.getAfdeling()
                );

        GebruikerResponse gebruikerResponse =
                new GebruikerResponse(
                        gebruiker.getId(),
                        gebruiker.getNaam(),
                        gebruiker.getEmail(),
                        afdelingResponse
                );

        return new BevestigingResponse(
                bevestiging.getId(),
                gebruikerResponse,
                bevestiging.isGezien(),
                bevestiging.getGezienOp(),
                bevestiging.getBeslissing(),
                bevestiging.getBeslistOp()
        );
    }

    private AfdelingResponse naarAfdelingResponse(
            Afdeling afdeling
    ) {

        if (afdeling == null) {
            return null;
        }

        return new AfdelingResponse(
                afdeling.getId(),
                afdeling.getNaam()
        );
    }
}