package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.request.CreateOpmerkingRequest;
import nl.outokumpu.afspraken.dto.response.AfdelingResponse;
import nl.outokumpu.afspraken.dto.response.GebruikerResponse;
import nl.outokumpu.afspraken.dto.response.OpmerkingResponse;

import nl.outokumpu.afspraken.entity.Afdeling;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.entity.OperationeleAfspraak;
import nl.outokumpu.afspraken.entity.Opmerking;

import nl.outokumpu.afspraken.mapper.AfspraakMapper;
import nl.outokumpu.afspraken.repository.GebruikerRepository;
import nl.outokumpu.afspraken.repository.OperationeleAfspraakRepository;
import nl.outokumpu.afspraken.repository.OpmerkingRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Stores agreement comments and retrieves them in chronological order.
 */
@Service
@Transactional
public class OpmerkingService {

    private final AfspraakMapper afspraakMapper;

    private final OpmerkingRepository opmerkingRepository;
    private final OperationeleAfspraakRepository afspraakRepository;
    private final GebruikerRepository gebruikerRepository;

    public OpmerkingService(
            OpmerkingRepository opmerkingRepository,
            OperationeleAfspraakRepository afspraakRepository,
            GebruikerRepository gebruikerRepository,
            AfspraakMapper afspraakMapper
    ) {
        this.opmerkingRepository =
                opmerkingRepository;

        this.afspraakRepository =
                afspraakRepository;

        this.gebruikerRepository =
                gebruikerRepository;

        this.afspraakMapper = afspraakMapper;
    }

    /**
     * Adds a comment to an existing agreement on behalf of an existing user.
     * The domain model trims the text and rejects blank or oversized comments.
     *
     * @param afspraakId the agreement receiving the comment
     * @param auteurId the author of the comment
     * @param request the comment text
     * @return the stored comment, including its author
     * @throws IllegalArgumentException if the agreement or author is missing, or the comment text is invalid
     */
    public OpmerkingResponse voegOpmerkingToe(
            UUID afspraakId,
            UUID auteurId,
            CreateOpmerkingRequest request
    ) {

        OperationeleAfspraak afspraak =
                afspraakRepository
                        .findById(
                                afspraakId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Afspraak niet gevonden"
                                )
                        );

        Gebruiker auteur =
                gebruikerRepository
                        .findById(
                                auteurId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Auteur niet gevonden"
                                )
                        );

        Opmerking opmerking =
                new Opmerking(
                        afspraak,
                        auteur,
                        request.tekst()
                );

        Opmerking opgeslagenOpmerking =
                opmerkingRepository.save(
                        opmerking
                );

        return naarResponse(
                opgeslagenOpmerking
        );
    }

    /**
     * Retrieves comments in creation order, distinguishing a missing agreement from one without comments.
     *
     * @param afspraakId the agreement whose comments are requested
     * @return the comments in ascending creation order, or an empty list
     * @throws IllegalArgumentException if the agreement does not exist
     */
    @Transactional(readOnly = true)
    public List<OpmerkingResponse> vindOpmerkingen(
            UUID afspraakId
    ) {

        if (!afspraakRepository.existsById(
                afspraakId
        )) {

            throw new IllegalArgumentException(
                    "Afspraak niet gevonden"
            );
        }

        return opmerkingRepository
                .findByAfspraakIdOrderByAangemaaktOpAsc(
                        afspraakId
                )
                .stream()
                .map(
                        this::naarResponse
                )
                .toList();
    }

    private OpmerkingResponse naarResponse(
            Opmerking opmerking
    ) {

        return new OpmerkingResponse(
                opmerking.getId(),
                naarGebruikerResponse(
                        opmerking.getAuteur()
                ),
                opmerking.getTekst(),
                opmerking.getAangemaaktOp()
        );
    }

    private GebruikerResponse naarGebruikerResponse(
            Gebruiker gebruiker
    ) {

        return afspraakMapper.naarGebruikerResponse(
                gebruiker,
                naarAfdelingResponse(
                        gebruiker.getAfdeling()
                )
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