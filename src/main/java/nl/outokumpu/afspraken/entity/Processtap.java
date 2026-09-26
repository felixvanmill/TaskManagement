package nl.outokumpu.afspraken.entity;

import jakarta.persistence.*;
import nl.outokumpu.afspraken.enums.ProcesstapStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

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
        // Vereist door JPA
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
