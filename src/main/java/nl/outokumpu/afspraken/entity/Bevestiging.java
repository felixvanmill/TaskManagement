package nl.outokumpu.afspraken.entity;

import jakarta.persistence.*;
import nl.outokumpu.afspraken.enums.Beslissing;

import java.time.Instant;
import java.util.UUID;

/**
 * Tracks one user's read acknowledgement and approval decision for an agreement.
 * A single confirmation is shared across the user's roles within that agreement.
 */
@Entity
@Table(
        name = "bevestigingen",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_bevestiging_afspraak_gebruiker",
                        columnNames = {"afspraak_id", "gebruiker_id"}
                )
        }
)
public class Bevestiging {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "afspraak_id", nullable = false)
    private OperationeleAfspraak afspraak;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gebruiker_id", nullable = false)
    private Gebruiker gebruiker;

    @Column(nullable = false)
    private boolean gezien;

    private Instant gezienOp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Beslissing beslissing;

    private Instant beslistOp;

    @Version
    private Long versie;

    protected Bevestiging() {
        // Required by JPA.
    }

    public Bevestiging(
            OperationeleAfspraak afspraak,
            Gebruiker gebruiker
    ) {
        this.afspraak = afspraak;
        this.gebruiker = gebruiker;
        this.gezien = false;
        this.beslissing = Beslissing.GEEN;
    }

    /**
     * Records the first read acknowledgement and its timestamp independently of an approval decision.
     *
     * @throws IllegalStateException if this confirmation has already been marked as seen
     */
    public void markeerGezien() {

        if (this.gezien) {
            throw new IllegalStateException(
                    "Afspraak is al als gezien geregistreerd"
            );
        }

        this.gezien = true;
        this.gezienOp = Instant.now();
    }

    /**
     * Records an approval or rejection and replaces the decision timestamp.
     * A previous decision may be changed, but repeating it is rejected; the read acknowledgement is unaffected.
     *
     * @param nieuweBeslissing the approval or rejection to record
     * @throws IllegalArgumentException if the decision is null, {@code GEEN}, or unchanged
     */
    public void registreerBeslissing(
            Beslissing nieuweBeslissing
    ) {

        if (nieuweBeslissing == null
                || nieuweBeslissing == Beslissing.GEEN) {

            throw new IllegalArgumentException(
                    "Beslissing moet GOEDGEKEURD of AFGEWEZEN zijn"
            );
        }

        if (this.beslissing == nieuweBeslissing) {
            throw new IllegalArgumentException(
                    "Nieuwe beslissing moet verschillen van de huidige beslissing"
            );
        }

        this.beslissing = nieuweBeslissing;
        this.beslistOp = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public OperationeleAfspraak getAfspraak() {
        return afspraak;
    }

    public Gebruiker getGebruiker() {
        return gebruiker;
    }

    public boolean isGezien() {
        return gezien;
    }

    public Instant getGezienOp() {
        return gezienOp;
    }

    public Beslissing getBeslissing() {
        return beslissing;
    }

    public Instant getBeslistOp() {
        return beslistOp;
    }

    public Long getVersie() {
        return versie;
    }
}