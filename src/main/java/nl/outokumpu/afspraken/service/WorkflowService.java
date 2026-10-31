package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.request.UpdateToewijzingRequest;
import nl.outokumpu.afspraken.dto.response.AfspraakDetailResponse;
import nl.outokumpu.afspraken.entity.*;
import nl.outokumpu.afspraken.enums.AfspraakStatus;
import nl.outokumpu.afspraken.enums.BetrokkenRol;
import nl.outokumpu.afspraken.enums.ProcesstapStatus;
import nl.outokumpu.afspraken.enums.UitvoeringsStatus;
import nl.outokumpu.afspraken.exception.ForbiddenOperationException;
import nl.outokumpu.afspraken.mapper.AfspraakMapper;
import nl.outokumpu.afspraken.repository.AfdelingRepository;
import nl.outokumpu.afspraken.repository.GebruikerRepository;
import nl.outokumpu.afspraken.repository.OperationeleAfspraakRepository;
import nl.outokumpu.afspraken.repository.ProcesstapRepository;
import nl.outokumpu.afspraken.repository.WijzigingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Applies authorized workflow transitions and assignment changes with an audit trail.
 */
@Service
@Transactional
public class WorkflowService {

    private final ProcesstapRepository processtapRepository;
    private final OperationeleAfspraakRepository afspraakRepository;
    private final GebruikerRepository gebruikerRepository;
    private final AfdelingRepository afdelingRepository;
    private final WijzigingRepository wijzigingRepository;
    private final AfspraakMapper afspraakMapper;

    public WorkflowService(
            ProcesstapRepository processtapRepository,
            OperationeleAfspraakRepository afspraakRepository,
            GebruikerRepository gebruikerRepository,
            AfdelingRepository afdelingRepository,
            WijzigingRepository wijzigingRepository,
            AfspraakMapper afspraakMapper
    ) {
        this.processtapRepository = processtapRepository;
        this.afspraakRepository = afspraakRepository;
        this.gebruikerRepository = gebruikerRepository;
        this.afdelingRepository = afdelingRepository;
        this.wijzigingRepository = wijzigingRepository;
        this.afspraakMapper = afspraakMapper;
    }

    /**
     * Starts a not-yet-started process step on behalf of its responsible user and records the transition.
     *
     * @param processtapId the process step to start
     * @param gebruikerId the user requesting the transition
     * @return the agreement containing the updated process step
     * @throws ForbiddenOperationException if the user is not responsible for this step
     * @throws IllegalArgumentException if the process step does not exist
     * @throws IllegalStateException if the step is not in NIET_GESTART
     */
    public AfspraakDetailResponse startProcesstap(
            UUID processtapId,
            UUID gebruikerId
    ) {

        Processtap processtap =
                processtapRepository.findById(processtapId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Processtap niet gevonden"
                                )
                        );

        Gebruiker verantwoordelijke =
                processtap.getVerantwoordelijke();

        controleerVerantwoordelijke(
                gebruikerId,
                verantwoordelijke,
                "Alleen de verantwoordelijke kan deze processtap starten"
        );

        String oudeStatus =
                processtap.getStatus().name();

        processtap.start();

        Wijziging wijziging =
                processtap.getAfspraak()
                        .registreerWijziging(
                                verantwoordelijke,
                                "Processtapstatus: "
                                        + processtap.getNaam(),
                                oudeStatus,
                                processtap.getStatus().name()
                        );

        processtapRepository.save(
                processtap
        );

        wijzigingRepository.save(
                wijziging
        );

