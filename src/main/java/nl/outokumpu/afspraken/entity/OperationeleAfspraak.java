
package nl.outokumpu.afspraken.entity;

import jakarta.persistence.*;
import nl.outokumpu.afspraken.enums.AfspraakStatus;
import nl.outokumpu.afspraken.enums.AfspraakType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

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
}
