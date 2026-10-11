package nl.outokumpu.afspraken.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "opmerkingen")
public class Opmerking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "afspraak_id",
            nullable = false,
            updatable = false
    )
    private OperationeleAfspraak afspraak;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "auteur_id",
            nullable = false,
            updatable = false
    )
    private Gebruiker auteur;

    @Column(
            nullable = false,
            length = 5000,
            updatable = false
    )
    private String tekst;

    @Column(
            name = "aangemaakt_op",
            nullable = false,
            updatable = false
    )
    private Instant aangemaaktOp;

    protected Opmerking() {
        // Vereist door JPA
    }

    public Opmerking(
            OperationeleAfspraak afspraak,
            Gebruiker auteur,
            String tekst
    ) {

        if (afspraak == null) {
            throw new IllegalArgumentException(
                    "Afspraak is verplicht"
            );
        }

        if (auteur == null) {
            throw new IllegalArgumentException(
                    "Auteur is verplicht"
            );
        }

        if (tekst == null
                || tekst.isBlank()) {

            throw new IllegalArgumentException(
                    "Opmerking mag niet leeg zijn"
            );
        }

        if (tekst.length() > 5000) {
            throw new IllegalArgumentException(
                    "Opmerking mag maximaal 5000 tekens bevatten"
            );
        }

        this.afspraak =
                afspraak;

        this.auteur =
                auteur;

        this.tekst =
                tekst.trim();
    }

    @PrePersist
    protected void bijAanmaken() {

        if (aangemaaktOp == null) {
            aangemaaktOp =
                    Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public OperationeleAfspraak getAfspraak() {
        return afspraak;
    }

    public Gebruiker getAuteur() {
        return auteur;
    }

    public String getTekst() {
        return tekst;
    }

    public Instant getAangemaaktOp() {
        return aangemaaktOp;
    }
}