        return afspraakMapper.naarDetailResponse(
                processtap.getAfspraak()
        );
    }

    /**
     * Completes an in-progress process step on behalf of its responsible user and records the transition.
     * This does not automatically complete the agreement or start another step.
     *
     * @param processtapId the process step to complete
     * @param gebruikerId the user requesting the transition
     * @return the agreement containing the updated process step
     * @throws ForbiddenOperationException if the user is not responsible for this step
     * @throws IllegalArgumentException if the process step does not exist
     * @throws IllegalStateException if the step is not in IN_UITVOERING
     */
    public AfspraakDetailResponse rondProcesstapAf(
            UUID processtapId,
            UUID gebruikerId
    ) {

        Processtap processtap =
                processtapRepository.findById(processtapId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Processtap niet gevonden"
                                )
                        );

        Gebruiker verantwoordelijke =
                processtap.getVerantwoordelijke();

        controleerVerantwoordelijke(
                gebruikerId,
                verantwoordelijke,
                "Alleen de verantwoordelijke kan deze processtap afronden"
        );

        String oudeStatus =
                processtap.getStatus().name();

        processtap.rondAf();

        Wijziging wijziging =
                processtap.getAfspraak()
                        .registreerWijziging(
                                verantwoordelijke,
                                "Processtapstatus: "
                                        + processtap.getNaam(),
                                oudeStatus,
                                processtap.getStatus().name()
                        );

        processtapRepository.save(
                processtap
        );

        wijzigingRepository.save(
                wijziging
        );

        return afspraakMapper.naarDetailResponse(
                processtap.getAfspraak()
        );
    }

    /**
     * Changes the agreement status on behalf of its current responsible user and records the change.
     * The current responsible user belongs to the first unfinished step, or the last step if all are complete.
     *
     * @param afspraakId the agreement to update
     * @param nieuweStatus the desired agreement status
     * @param gebruikerId the user requesting the change
     * @return the agreement with its updated status
     * @throws ForbiddenOperationException if the user is not the current responsible user
     * @throws IllegalArgumentException if the agreement is missing or the status is null or unchanged
     * @throws IllegalStateException if the agreement has no process steps
     */
    public AfspraakDetailResponse wijzigAfspraakStatus(
            UUID afspraakId,
            AfspraakStatus nieuweStatus,
            UUID gebruikerId
    ) {

        OperationeleAfspraak afspraak =
                afspraakRepository.findById(
                                afspraakId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Afspraak niet gevonden"
                                )
                        );

        Gebruiker huidigeVerantwoordelijke =
                bepaalHuidigeVerantwoordelijke(
                        afspraak
                );

        controleerVerantwoordelijke(
                gebruikerId,
                huidigeVerantwoordelijke,
                "Alleen de huidige verantwoordelijke kan de afspraakstatus wijzigen"
        );

        String oudeStatus =
                afspraak.getStatus().name();

        afspraak.wijzigStatus(
                nieuweStatus
        );

        Wijziging wijziging =
                afspraak.registreerWijziging(
                        huidigeVerantwoordelijke,
                        "Afspraakstatus",
                        oudeStatus,
                        afspraak.getStatus().name()
                );

        wijzigingRepository.save(
                wijziging
        );

        return afspraakMapper.naarDetailResponse(
                afspraak
        );
    }

    /**
     * Changes an order relocation's execution status on behalf of its current responsible user.
     * The execution status is separate from the agreement status and is recorded in the change history.
     *
     * @param afspraakId the order relocation agreement to update
     * @param nieuweStatus the desired execution status
     * @param gebruikerId the user requesting the change
     * @return the agreement with its updated execution status
     * @throws ForbiddenOperationException if the user is not the current responsible user
     * @throws IllegalArgumentException if the agreement is missing, is not an order relocation, or the status is null or unchanged
     * @throws IllegalStateException if the agreement has no process steps
     */
    public AfspraakDetailResponse wijzigUitvoeringsstatus(
            UUID afspraakId,
            UitvoeringsStatus nieuweStatus,
            UUID gebruikerId
    ) {

        OperationeleAfspraak afspraak =
                afspraakRepository.findById(
                                afspraakId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Afspraak niet gevonden"
                                )
                        );

        if (!(afspraak instanceof Orderverplaatsing orderverplaatsing)) {
            throw new IllegalArgumentException(
                    "Afspraak is geen orderverplaatsing"
            );
        }

        Gebruiker huidigeVerantwoordelijke =
                bepaalHuidigeVerantwoordelijke(
                        orderverplaatsing
                );

        controleerVerantwoordelijke(
                gebruikerId,
                huidigeVerantwoordelijke,
                "Alleen de huidige verantwoordelijke kan de uitvoeringsstatus wijzigen"
        );

        String oudeStatus =
                orderverplaatsing
                        .getUitvoeringsstatus()
                        .name();

        orderverplaatsing.wijzigUitvoeringsstatus(
                nieuweStatus
        );

        Wijziging wijziging =
                orderverplaatsing.registreerWijziging(
                        huidigeVerantwoordelijke,
                        "Uitvoeringsstatus",
                        oudeStatus,
                        orderverplaatsing
                                .getUitvoeringsstatus()
                                .name()
                );

        wijzigingRepository.save(
                wijziging
        );

        return afspraakMapper.naarDetailResponse(
                orderverplaatsing
        );
    }

    /**
     * Replaces involved users and departments and assigns a responsible user to a selected process step.
     * Only the initiator or current responsible user may change assignments. The selected step must
     * belong to the agreement; confirmations of removed participants are retained.
     *
     * @param afspraakId the agreement whose assignments are being changed
     * @param request the desired participants, departments, process step and responsible user
     * @param gebruikerId the user performing the change
     * @return the agreement with its updated assignments
     * @throws ForbiddenOperationException if the user is neither the initiator nor the current responsible user
     * @throws IllegalArgumentException if a referenced entity is missing or the step belongs to another agreement
     * @throws IllegalStateException if a non-initiator's responsibility cannot be determined because there are no steps
     */
    public AfspraakDetailResponse updateToewijzing(
            UUID afspraakId,
            UpdateToewijzingRequest request,
            UUID gebruikerId
    ) {

        OperationeleAfspraak afspraak =
                afspraakRepository.findById(
                                afspraakId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Afspraak niet gevonden"
                                )
                        );

        Gebruiker uitvoerder =
                controleerBevoegdVoorToewijzing(
                        afspraak,
                        gebruikerId
                );

        List<Gebruiker> nieuweBetrokkenen =
                gebruikerRepository.findAllById(
                        request.betrokkeneIds()
                );

        controleerAlleIdsGevonden(
                request.betrokkeneIds(),
                nieuweBetrokkenen.size(),
                "Een of meer betrokken gebruikers zijn niet gevonden"
        );

        List<Afdeling> nieuweAfdelingen =
                afdelingRepository.findAllById(
                        request.afdelingIds()
                );

        controleerAlleIdsGevonden(
                request.afdelingIds(),
                nieuweAfdelingen.size(),
                "Een of meer afdelingen zijn niet gevonden"
        );

        Processtap processtap =
                processtapRepository.findById(
                                request.processtapId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Processtap niet gevonden"
                                )
                        );

        controleerProcesstapHoortBijAfspraak(
                processtap,
                afspraak
        );

        Gebruiker nieuweVerantwoordelijke =
                gebruikerRepository.findById(
                                request.verantwoordelijkeId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Verantwoordelijke niet gevonden"
                                )
                        );

        List<Gebruiker> huidigeBetrokkenen =
                afspraak.getBetrokkenheden()
                        .stream()
                        .filter(betrokkenheid ->
                                betrokkenheid.getRol()
                                        == BetrokkenRol.BETROKKENE
                        )
                        .map(
                                Betrokkenheid::getGebruiker
                        )
                        .toList();

        List<Afdeling> huidigeAfdelingen =
                afspraak.getBetrokkenAfdelingen()
                        .stream()
                        .toList();

        Gebruiker huidigeVerantwoordelijke =
                processtap.getVerantwoordelijke();

        String oudeToewijzing =
                beschrijfToewijzing(
                        processtap.getNaam(),
                        huidigeBetrokkenen,
                        huidigeAfdelingen,
                        huidigeVerantwoordelijke
                );

        String nieuweToewijzing =
                beschrijfToewijzing(
                        processtap.getNaam(),
                        nieuweBetrokkenen,
                        nieuweAfdelingen,
                        nieuweVerantwoordelijke
                );

        Set<UUID> nieuweBetrokkeneIds =
                new HashSet<>(
                        nieuweBetrokkenen
                                .stream()
                                .map(
                                        Gebruiker::getId
                                )
                                .toList()
                );

        List<Betrokkenheid> teVerwijderenBetrokkenheden =
                afspraak.getBetrokkenheden()
                        .stream()
                        .filter(betrokkenheid ->
                                betrokkenheid.getRol()
                                        == BetrokkenRol.BETROKKENE
                        )
                        .filter(betrokkenheid ->
                                !nieuweBetrokkeneIds.contains(
                                        betrokkenheid
                                                .getGebruiker()
                                                .getId()
                                )
                        )
                        .toList();

        for (
                Betrokkenheid betrokkenheid
                : teVerwijderenBetrokkenheden
        ) {
            afspraak.verwijderBetrokkenheid(
                    betrokkenheid
            );
        }

        Set<UUID> huidigeBetrokkeneIds =
                new HashSet<>(
                        afspraak.getBetrokkenheden()
                                .stream()
                                .filter(betrokkenheid ->
                                        betrokkenheid.getRol()
                                                == BetrokkenRol.BETROKKENE
                                )
                                .map(
                                        Betrokkenheid::getGebruiker
                                )
                                .map(
                                        Gebruiker::getId
                                )
                                .toList()
                );

        for (Gebruiker betrokkene : nieuweBetrokkenen) {

            if (!huidigeBetrokkeneIds.contains(
                    betrokkene.getId()
            )) {
                afspraak.voegBetrokkenheidToe(
                        betrokkene,
                        BetrokkenRol.BETROKKENE
                );

                huidigeBetrokkeneIds.add(
                        betrokkene.getId()
                );
            }

            if (!heeftBevestiging(
                    afspraak,
                    betrokkene
            )) {
                afspraak.voegBevestigingToe(
                        betrokkene
                );
            }
        }

        Set<UUID> nieuweAfdelingIds =
                new HashSet<>(
                        nieuweAfdelingen
                                .stream()
                                .map(
                                        Afdeling::getId
                                )
                                .toList()
                );

        List<Afdeling> teVerwijderenAfdelingen =
                afspraak.getBetrokkenAfdelingen()
                        .stream()
                        .filter(afdeling ->
                                !nieuweAfdelingIds.contains(
                                        afdeling.getId()
                                )
                        )
                        .toList();

        for (
                Afdeling afdeling
                : teVerwijderenAfdelingen
        ) {
            afspraak.verwijderAfdeling(
                    afdeling
            );
        }

        Set<UUID> huidigeAfdelingIds =
                new HashSet<>(
                        afspraak.getBetrokkenAfdelingen()
                                .stream()
                                .map(
                                        Afdeling::getId
                                )
                                .toList()
                );

        for (Afdeling afdeling : nieuweAfdelingen) {

            if (!huidigeAfdelingIds.contains(
                    afdeling.getId()
            )) {
                afspraak.voegAfdelingToe(
                        afdeling
                );

                huidigeAfdelingIds.add(
                        afdeling.getId()
                );
            }
        }

        if (!Objects.equals(
                huidigeVerantwoordelijke.getId(),
                nieuweVerantwoordelijke.getId()
        )) {
            processtap.wijzigVerantwoordelijke(
                    nieuweVerantwoordelijke
            );
        }

        afspraakRepository.save(
                afspraak
        );

        processtapRepository.save(
                processtap
        );

        if (!oudeToewijzing.equals(
                nieuweToewijzing
        )) {

            Wijziging wijziging =
                    afspraak.registreerWijziging(
                            uitvoerder,
                            "Toewijzing",
                            oudeToewijzing,
                            nieuweToewijzing
                    );

            wijzigingRepository.save(
                    wijziging
            );
        }

        return afspraakMapper.naarDetailResponse(
                afspraak
        );
    }

    private Gebruiker controleerBevoegdVoorToewijzing(
            OperationeleAfspraak afspraak,
            UUID gebruikerId
    ) {

        if (gebruikerId == null) {
            throw new ForbiddenOperationException(
                    "Gebruiker is niet bevoegd om de toewijzing te wijzigen"
            );
        }

        Gebruiker initiatiefnemer =
                afspraak.getInitiatiefnemer();

        if (gebruikerId.equals(
                initiatiefnemer.getId()
        )) {
            return initiatiefnemer;
        }

        Gebruiker huidigeVerantwoordelijke =
                bepaalHuidigeVerantwoordelijke(
                        afspraak
                );

        if (gebruikerId.equals(
                huidigeVerantwoordelijke.getId()
        )) {
            return huidigeVerantwoordelijke;
        }

        throw new ForbiddenOperationException(
                "Gebruiker is niet bevoegd om de toewijzing te wijzigen"
        );
    }

    private void controleerProcesstapHoortBijAfspraak(
            Processtap processtap,
            OperationeleAfspraak afspraak
    ) {

        if (processtap.getAfspraak() == afspraak) {
            return;
        }

        UUID processtapAfspraakId =
                processtap.getAfspraak()
                        .getId();

        UUID afspraakId =
                afspraak.getId();

        if (processtapAfspraakId == null
                || afspraakId == null
                || !afspraakId.equals(
                processtapAfspraakId
        )) {

            throw new IllegalArgumentException(
                    "Processtap hoort niet bij deze afspraak"
            );
        }
    }

    private boolean heeftBevestiging(
            OperationeleAfspraak afspraak,
            Gebruiker gebruiker
    ) {

        return afspraak.getBevestigingen()
                .stream()
                .anyMatch(bevestiging ->
                        bevestiging.getGebruiker()
                                == gebruiker
                                ||
                                (
                                        gebruiker.getId() != null
                                                &&
                                                gebruiker.getId()
                                                        .equals(
                                                                bevestiging
                                                                        .getGebruiker()
                                                                        .getId()
                                                        )
                                )
                );
    }

    private String beschrijfToewijzing(
            String processtapNaam,
            List<Gebruiker> betrokkenen,
            List<Afdeling> afdelingen,
            Gebruiker verantwoordelijke
    ) {

        List<String> betrokkenNamen =
                betrokkenen.stream()
                        .map(
                                this::beschrijfGebruiker
                        )
                        .distinct()
                        .sorted(
                                String.CASE_INSENSITIVE_ORDER
                        )
                        .toList();

        List<String> afdelingNamen =
                afdelingen.stream()
                        .map(
                                this::beschrijfAfdeling
                        )
                        .distinct()
                        .sorted(
                                String.CASE_INSENSITIVE_ORDER
                        )
                        .toList();

        String betrokkenenTekst =
                betrokkenNamen.isEmpty()
                        ? "Geen"
                        : String.join(
                        ", ",
                        betrokkenNamen
                );

        String afdelingenTekst =
                afdelingNamen.isEmpty()
                        ? "Geen"
                        : String.join(
                        ", ",
                        afdelingNamen
                );

        return "Processtap: "
                + processtapNaam
                + " | Betrokkenen: "
                + betrokkenenTekst
                + " | Afdelingen: "
                + afdelingenTekst
                + " | Verantwoordelijke: "
                + beschrijfGebruiker(
                verantwoordelijke
        );
    }

    private String beschrijfGebruiker(
            Gebruiker gebruiker
    ) {

        if (gebruiker.getNaam() != null
                && !gebruiker.getNaam().isBlank()) {

            return gebruiker.getNaam();
        }

        return gebruiker.getId() == null
                ? "Onbekend"
                : gebruiker.getId().toString();
    }

    private String beschrijfAfdeling(
            Afdeling afdeling
    ) {

        if (afdeling.getNaam() != null
                && !afdeling.getNaam().isBlank()) {

            return afdeling.getNaam();
        }

        return afdeling.getId() == null
                ? "Onbekend"
                : afdeling.getId().toString();
    }

    private void controleerAlleIdsGevonden(
            List<UUID> gevraagdeIds,
            int gevondenAantal,
            String foutmelding
    ) {

        long uniekAantalIds =
                gevraagdeIds.stream()
                        .distinct()
                        .count();

        if (gevondenAantal != uniekAantalIds) {
            throw new IllegalArgumentException(
                    foutmelding
            );
        }
    }

    private Gebruiker bepaalHuidigeVerantwoordelijke(
            OperationeleAfspraak afspraak
    ) {

        List<Processtap> processtappen =
                afspraak.getProcesstappen();

        if (processtappen.isEmpty()) {
            throw new IllegalStateException(
                    "Afspraak heeft geen processtappen"
            );
        }

        return processtappen.stream()
                .filter(processtap ->
                        processtap.getStatus()
                                != ProcesstapStatus.AFGEROND
                )
                .findFirst()
                .orElse(
                        processtappen.get(
                                processtappen.size() - 1
                        )
                )
                .getVerantwoordelijke();
    }

    private void controleerVerantwoordelijke(
            UUID gebruikerId,
            Gebruiker verantwoordelijke,
            String foutmelding
    ) {

        if (gebruikerId == null
                || !gebruikerId.equals(
                verantwoordelijke.getId()
        )) {

            throw new ForbiddenOperationException(
                    foutmelding
            );
        }
    }
}