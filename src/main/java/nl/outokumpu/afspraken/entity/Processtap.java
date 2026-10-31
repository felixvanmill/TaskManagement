package nl.outokumpu.afspraken.entity;

import jakarta.persistence.*;
import nl.outokumpu.afspraken.enums.ProcesstapStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * An ordered step in an agreement's workflow, assigned to a responsible user.
 * Step order is unique within the agreement, and optimistic locking protects concurrent updates.
 */
@Entity
@Table(
        name = "processtappen",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_afspraak_volgorde",
                        columnNames = {"afspraak_id", "volgorde"}
                )
        }
)
public class Processtap {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String naam;

    @Column(nullable = false)
    private int volgorde;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProcesstapStatus status;

    @Column(nullable = false)
    private LocalDate deadline;

    private Instant gestartOp;

    private Instant afgerondOp;

    @Version
    private Long versie;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "afspraak_id", nullable = false)
    private OperationeleAfspraak afspraak;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "verantwoordelijke_id", nullable = false)
    private Gebruiker verantwoordelijke;

    protected Processtap() {
        // Required by JPA.
    }

    public Processtap(
            String naam,
            int volgorde,
            LocalDate deadline,
            OperationeleAfspraak afspraak,
            Gebruiker verantwoordelijke
    ) {
        this.naam = naam;
        this.volgorde = volgorde;
        this.deadline = deadline;
        this.afspraak = afspraak;
        this.verantwoordelijke = verantwoordelijke;
        this.status = ProcesstapStatus.NIET_GESTART;
    }

    /**
     * Transitions from {@code NIET_GESTART} to {@code IN_UITVOERING} and records the start time.
     *
     * @throws IllegalStateException if the step has already started or completed
     */
    public void start() {

        if (this.status != ProcesstapStatus.NIET_GESTART) {
            throw new IllegalStateException(
                    "Processtap kan alleen worden gestart vanuit NIET_GESTART"
            );
        }

        this.status = ProcesstapStatus.IN_UITVOERING;
        this.gestartOp = Instant.now();
    }

    /**
     * Completes an in-progress step and records its completion time.
     * This does not advance another step or change the agreement's status.
     *
     * @throws IllegalStateException if the step is not in {@code IN_UITVOERING}
     */
    public void rondAf() {

        if (this.status != ProcesstapStatus.IN_UITVOERING) {
            throw new IllegalStateException(
                    "Processtap kan alleen worden afgerond vanuit IN_UITVOERING"
            );
        }

        this.status = ProcesstapStatus.AFGEROND;
        this.afgerondOp = Instant.now();
    }

    /**
     * Reassigns the step without changing its progress or timestamps.
     * The current user is recognized by object identity or a matching non-null user ID.
     *
     * @param nieuweVerantwoordelijke the replacement responsible user
     * @throws IllegalArgumentException if the user is null or is already responsible for this step
     */
    public void wijzigVerantwoordelijke(
            Gebruiker nieuweVerantwoordelijke
    ) {

        if (nieuweVerantwoordelijke == null) {
            throw new IllegalArgumentException(
                    "Nieuwe verantwoordelijke is verplicht"
            );
        }

        boolean zelfdeVerantwoordelijke =
                this.verantwoordelijke == nieuweVerantwoordelijke
                        ||
                        (
                                this.verantwoordelijke.getId() != null
                                        &&
                                        this.verantwoordelijke
                                                .getId()
                                                .equals(
                                                        nieuweVerantwoordelijke
                                                                .getId()
                                                )
                        );

        if (zelfdeVerantwoordelijke) {
            throw new IllegalArgumentException(
                    "Nieuwe verantwoordelijke moet verschillen van de huidige verantwoordelijke"
            );
        }

        this.verantwoordelijke =
                nieuweVerantwoordelijke;
    }

    public UUID getId() {
        return id;
    }

    public String getNaam() {
        return naam;
    }

    public int getVolgorde() {
        return volgorde;
    }

    public ProcesstapStatus getStatus() {
        return status;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public Instant getGestartOp() {
        return gestartOp;
    }

    public Instant getAfgerondOp() {
        return afgerondOp;
    }

    public Long getVersie() {
        return versie;
    }

    public OperationeleAfspraak getAfspraak() {
        return afspraak;
    }

    public Gebruiker getVerantwoordelijke() {
        return verantwoordelijke;
    }
}