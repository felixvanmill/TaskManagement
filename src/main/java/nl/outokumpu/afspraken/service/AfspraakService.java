package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.request.UpdateGoedkeurdersRequest;
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
import nl.outokumpu.afspraken.dto.request.AfspraakFilterRequest;
import nl.outokumpu.afspraken.repository.specification.AfspraakSpecificaties;
import nl.outokumpu.afspraken.exception.ForbiddenOperationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages operational agreements, their participants and approvers, and field-level change history.
 */
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
            AfspraakMapper afspraakMapper) {
        this.afspraakRepository = afspraakRepository;
        this.gebruikerRepository = gebruikerRepository;
        this.afdelingRepository = afdelingRepository;
        this.wijzigingRepository = wijzigingRepository;
        this.afspraakMapper = afspraakMapper;
    }

    /**
     * Creates a typed agreement with its first process step, participants, approvers and departments.
     * Participants and approvers share one confirmation per user, even when a user holds both roles.
     *
     * @param request agreement details and references to existing users and departments
     * @param initiatiefnemerId the user creating the agreement
     * @return the created agreement with its related details
     * @throws IllegalArgumentException if a referenced entity is missing or the supplied data does not match the agreement type
     */
    public AfspraakDetailResponse createAfspraak(CreateAfspraakRequest request, UUID initiatiefnemerId) {
        Gebruiker initiatiefnemer = gebruikerRepository.findById(initiatiefnemerId).orElseThrow(() ->
                new IllegalArgumentException("Initiatiefnemer niet gevonden"));

        Gebruiker verantwoordelijke = gebruikerRepository.findById(request.verantwoordelijkeId()).orElseThrow(() ->
                new IllegalArgumentException("Verantwoordelijke niet gevonden"));

        OperationeleAfspraak afspraak = maakAfspraakOpBasisVanType(request, initiatiefnemer);

        afspraak.voegProcesstapToe(
                request.eersteProcesstapNaam(),
                1,
                request.eersteProcesstapDeadline(),
                verantwoordelijke);

        List<Gebruiker> betrokkenGebruikers = gebruikerRepository.findAllById(request.betrokkeneIds());

        controleerAlleIdsGevonden(
                request.betrokkeneIds(),
                betrokkenGebruikers.size(),
                "Een of meer betrokken gebruikers zijn niet gevonden");

        for (Gebruiker gebruiker : betrokkenGebruikers) {
            afspraak.voegBetrokkenheidToe(gebruiker, BetrokkenRol.BETROKKENE);

            afspraak.voegBevestigingToe(gebruiker);
        }

        List<Gebruiker> goedkeurders = gebruikerRepository.findAllById(request.goedkeurderIds());

        controleerAlleIdsGevonden(
                request.goedkeurderIds(),
                goedkeurders.size(),
                "Een of meer goedkeurders zijn niet gevonden");

        for (Gebruiker goedkeurder : goedkeurders) {
            afspraak.voegBetrokkenheidToe(goedkeurder, BetrokkenRol.GOEDKEURDER);

            boolean heeftAlBevestiging = afspraak.getBevestigingen().stream()
                    .anyMatch(bevestiging ->
                            bevestiging.getGebruiker() == goedkeurder
                                    || (goedkeurder.getId() != null
                                            && goedkeurder.getId().equals(bevestiging.getGebruiker().getId())));

            if (!heeftAlBevestiging) {
                afspraak.voegBevestigingToe(goedkeurder);
            }
        }

        List<Afdeling> betrokkenAfdelingen = afdelingRepository.findAllById(request.afdelingIds());

        controleerAlleIdsGevonden(
                request.afdelingIds(),
                betrokkenAfdelingen.size(),
                "Een of meer afdelingen zijn niet gevonden");

        for (Afdeling afdeling : betrokkenAfdelingen) {
            afspraak.voegAfdelingToe(afdeling);
        }

        OperationeleAfspraak opgeslagenAfspraak = afspraakRepository.save(afspraak);

        return afspraakMapper.naarDetailResponse(opgeslagenAfspraak);
    }

    /**
     * Updates editable fields and records a history entry for each changed value.
     * Only the initiator or current responsible user may edit the agreement. If all process steps
     * are completed, the responsible user of the last step is used.
     *
     * @param afspraakId the agreement to update
     * @param request replacement values for the editable fields
     * @param gebruikerId the user performing the update
     * @return the updated agreement, mapped after persistence callbacks have run
     * @throws ForbiddenOperationException if the user is neither the initiator nor the current responsible user
     * @throws IllegalArgumentException if the agreement is missing, the data is invalid, or no values have changed
     */
    public AfspraakDetailResponse updateAfspraak(UUID afspraakId, UpdateAfspraakRequest request, UUID gebruikerId) {
        OperationeleAfspraak afspraak = afspraakRepository.findById(afspraakId).orElseThrow(() ->
                new IllegalArgumentException("Afspraak niet gevonden"));

        Gebruiker uitvoerder = bepaalBevoegdeGebruiker(afspraak, gebruikerId);

        controleerUpdateDataPastBijType(afspraak, request);

        Map<String, String> oudeGegevens = verzamelBewerkbareGegevens(afspraak);

        afspraak.wijzigGegevens(
                request.titel(),
                request.beschrijving(),
                request.reden(),
                request.achtergrond(),
                request.aannames(),
                request.ingangsdatum(),
                request.deadline());

        wijzigTypespecifiekeGegevens(afspraak, request);

        Map<String, String> nieuweGegevens = verzamelBewerkbareGegevens(afspraak);

        List<Wijziging> wijzigingen = registreerGewijzigdeVelden(afspraak, uitvoerder, oudeGegevens, nieuweGegevens);

        if (wijzigingen.isEmpty()) {
            throw new IllegalArgumentException("Er zijn geen wijzigingen om op te slaan");
        }

        /*
         * The agreement was loaded within this transaction and is already managed.
         * JPA dirty checking persists its changes without another save call.
         * New history entries are saved explicitly.
         */
        wijzigingRepository.saveAll(wijzigingen);

        /*
         * Flush before mapping so agreement changes and persistence callbacks,
         * including @PreUpdate and history ID/timestamp generation, are reflected
         * in the response.
         */
        afspraakRepository.flush();

        return afspraakMapper.naarDetailResponse(afspraak);
    }

    /**
     * Replaces the designated approvers; only the agreement's initiator may do so.
     * Existing confirmations are retained, including those of removed approvers.
     * An unchanged approver set returns the current details without a history entry.
     *
     * @param afspraakId the agreement whose approvers are being replaced
     * @param request the complete set of desired approver IDs
     * @param gebruikerId the user performing the change
     * @return the agreement with its current approvers
     * @throws ForbiddenOperationException if the user is not the initiator
     * @throws IllegalArgumentException if the agreement or a requested approver is missing
     */
    public AfspraakDetailResponse wijzigGoedkeurders(
            UUID afspraakId,
            UpdateGoedkeurdersRequest request,
            UUID gebruikerId) {
        OperationeleAfspraak afspraak = afspraakRepository.findById(afspraakId).orElseThrow(() ->
                new IllegalArgumentException("Afspraak niet gevonden"));

        Gebruiker initiatiefnemer = controleerInitiatiefnemerVoorGoedkeurders(afspraak, gebruikerId);

        List<Gebruiker> nieuweGoedkeurders = gebruikerRepository.findAllById(request.goedkeurderIds());

        controleerAlleIdsGevonden(
                request.goedkeurderIds(),
                nieuweGoedkeurders.size(),
                "Een of meer goedkeurders zijn niet gevonden");

        List<Gebruiker> huidigeGoedkeurders = afspraak.getBetrokkenheden().stream()
                .filter(betrokkenheid -> betrokkenheid.getRol() == BetrokkenRol.GOEDKEURDER)
                .map(Betrokkenheid::getGebruiker)
                .toList();

        List<UUID> huidigeIds = gesorteerdeGebruikerIds(huidigeGoedkeurders);

        List<UUID> nieuweIds = gesorteerdeGebruikerIds(nieuweGoedkeurders);

        if (huidigeIds.equals(nieuweIds)) {
            return afspraakMapper.naarDetailResponse(afspraak);
        }

        Set<UUID> nieuweGoedkeurderIds = new HashSet<>(nieuweIds);

        List<Betrokkenheid> teVerwijderenGoedkeurders = afspraak.getBetrokkenheden().stream()
                .filter(betrokkenheid -> betrokkenheid.getRol() == BetrokkenRol.GOEDKEURDER)
                .filter(betrokkenheid -> !nieuweGoedkeurderIds.contains(betrokkenheid.getGebruiker().getId()))
                .toList();

        for (Betrokkenheid betrokkenheid : teVerwijderenGoedkeurders) {
            afspraak.verwijderBetrokkenheid(betrokkenheid);
        }

        Set<UUID> huidigeGoedkeurderIds = new HashSet<>(
                afspraak.getBetrokkenheden().stream().filter(betrokkenheid ->
                        betrokkenheid.getRol()
                        == BetrokkenRol.GOEDKEURDER).map(Betrokkenheid::getGebruiker).map(Gebruiker::getId).toList());

        for (Gebruiker goedkeurder : nieuweGoedkeurders) {
            if (!huidigeGoedkeurderIds.contains(goedkeurder.getId())) {
                afspraak.voegBetrokkenheidToe(goedkeurder, BetrokkenRol.GOEDKEURDER);

                huidigeGoedkeurderIds.add(goedkeurder.getId());
            }

            if (!heeftBevestiging(afspraak, goedkeurder)) {
                afspraak.voegBevestigingToe(goedkeurder);
            }
        }

        Wijziging wijziging = afspraak.registreerWijziging(
                initiatiefnemer,
                "Goedkeurders",
                huidigeIds.toString(),
                nieuweIds.toString());

        /*
         * The agreement is already managed within this transaction.
         * Dirty checking and orphanRemoval persist the changed participants
         * without another save call for the agreement.
         */
        wijzigingRepository.save(wijziging);

        /*
         * Flush database changes before mapping the response.
         */
        afspraakRepository.flush();

        return afspraakMapper.naarDetailResponse(afspraak);
    }

    @Transactional(readOnly = true)
    public AfspraakDetailResponse vindAfspraak(UUID afspraakId) {
        OperationeleAfspraak afspraak = afspraakRepository.findById(afspraakId).orElseThrow(() ->
                new IllegalArgumentException("Afspraak niet gevonden"));

        return afspraakMapper.naarDetailResponse(afspraak);
    }

    @Transactional(readOnly = true)
    public List<AfspraakSummaryResponse> vindAlleAfspraken() {
        return afspraakRepository.findAll().stream()
                .map(afspraakMapper::naarSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AfspraakSummaryResponse> vindActieveAfspraken() {
        return afspraakRepository.findByStatusNotOrderByLaatstGewijzigdOpDesc(AfspraakStatus.AFGEROND).stream()
                .map(afspraakMapper::naarSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AfspraakSummaryResponse> vindAfgerondeAfspraken() {
        return afspraakRepository.findByStatusOrderByLaatstGewijzigdOpDesc(AfspraakStatus.AFGEROND).stream()
                .map(afspraakMapper::naarSummaryResponse)
                .toList();
    }

    /**
     * Searches agreement text using a trimmed, case-insensitive keyword match.
     *
     * @param zoekterm the required, non-blank search term
     * @return matching agreements ordered by most recent modification first
     * @throws IllegalArgumentException if the search term is null or blank
     */
    @Transactional(readOnly = true)
    public List<AfspraakSummaryResponse> zoekAfspraken(String zoekterm) {
        if (zoekterm == null || zoekterm.isBlank()) {
            throw new IllegalArgumentException("Zoekterm is verplicht");
        }

        return afspraakRepository.zoekOpTrefwoord(zoekterm.trim()).stream()
                .map(afspraakMapper::naarSummaryResponse)
                .toList();
    }

    /**
     * Combines the supplied filters and orders results by most recent modification first.
     * The date range matches agreements whose effective-date-to-deadline interval overlaps it.
     * The responsible-user filter matches any process step, not only the current step.
     *
     * @param filter the filter criteria; individual criteria may be omitted
     * @return agreements satisfying the supplied criteria
     * @throws IllegalArgumentException if the filter is null or its start date is after its end date
     */
    @Transactional(readOnly = true)
    public List<AfspraakSummaryResponse> filterAfspraken(AfspraakFilterRequest filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Filter is verplicht");
        }

        if (filter.periodeVan() != null
                && filter.periodeTot() != null
                && filter.periodeVan().isAfter(filter.periodeTot())) {
            throw new IllegalArgumentException("Periode van mag niet na periode tot liggen");
        }

        Sort sortering = Sort.by(Sort.Direction.DESC, "laatstGewijzigdOp");

        return afspraakRepository.findAll(AfspraakSpecificaties.metFilters(filter), sortering).stream()
                .map(afspraakMapper::naarSummaryResponse)
                .toList();
    }

    private OperationeleAfspraak maakAfspraakOpBasisVanType(CreateAfspraakRequest request, Gebruiker initiatiefnemer) {
        return switch (request.type()) {
            case ALGEMEEN -> {
                if (request.capaciteitswissel() != null || request.orderverplaatsing() != null) {
                    throw new IllegalArgumentException(
                            "Een algemene afspraak mag geen typespecifieke gegevens bevatten");
                }

                yield new OperationeleAfspraak(
                        request.titel(),
                        request.beschrijving(),
                        request.reden(),
                        request.achtergrond(),
                        request.aannames(),
                        request.ingangsdatum(),
                        request.deadline(),
                        initiatiefnemer);
            }

            case CAPACITEITSWISSEL -> {
                CapaciteitswisselData data = request.capaciteitswissel();

                if (data == null) {
                    throw new IllegalArgumentException("Gegevens voor capaciteitswissel zijn verplicht");
                }

                if (request.orderverplaatsing() != null) {
                    throw new IllegalArgumentException(
                            "Een capaciteitswissel mag geen orderverplaatsingsgegevens bevatten");
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
                        data.periodeTot());
            }

            case ORDERVERPLAATSING -> {
                OrderverplaatsingData data = request.orderverplaatsing();

                if (data == null) {
                    throw new IllegalArgumentException("Gegevens voor orderverplaatsing zijn verplicht");
                }

                if (request.capaciteitswissel() != null) {
                    throw new IllegalArgumentException(
                            "Een orderverplaatsing mag geen capaciteitswisselgegevens bevatten");
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
                        data.gewensteVerplaatsingsdatum());
            }
        };
    }

    private Gebruiker bepaalBevoegdeGebruiker(OperationeleAfspraak afspraak, UUID gebruikerId) {
        if (gebruikerId == null) {
            throw new ForbiddenOperationException("Gebruiker is niet bevoegd om de afspraak te wijzigen");
        }

        Gebruiker initiatiefnemer = afspraak.getInitiatiefnemer();

        if (gebruikerId.equals(initiatiefnemer.getId())) {
            return initiatiefnemer;
        }

        List<Processtap> processtappen = afspraak.getProcesstappen();

        if (!processtappen.isEmpty()) {
            Gebruiker huidigeVerantwoordelijke = processtappen.stream()
                    .filter(processtap -> processtap.getStatus() != ProcesstapStatus.AFGEROND)
                    .findFirst()
                    .orElse(processtappen.get(processtappen.size() - 1))
                    .getVerantwoordelijke();

            if (gebruikerId.equals(huidigeVerantwoordelijke.getId())) {
                return huidigeVerantwoordelijke;
            }
        }

        throw new ForbiddenOperationException("Gebruiker is niet bevoegd om de afspraak te wijzigen");
    }

    private Gebruiker controleerInitiatiefnemerVoorGoedkeurders(OperationeleAfspraak afspraak, UUID gebruikerId) {
        Gebruiker initiatiefnemer = afspraak.getInitiatiefnemer();

        if (gebruikerId == null || !gebruikerId.equals(initiatiefnemer.getId())) {
            throw new ForbiddenOperationException("Alleen de initiatiefnemer kan goedkeurders wijzigen");
        }

        return initiatiefnemer;
    }

    private boolean heeftBevestiging(OperationeleAfspraak afspraak, Gebruiker gebruiker) {
        return afspraak.getBevestigingen().stream()
                .anyMatch(bevestiging ->
                        bevestiging.getGebruiker() == gebruiker
                                || (gebruiker.getId() != null
                                        && gebruiker.getId().equals(bevestiging.getGebruiker().getId())));
    }

    private List<UUID> gesorteerdeGebruikerIds(List<Gebruiker> gebruikers) {
        return gebruikers.stream()
                .map(Gebruiker::getId)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
    }

    private void controleerUpdateDataPastBijType(OperationeleAfspraak afspraak, UpdateAfspraakRequest request) {
        if (afspraak instanceof Capaciteitswissel) {
            if (request.capaciteitswissel() == null) {
                throw new IllegalArgumentException("Gegevens voor capaciteitswissel zijn verplicht");
            }

            if (request.orderverplaatsing() != null) {
                throw new IllegalArgumentException(
                        "Een capaciteitswissel mag geen orderverplaatsingsgegevens bevatten");
            }

            return;
        }

        if (afspraak instanceof Orderverplaatsing) {
            if (request.orderverplaatsing() == null) {
                throw new IllegalArgumentException("Gegevens voor orderverplaatsing zijn verplicht");
            }

            if (request.capaciteitswissel() != null) {
                throw new IllegalArgumentException("Een orderverplaatsing mag geen capaciteitswisselgegevens bevatten");
            }

            return;
        }

        if (request.capaciteitswissel() != null || request.orderverplaatsing() != null) {
            throw new IllegalArgumentException("Een algemene afspraak mag geen typespecifieke gegevens bevatten");
        }
    }

    private void wijzigTypespecifiekeGegevens(OperationeleAfspraak afspraak, UpdateAfspraakRequest request) {
        if (afspraak instanceof Capaciteitswissel capaciteitswissel) {
            CapaciteitswisselData data = request.capaciteitswissel();

            capaciteitswissel.wijzigCapaciteitsgegevens(
                    data.fabriek(),
                    data.finishType(),
                    data.grade(),
                    data.capaciteitVan(),
                    data.capaciteitNaar(),
                    data.capaciteitEenheid(),
                    data.periodeVan(),
                    data.periodeTot());

            return;
        }

        if (afspraak instanceof Orderverplaatsing orderverplaatsing) {
            OrderverplaatsingData data = request.orderverplaatsing();

            orderverplaatsing.wijzigOrdergegevens(
                    data.vanFabriek(),
                    data.naarFabriek(),
                    data.finishType(),
                    data.totaalVolumeTons(),
                    data.gewensteVerplaatsingsdatum());
        }
    }

    private Map<String, String> verzamelBewerkbareGegevens(OperationeleAfspraak afspraak) {
        Map<String, String> gegevens = new LinkedHashMap<>();

        gegevens.put("Titel", afspraak.getTitel());

        gegevens.put("Beschrijving", afspraak.getBeschrijving());

        gegevens.put("Reden", afspraak.getReden());

        gegevens.put("Achtergrond", afspraak.getAchtergrond());

        gegevens.put("Aannames", afspraak.getAannames());

        gegevens.put("Ingangsdatum", alsTekst(afspraak.getIngangsdatum()));

        gegevens.put("Deadline", alsTekst(afspraak.getDeadline()));

        if (afspraak instanceof Capaciteitswissel capaciteitswissel) {
            gegevens.put("Fabriek", capaciteitswissel.getFabriek());

            gegevens.put("Finish type", capaciteitswissel.getFinishType());

            gegevens.put("Grade", capaciteitswissel.getGrade());

            gegevens.put("Capaciteit van", alsTekst(capaciteitswissel.getCapaciteitVan()));

            gegevens.put("Capaciteit naar", alsTekst(capaciteitswissel.getCapaciteitNaar()));

            gegevens.put("Capaciteitseenheid", capaciteitswissel.getCapaciteitEenheid());

            gegevens.put("Periode van", alsTekst(capaciteitswissel.getPeriodeVan()));

            gegevens.put("Periode tot", alsTekst(capaciteitswissel.getPeriodeTot()));
        }

        if (afspraak instanceof Orderverplaatsing orderverplaatsing) {
            gegevens.put("Fabriek van herkomst", orderverplaatsing.getVanFabriek());

            gegevens.put("Bestemmingsfabriek", orderverplaatsing.getNaarFabriek());

            gegevens.put("Finish type", orderverplaatsing.getFinishType());

            gegevens.put("Totaal volume tons", alsTekst(orderverplaatsing.getTotaalVolumeTons()));

            gegevens.put("Gewenste verplaatsingsdatum", alsTekst(orderverplaatsing.getGewensteVerplaatsingsdatum()));
        }

        return gegevens;
    }

    private List<Wijziging> registreerGewijzigdeVelden(
            OperationeleAfspraak afspraak,
            Gebruiker uitvoerder,
            Map<String, String> oudeGegevens,
            Map<String, String> nieuweGegevens) {
        List<Wijziging> wijzigingen = new ArrayList<>();

        for (Map.Entry<String, String> entry : oudeGegevens.entrySet()) {
            String onderdeel = entry.getKey();

            String oudeWaarde = entry.getValue();

            String nieuweWaarde = nieuweGegevens.get(onderdeel);

            if (!Objects.equals(oudeWaarde, nieuweWaarde)) {
                Wijziging wijziging = afspraak.registreerWijziging(uitvoerder, onderdeel, oudeWaarde, nieuweWaarde);

                wijzigingen.add(wijziging);
            }
        }

        return wijzigingen;
    }

    private String alsTekst(Object waarde) {
        return waarde == null ? null : waarde.toString();
    }

    private void controleerAlleIdsGevonden(List<UUID> gevraagdeIds, int gevondenAantal, String foutmelding) {
        long uniekAantalIds = gevraagdeIds.stream()
                .distinct()
                .count();

        if (gevondenAantal != uniekAantalIds) {
            throw new IllegalArgumentException(foutmelding);
        }
    }
}