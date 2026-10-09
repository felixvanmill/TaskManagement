package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.request.UpdateToewijzingRequest;
import nl.outokumpu.afspraken.dto.response.AfspraakDetailResponse;
import nl.outokumpu.afspraken.entity.*;
import nl.outokumpu.afspraken.enums.AfspraakStatus;
import nl.outokumpu.afspraken.enums.BetrokkenRol;
import nl.outokumpu.afspraken.enums.ProcesstapStatus;
import nl.outokumpu.afspraken.enums.UitvoeringsStatus;
import nl.outokumpu.afspraken.mapper.AfspraakMapper;
import nl.outokumpu.afspraken.repository.AfdelingRepository;
import nl.outokumpu.afspraken.repository.GebruikerRepository;
import nl.outokumpu.afspraken.repository.OperationeleAfspraakRepository;
import nl.outokumpu.afspraken.repository.ProcesstapRepository;
import nl.outokumpu.afspraken.repository.WijzigingRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
                                "Processtapstatus: " + processtap.getNaam(),
                                oudeStatus,
                                processtap.getStatus().name()
                        );

        processtapRepository.save(processtap);
        wijzigingRepository.save(wijziging);

        return afspraakMapper.naarDetailResponse(
                processtap.getAfspraak()
        );
    }

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
                                "Processtapstatus: " + processtap.getNaam(),
                                oudeStatus,
                                processtap.getStatus().name()
                        );

        processtapRepository.save(processtap);
        wijzigingRepository.save(wijziging);

        return afspraakMapper.naarDetailResponse(
                processtap.getAfspraak()
        );
    }

    public AfspraakDetailResponse wijzigAfspraakStatus(
            UUID afspraakId,
            AfspraakStatus nieuweStatus,
            UUID gebruikerId
    ) {

        OperationeleAfspraak afspraak =
                afspraakRepository.findById(afspraakId)
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

        afspraakRepository.save(
                afspraak
        );

        wijzigingRepository.save(
                wijziging
        );

        return afspraakMapper.naarDetailResponse(
                afspraak
        );
    }

    public AfspraakDetailResponse wijzigUitvoeringsstatus(
            UUID afspraakId,
            UitvoeringsStatus nieuweStatus,
            UUID gebruikerId
    ) {

        OperationeleAfspraak afspraak =
                afspraakRepository.findById(afspraakId)
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

        afspraakRepository.save(
                orderverplaatsing
        );

        wijzigingRepository.save(
                wijziging
        );

        return afspraakMapper.naarDetailResponse(
                orderverplaatsing
        );
    }

    public AfspraakDetailResponse updateToewijzing(
            UUID afspraakId,
            UpdateToewijzingRequest request,
            UUID gebruikerId
    ) {

        OperationeleAfspraak afspraak =
                afspraakRepository.findById(afspraakId)
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

        String oudeToewijzing =
                beschrijfToewijzing(
                        afspraak.getBetrokkenheden()
                                .stream()
                                .filter(betrokkenheid ->
                                        betrokkenheid.getRol()
                                                == BetrokkenRol.BETROKKENE
                                )
                                .map(Betrokkenheid::getGebruiker)
                                .map(Gebruiker::getId)
                                .toList(),
                        afspraak.getBetrokkenAfdelingen()
                                .stream()
                                .map(Afdeling::getId)
                                .toList(),
                        processtap.getVerantwoordelijke()
                                .getId()
                );

        String nieuweToewijzing =
                beschrijfToewijzing(
                        nieuweBetrokkenen
                                .stream()
                                .map(Gebruiker::getId)
                                .toList(),
                        nieuweAfdelingen
                                .stream()
                                .map(Afdeling::getId)
                                .toList(),
                        nieuweVerantwoordelijke.getId()
                );

        List<Betrokkenheid> huidigeBetrokkenheden =
                new ArrayList<>(
                        afspraak.getBetrokkenheden()
                );

        huidigeBetrokkenheden.stream()
                .filter(betrokkenheid ->
                        betrokkenheid.getRol()
                                == BetrokkenRol.BETROKKENE
                )
                .forEach(
                        afspraak::verwijderBetrokkenheid
                );

        for (Gebruiker betrokkene : nieuweBetrokkenen) {

            afspraak.voegBetrokkenheidToe(
                    betrokkene,
                    BetrokkenRol.BETROKKENE
            );

            if (!heeftBevestiging(
                    afspraak,
                    betrokkene
            )) {
                afspraak.voegBevestigingToe(
                        betrokkene
                );
            }
        }

        List<Afdeling> huidigeAfdelingen =
                new ArrayList<>(
                        afspraak.getBetrokkenAfdelingen()
                );

        for (Afdeling afdeling : huidigeAfdelingen) {
            afspraak.verwijderAfdeling(
                    afdeling
            );
        }

        for (Afdeling afdeling : nieuweAfdelingen) {
            afspraak.voegAfdelingToe(
                    afdeling
            );
        }

        UUID huidigeVerantwoordelijkeId =
                processtap.getVerantwoordelijke()
                        .getId();

        if (!Objects.equals(
                huidigeVerantwoordelijkeId,
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
            throw new IllegalArgumentException(
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

        throw new IllegalArgumentException(
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
            List<UUID> betrokkeneIds,
            List<UUID> afdelingIds,
            UUID verantwoordelijkeId
    ) {

        List<UUID> gesorteerdeBetrokkeneIds =
                betrokkeneIds.stream()
                        .distinct()
                        .sorted()
                        .toList();

        List<UUID> gesorteerdeAfdelingIds =
                afdelingIds.stream()
                        .distinct()
                        .sorted()
                        .toList();

        return "betrokkenen="
                + gesorteerdeBetrokkeneIds
                + "; afdelingen="
                + gesorteerdeAfdelingIds
                + "; verantwoordelijke="
                + verantwoordelijkeId;
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

            throw new IllegalArgumentException(
                    foutmelding
            );
        }
    }
}