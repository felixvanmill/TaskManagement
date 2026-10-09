package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.data.CapaciteitswisselData;
import nl.outokumpu.afspraken.dto.data.OrderverplaatsingData;
import nl.outokumpu.afspraken.dto.request.CreateAfspraakRequest;
import nl.outokumpu.afspraken.dto.request.UpdateAfspraakRequest;
import nl.outokumpu.afspraken.dto.response.AfspraakDetailResponse;
import nl.outokumpu.afspraken.dto.response.AfspraakSummaryResponse;
import nl.outokumpu.afspraken.entity.*;
import nl.outokumpu.afspraken.enums.AfspraakStatus;
import nl.outokumpu.afspraken.enums.BetrokkenRol;
import nl.outokumpu.afspraken.enums.ProcesstapStatus;
import nl.outokumpu.afspraken.mapper.AfspraakMapper;
import nl.outokumpu.afspraken.repository.AfdelingRepository;
import nl.outokumpu.afspraken.repository.GebruikerRepository;
import nl.outokumpu.afspraken.repository.OperationeleAfspraakRepository;
import nl.outokumpu.afspraken.repository.WijzigingRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class AfspraakService {

    private final OperationeleAfspraakRepository afspraakRepository;
    private final GebruikerRepository gebruikerRepository;
    private final AfdelingRepository afdelingRepository;
    private final WijzigingRepository wijzigingRepository;
    private final AfspraakMapper afspraakMapper;

    public AfspraakService(
            OperationeleAfspraakRepository afspraakRepository,
            GebruikerRepository gebruikerRepository,
            AfdelingRepository afdelingRepository,
            WijzigingRepository wijzigingRepository,
            AfspraakMapper afspraakMapper
    ) {
        this.afspraakRepository = afspraakRepository;
        this.gebruikerRepository = gebruikerRepository;
        this.afdelingRepository = afdelingRepository;
        this.wijzigingRepository = wijzigingRepository;
        this.afspraakMapper = afspraakMapper;
    }

    public AfspraakDetailResponse createAfspraak(
            CreateAfspraakRequest request,
            UUID initiatiefnemerId
    ) {

        Gebruiker initiatiefnemer =
                gebruikerRepository.findById(initiatiefnemerId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Initiatiefnemer niet gevonden"
                                )
                        );

        Gebruiker verantwoordelijke =
                gebruikerRepository.findById(
                                request.verantwoordelijkeId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Verantwoordelijke niet gevonden"
                                )
                        );

        OperationeleAfspraak afspraak =
                maakAfspraakOpBasisVanType(
                        request,
                        initiatiefnemer
                );

        afspraak.voegProcesstapToe(
                request.eersteProcesstapNaam(),
                1,
                request.eersteProcesstapDeadline(),
                verantwoordelijke
        );

        List<Gebruiker> betrokkenGebruikers =
                gebruikerRepository.findAllById(
                        request.betrokkeneIds()
                );

        controleerAlleIdsGevonden(
                request.betrokkeneIds(),
                betrokkenGebruikers.size(),
                "Een of meer betrokken gebruikers zijn niet gevonden"
        );

        for (Gebruiker gebruiker : betrokkenGebruikers) {

            afspraak.voegBetrokkenheidToe(
                    gebruiker,
                    BetrokkenRol.BETROKKENE
            );

            afspraak.voegBevestigingToe(
                    gebruiker
            );
        }

        List<Gebruiker> goedkeurders =
                gebruikerRepository.findAllById(
                        request.goedkeurderIds()
                );

        controleerAlleIdsGevonden(
                request.goedkeurderIds(),
                goedkeurders.size(),
                "Een of meer goedkeurders zijn niet gevonden"
        );

        for (Gebruiker goedkeurder : goedkeurders) {

            afspraak.voegBetrokkenheidToe(
                    goedkeurder,
                    BetrokkenRol.GOEDKEURDER
            );

            boolean heeftAlBevestiging =
                    afspraak.getBevestigingen()
                            .stream()
                            .anyMatch(bevestiging ->
                                    bevestiging.getGebruiker()
                                            == goedkeurder
                                            ||
                                            (
                                                    goedkeurder.getId() != null
                                                            &&
                                                            goedkeurder.getId()
                                                                    .equals(
                                                                            bevestiging
                                                                                    .getGebruiker()
                                                                                    .getId()
                                                                    )
                                            )
                            );

            if (!heeftAlBevestiging) {
                afspraak.voegBevestigingToe(
                        goedkeurder
                );
            }
        }

        List<Afdeling> betrokkenAfdelingen =
                afdelingRepository.findAllById(
                        request.afdelingIds()
                );

        controleerAlleIdsGevonden(
                request.afdelingIds(),
                betrokkenAfdelingen.size(),
                "Een of meer afdelingen zijn niet gevonden"
        );

        for (Afdeling afdeling : betrokkenAfdelingen) {
            afspraak.voegAfdelingToe(
                    afdeling
            );
        }

        OperationeleAfspraak opgeslagenAfspraak =
                afspraakRepository.save(
                        afspraak
                );

        return afspraakMapper.naarDetailResponse(
                opgeslagenAfspraak
        );
    }

    public AfspraakDetailResponse updateAfspraak(
            UUID afspraakId,
            UpdateAfspraakRequest request,
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
                bepaalBevoegdeGebruiker(
                        afspraak,
                        gebruikerId
                );

        controleerUpdateDataPastBijType(
                afspraak,
                request
        );

        Map<String, String> oudeGegevens =
                verzamelBewerkbareGegevens(
                        afspraak
                );

        afspraak.wijzigGegevens(
                request.titel(),
                request.beschrijving(),
                request.reden(),
                request.achtergrond(),
                request.aannames(),
                request.ingangsdatum(),
                request.deadline()
        );

        wijzigTypespecifiekeGegevens(
                afspraak,
                request
        );

        Map<String, String> nieuweGegevens =
                verzamelBewerkbareGegevens(
                        afspraak
                );

        List<Wijziging> wijzigingen =
                registreerGewijzigdeVelden(
                        afspraak,
                        uitvoerder,
                        oudeGegevens,
                        nieuweGegevens
                );

        if (wijzigingen.isEmpty()) {
            throw new IllegalArgumentException(
                    "Er zijn geen wijzigingen om op te slaan"
            );
        }

        afspraakRepository.save(
                afspraak
        );

        wijzigingRepository.saveAll(
                wijzigingen
        );

        return afspraakMapper.naarDetailResponse(
                afspraak
        );
    }

    @Transactional(readOnly = true)
    public AfspraakDetailResponse vindAfspraak(
            UUID afspraakId
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

        return afspraakMapper.naarDetailResponse(
                afspraak
        );
    }

    @Transactional(readOnly = true)
    public List<AfspraakSummaryResponse> vindAlleAfspraken() {

        return afspraakRepository.findAll()
                .stream()
                .map(
                        afspraakMapper::naarSummaryResponse
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AfspraakSummaryResponse> vindActieveAfspraken() {

        return afspraakRepository
                .findByStatusNotOrderByLaatstGewijzigdOpDesc(
                        AfspraakStatus.AFGEROND
                )
                .stream()
                .map(
                        afspraakMapper::naarSummaryResponse
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AfspraakSummaryResponse> vindAfgerondeAfspraken() {

        return afspraakRepository
                .findByStatusOrderByLaatstGewijzigdOpDesc(
                        AfspraakStatus.AFGEROND
                )
                .stream()
                .map(
                        afspraakMapper::naarSummaryResponse
                )
                .toList();
    }

    private OperationeleAfspraak maakAfspraakOpBasisVanType(
            CreateAfspraakRequest request,
            Gebruiker initiatiefnemer
    ) {

        return switch (request.type()) {

            case ALGEMEEN -> {

                if (request.capaciteitswissel() != null
                        || request.orderverplaatsing() != null) {

                    throw new IllegalArgumentException(
                            "Een algemene afspraak mag geen typespecifieke gegevens bevatten"
                    );
                }

                yield new OperationeleAfspraak(
                        request.titel(),
                        request.beschrijving(),
                        request.reden(),
                        request.achtergrond(),
                        request.aannames(),
                        request.ingangsdatum(),
                        request.deadline(),
                        initiatiefnemer
                );
            }

            case CAPACITEITSWISSEL -> {

                CapaciteitswisselData data =
                        request.capaciteitswissel();

                if (data == null) {
                    throw new IllegalArgumentException(
                            "Gegevens voor capaciteitswissel zijn verplicht"
                    );
                }

                if (request.orderverplaatsing() != null) {
                    throw new IllegalArgumentException(
                            "Een capaciteitswissel mag geen orderverplaatsingsgegevens bevatten"
                    );
                }

                yield new Capaciteitswissel(
                        request.titel(),
                        request.beschrijving(),
                        request.reden(),
                        request.achtergrond(),
                        request.aannames(),
                        request.ingangsdatum(),
                        request.deadline(),
                        initiatiefnemer,
                        data.fabriek(),
                        data.finishType(),
                        data.grade(),
                        data.capaciteitVan(),
                        data.capaciteitNaar(),
                        data.capaciteitEenheid(),
                        data.periodeVan(),
                        data.periodeTot()
                );
            }

            case ORDERVERPLAATSING -> {

                OrderverplaatsingData data =
                        request.orderverplaatsing();

                if (data == null) {
                    throw new IllegalArgumentException(
                            "Gegevens voor orderverplaatsing zijn verplicht"
                    );
                }

                if (request.capaciteitswissel() != null) {
                    throw new IllegalArgumentException(
                            "Een orderverplaatsing mag geen capaciteitswisselgegevens bevatten"
                    );
                }

                yield new Orderverplaatsing(
                        request.titel(),
                        request.beschrijving(),
                        request.reden(),
                        request.achtergrond(),
                        request.aannames(),
                        request.ingangsdatum(),
                        request.deadline(),
                        initiatiefnemer,
                        data.vanFabriek(),
                        data.naarFabriek(),
                        data.finishType(),
                        data.totaalVolumeTons(),
                        data.gewensteVerplaatsingsdatum()
                );
            }
        };
    }

    private Gebruiker bepaalBevoegdeGebruiker(
            OperationeleAfspraak afspraak,
            UUID gebruikerId
    ) {

        if (gebruikerId == null) {
            throw new IllegalArgumentException(
                    "Gebruiker is niet bevoegd om de afspraak te wijzigen"
            );
        }

        Gebruiker initiatiefnemer =
                afspraak.getInitiatiefnemer();

        if (gebruikerId.equals(
                initiatiefnemer.getId()
        )) {
            return initiatiefnemer;
        }

        List<Processtap> processtappen =
                afspraak.getProcesstappen();

        if (!processtappen.isEmpty()) {

            Gebruiker huidigeVerantwoordelijke =
                    processtappen.stream()
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

            if (gebruikerId.equals(
                    huidigeVerantwoordelijke.getId()
            )) {
                return huidigeVerantwoordelijke;
            }
        }

        throw new IllegalArgumentException(
                "Gebruiker is niet bevoegd om de afspraak te wijzigen"
        );
    }

    private void controleerUpdateDataPastBijType(
            OperationeleAfspraak afspraak,
            UpdateAfspraakRequest request
    ) {

        if (afspraak instanceof Capaciteitswissel) {

            if (request.capaciteitswissel() == null) {
                throw new IllegalArgumentException(
                        "Gegevens voor capaciteitswissel zijn verplicht"
                );
            }

            if (request.orderverplaatsing() != null) {
                throw new IllegalArgumentException(
                        "Een capaciteitswissel mag geen orderverplaatsingsgegevens bevatten"
                );
            }

            return;
        }

        if (afspraak instanceof Orderverplaatsing) {

            if (request.orderverplaatsing() == null) {
                throw new IllegalArgumentException(
                        "Gegevens voor orderverplaatsing zijn verplicht"
                );
            }

            if (request.capaciteitswissel() != null) {
                throw new IllegalArgumentException(
                        "Een orderverplaatsing mag geen capaciteitswisselgegevens bevatten"
                );
            }

            return;
        }

        if (request.capaciteitswissel() != null
                || request.orderverplaatsing() != null) {

            throw new IllegalArgumentException(
                    "Een algemene afspraak mag geen typespecifieke gegevens bevatten"
            );
        }
    }

    private void wijzigTypespecifiekeGegevens(
            OperationeleAfspraak afspraak,
            UpdateAfspraakRequest request
    ) {

        if (afspraak instanceof Capaciteitswissel capaciteitswissel) {

            CapaciteitswisselData data =
                    request.capaciteitswissel();

            capaciteitswissel.wijzigCapaciteitsgegevens(
                    data.fabriek(),
                    data.finishType(),
                    data.grade(),
                    data.capaciteitVan(),
                    data.capaciteitNaar(),
                    data.capaciteitEenheid(),
                    data.periodeVan(),
                    data.periodeTot()
            );

            return;
        }

        if (afspraak instanceof Orderverplaatsing orderverplaatsing) {

            OrderverplaatsingData data =
                    request.orderverplaatsing();

            orderverplaatsing.wijzigOrdergegevens(
                    data.vanFabriek(),
                    data.naarFabriek(),
                    data.finishType(),
                    data.totaalVolumeTons(),
                    data.gewensteVerplaatsingsdatum()
            );
        }
    }

    private Map<String, String> verzamelBewerkbareGegevens(
            OperationeleAfspraak afspraak
    ) {

        Map<String, String> gegevens =
                new LinkedHashMap<>();

        gegevens.put(
                "Titel",
                afspraak.getTitel()
        );

        gegevens.put(
                "Beschrijving",
                afspraak.getBeschrijving()
        );

        gegevens.put(
                "Reden",
                afspraak.getReden()
        );

        gegevens.put(
                "Achtergrond",
                afspraak.getAchtergrond()
        );

        gegevens.put(
                "Aannames",
                afspraak.getAannames()
        );

        gegevens.put(
                "Ingangsdatum",
                alsTekst(
                        afspraak.getIngangsdatum()
                )
        );

        gegevens.put(
                "Deadline",
                alsTekst(
                        afspraak.getDeadline()
                )
        );

        if (afspraak instanceof Capaciteitswissel capaciteitswissel) {

            gegevens.put(
                    "Fabriek",
                    capaciteitswissel.getFabriek()
            );

            gegevens.put(
                    "Finish type",
                    capaciteitswissel.getFinishType()
            );

            gegevens.put(
                    "Grade",
                    capaciteitswissel.getGrade()
            );

            gegevens.put(
                    "Capaciteit van",
                    alsTekst(
                            capaciteitswissel.getCapaciteitVan()
                    )
            );

            gegevens.put(
                    "Capaciteit naar",
                    alsTekst(
                            capaciteitswissel.getCapaciteitNaar()
                    )
            );

            gegevens.put(
                    "Capaciteitseenheid",
                    capaciteitswissel.getCapaciteitEenheid()
            );

            gegevens.put(
                    "Periode van",
                    alsTekst(
                            capaciteitswissel.getPeriodeVan()
                    )
            );

            gegevens.put(
                    "Periode tot",
                    alsTekst(
                            capaciteitswissel.getPeriodeTot()
                    )
            );
        }

        if (afspraak instanceof Orderverplaatsing orderverplaatsing) {

            gegevens.put(
                    "Fabriek van herkomst",
                    orderverplaatsing.getVanFabriek()
            );

            gegevens.put(
                    "Bestemmingsfabriek",
                    orderverplaatsing.getNaarFabriek()
            );

            gegevens.put(
                    "Finish type",
                    orderverplaatsing.getFinishType()
            );

            gegevens.put(
                    "Totaal volume tons",
                    alsTekst(
                            orderverplaatsing.getTotaalVolumeTons()
                    )
            );

            gegevens.put(
                    "Gewenste verplaatsingsdatum",
                    alsTekst(
                            orderverplaatsing
                                    .getGewensteVerplaatsingsdatum()
                    )
            );
        }

        return gegevens;
    }

    private List<Wijziging> registreerGewijzigdeVelden(
            OperationeleAfspraak afspraak,
            Gebruiker uitvoerder,
            Map<String, String> oudeGegevens,
            Map<String, String> nieuweGegevens
    ) {

        List<Wijziging> wijzigingen =
                new ArrayList<>();

        for (Map.Entry<String, String> entry
                : oudeGegevens.entrySet()) {

            String onderdeel =
                    entry.getKey();

            String oudeWaarde =
                    entry.getValue();

            String nieuweWaarde =
                    nieuweGegevens.get(
                            onderdeel
                    );

            if (!Objects.equals(
                    oudeWaarde,
                    nieuweWaarde
            )) {

                Wijziging wijziging =
                        afspraak.registreerWijziging(
                                uitvoerder,
                                onderdeel,
                                oudeWaarde,
                                nieuweWaarde
                        );

                wijzigingen.add(
                        wijziging
                );
            }
        }

        return wijzigingen;
    }

    private String alsTekst(
            Object waarde
    ) {

        return waarde == null
                ? null
                : waarde.toString();
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
}