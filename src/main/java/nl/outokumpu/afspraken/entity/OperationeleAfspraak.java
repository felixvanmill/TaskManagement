
package nl.outokumpu.afspraken.entity;

import jakarta.persistence.*;
import nl.outokumpu.afspraken.enums.AfspraakStatus;
import nl.outokumpu.afspraken.enums.AfspraakType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import nl.outokumpu.afspraken.enums.BetrokkenRol;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

@Entity
@Table(name = "operationele_afspraken")
@Inheritance(strategy = InheritanceType.JOINED)
public class OperationeleAfspraak {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String titel;

    @Column(nullable = false, length = 5000)
    private String beschrijving;

    @Column(nullable = false, length = 2000)
    private String reden;

    @Column(length = 5000)
    private String achtergrond;

    @Column(length = 5000)
    private String aannames;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private AfspraakType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AfspraakStatus status;

    @Column(nullable = false)
    private LocalDate ingangsdatum;

    @Column(nullable = false)
    private LocalDate deadline;

    @Column(nullable = false, updatable = false)
    private Instant aangemaaktOp;

    @Column(nullable = false)
    private Instant laatstGewijzigdOp;

    private Instant afgerondOp;

    @Version
    private Long versie;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "initiatiefnemer_id", nullable = false)
    private Gebruiker initiatiefnemer;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "afspraak_afdelingen",
            joinColumns = @JoinColumn(name = "afspraak_id"),
            inverseJoinColumns = @JoinColumn(name = "afdeling_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_afspraak_afdeling",
                    columnNames = {"afspraak_id", "afdeling_id"}
            )
    )
    private Set<Afdeling> betrokkenAfdelingen = new HashSet<>();


    @OneToMany(
            mappedBy = "afspraak",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Betrokkenheid> betrokkenheden = new ArrayList<>();


    @OneToMany(
            mappedBy = "afspraak",
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            }
    )
    @OrderBy("volgorde ASC")
    private List<Processtap> processtappen = new ArrayList<>();


    protected OperationeleAfspraak() {
        // Vereist door JPA
    }

    public OperationeleAfspraak(
            String titel,
            String beschrijving,
            String reden,
            String achtergrond,
            String aannames,
            LocalDate ingangsdatum,
            LocalDate deadline,
            Gebruiker initiatiefnemer
    ) {
        this(
                AfspraakType.ALGEMEEN,
                titel,
                beschrijving,
                reden,
                achtergrond,
                aannames,
                ingangsdatum,
                deadline,
                initiatiefnemer
        );
    }

    protected OperationeleAfspraak(
            AfspraakType type,
            String titel,
            String beschrijving,
            String reden,
            String achtergrond,
            String aannames,
            LocalDate ingangsdatum,
            LocalDate deadline,
            Gebruiker initiatiefnemer
    ) {
        this.type = type;
        this.titel = titel;
        this.beschrijving = beschrijving;
        this.reden = reden;
        this.achtergrond = achtergrond;
        this.aannames = aannames;
        this.ingangsdatum = ingangsdatum;
        this.deadline = deadline;
        this.initiatiefnemer = initiatiefnemer;
        this.status = AfspraakStatus.OPEN;
    }

    @PrePersist
    protected void bijAanmaken() {
        Instant nu = Instant.now();
        this.aangemaaktOp = nu;
        this.laatstGewijzigdOp = nu;
    }

    @PreUpdate
    protected void bijWijzigen() {
        this.laatstGewijzigdOp = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getTitel() {
        return titel;
    }

    public String getBeschrijving() {
        return beschrijving;
    }

    public String getReden() {
        return reden;
    }

    public String getAchtergrond() {
        return achtergrond;
    }

    public String getAannames() {
        return aannames;
    }

    public AfspraakType getType() {
        return type;
    }

    public AfspraakStatus getStatus() {
        return status;
    }

    public LocalDate getIngangsdatum() {
        return ingangsdatum;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public Instant getAangemaaktOp() {
        return aangemaaktOp;
    }

    public Instant getLaatstGewijzigdOp() {
        return laatstGewijzigdOp;
    }

    public Instant getAfgerondOp() {
        return afgerondOp;
    }

    public Long getVersie() {
        return versie;
    }

    public Gebruiker getInitiatiefnemer() {
        return initiatiefnemer;
    }

    public Set<Afdeling> getBetrokkenAfdelingen() {
        return Collections.unmodifiableSet(betrokkenAfdelingen);
    }

    public void voegAfdelingToe(Afdeling afdeling) {
        if (afdeling == null) {
            throw new IllegalArgumentException("Afdeling mag niet null zijn");
        }

        betrokkenAfdelingen.add(afdeling);
    }

    public void verwijderAfdeling(Afdeling afdeling) {
        betrokkenAfdelingen.remove(afdeling);
    }


    public List<Betrokkenheid> getBetrokkenheden() {
        return Collections.unmodifiableList(betrokkenheden);
    }

    public void voegBetrokkenheidToe(Gebruiker gebruiker, BetrokkenRol rol) {

        if (gebruiker == null || rol == null) {
            throw new IllegalArgumentException(
                    "Gebruiker en rol mogen niet null zijn"
            );
        }

        boolean bestaatAl = betrokkenheden.stream().anyMatch(b ->
                b.getRol() == rol &&
                        (
                                b.getGebruiker() == gebruiker ||
                                        (
                                                gebruiker.getId() != null &&
                                                        gebruiker.getId().equals(b.getGebruiker().getId())
                                        )
                        )
        );

        if (bestaatAl) {
            throw new IllegalArgumentException(
                    "Gebruiker heeft deze rol al binnen de afspraak"
            );
        }

        Betrokkenheid betrokkenheid =
                new Betrokkenheid(this, gebruiker, rol);

        betrokkenheden.add(betrokkenheid);
    }

    public void verwijderBetrokkenheid(Betrokkenheid betrokkenheid) {

        if (betrokkenheid == null ||
                !betrokkenheden.remove(betrokkenheid)) {

            throw new IllegalArgumentException(
                    "Betrokkenheid hoort niet bij deze afspraak"
            );
        }
    }


    public List<Processtap> getProcesstappen() {
        return processtappen.stream()
                .sorted(Comparator.comparingInt(Processtap::getVolgorde))
                .toList();
    }

    public Processtap voegProcesstapToe(
            String naam,
            int volgorde,
            LocalDate deadline,
            Gebruiker verantwoordelijke
    ) {

        if (naam == null || naam.isBlank()) {
            throw new IllegalArgumentException(
                    "Naam van de processtap is verplicht"
            );
        }

        if (volgorde < 1) {
            throw new IllegalArgumentException(
                    "Volgorde moet minimaal 1 zijn"
            );
        }

        if (deadline == null) {
            throw new IllegalArgumentException(
                    "Deadline is verplicht"
            );
        }

        if (verantwoordelijke == null) {
            throw new IllegalArgumentException(
                    "Verantwoordelijke is verplicht"
            );
        }

        boolean volgordeBestaat = processtappen.stream()
                .anyMatch(stap -> stap.getVolgorde() == volgorde);

        if (volgordeBestaat) {
            throw new IllegalArgumentException(
                    "Er bestaat al een processtap met deze volgorde"
            );
        }

        Processtap processtap = new Processtap(
                naam,
                volgorde,
                deadline,
                this,
                verantwoordelijke
        );

        processtappen.add(processtap);

        return processtap;
    }



}
