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
import nl.outokumpu.afspraken.mapper.AfspraakMapper;
import nl.outokumpu.afspraken.repository.BevestigingRepository;
import nl.outokumpu.afspraken.exception.ForbiddenOperationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Records read confirmations and restricts approval decisions to designated approvers.
 */
@Service
@Transactional
public class BevestigingService {

    private final AfspraakMapper afspraakMapper;

    private final BevestigingRepository bevestigingRepository;

    public BevestigingService(
            BevestigingRepository bevestigingRepository,
            AfspraakMapper afspraakMapper
    ) {
        this.bevestigingRepository =
                bevestigingRepository;

        this.afspraakMapper = afspraakMapper;
    }

    /**
     * Records a read acknowledgement for an existing user confirmation.
     * Acknowledging an agreement does not require the approver role.
     *
     * @param afspraakId the agreement being acknowledged
     * @param gebruikerId the user whose confirmation is updated
     * @return the updated confirmation with its read timestamp
     * @throws IllegalArgumentException if no confirmation exists for the agreement and user
     * @throws IllegalStateException if the agreement has already been acknowledged by this user
     */
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

    /**
     * Records an approval or rejection only for a currently designated approver.
     * A previous decision may be changed, but repeating the same decision is rejected.
     *
     * @param afspraakId the agreement being reviewed
     * @param gebruikerId the user making the decision
     * @param beslissing the approval or rejection to record
     * @return the updated confirmation with its decision timestamp
     * @throws ForbiddenOperationException if the user is not a designated approver
     * @throws IllegalArgumentException if no confirmation exists or the decision is null, GEEN, or unchanged
     */
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
                afspraakMapper.naarGebruikerResponse(
                        gebruiker,
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

        return afspraakMapper.naarAfdelingResponse(
                afdeling
        );
    }
}