package nl.outokumpu.afspraken.entity;

import jakarta.persistence.*;

import nl.outokumpu.afspraken.enums.GebruikersRol;

import java.util.UUID;

@Entity
@Table(name = "gebruikers")
public class Gebruiker {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            nullable = false,
            length = 100
    )
    private String naam;

    @Column(
            nullable = false,
            unique = true,
            length = 255
    )
    private String email;

    /*
     * Nullable zodat bestaande gebruikers/data niet direct breken.
     *
     * Een gebruiker zonder wachtwoordHash kan niet lokaal inloggen.
     * Een echt wachtwoord slaan we hier NOOIT op.
     */
    @Column(
            name = "wachtwoord_hash",
            length = 255
    )
    private String wachtwoordHash;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 50
    )
    private GebruikersRol rol;

    @Column(nullable = false)
    private boolean actief;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "afdeling_id",
            nullable = false
    )
    private Afdeling afdeling;

    protected Gebruiker() {
        // Vereist door JPA
    }

    public Gebruiker(
            String naam,
            String email,
            Afdeling afdeling
    ) {
        this.naam = naam;
        this.email = email;
        this.afdeling = afdeling;
        this.rol = GebruikersRol.GEBRUIKER;
        this.actief = true;
    }

    public void configureerToegang(
            String wachtwoordHash,
            GebruikersRol rol,
            boolean actief
    ) {

        controleerWachtwoordHash(
                wachtwoordHash
        );

        if (rol == null) {
            throw new IllegalArgumentException(
                    "Gebruikersrol is verplicht"
            );
        }

        this.wachtwoordHash =
                wachtwoordHash;

        this.rol =
                rol;

        this.actief =
                actief;
    }

    public void wijzigWachtwoordHash(
            String nieuwWachtwoordHash
    ) {

        controleerWachtwoordHash(
                nieuwWachtwoordHash
        );

        this.wachtwoordHash =
                nieuwWachtwoordHash;
    }

    public void wijzigRol(
            GebruikersRol nieuweRol
    ) {

        if (nieuweRol == null) {
            throw new IllegalArgumentException(
                    "Gebruikersrol is verplicht"
            );
        }

        this.rol =
                nieuweRol;
    }

    public void wijzigToegang(
            boolean actief
    ) {
        this.actief =
                actief;
    }

    private void controleerWachtwoordHash(
            String wachtwoordHash
    ) {

        if (wachtwoordHash == null
                || wachtwoordHash.isBlank()) {

            throw new IllegalArgumentException(
                    "Wachtwoordhash is verplicht"
            );
        }
    }

    public UUID getId() {
        return id;
    }

    public String getNaam() {
        return naam;
    }

    public void setNaam(
            String naam
    ) {
        this.naam = naam;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(
            String email
    ) {
        this.email = email;
    }

    public String getWachtwoordHash() {
        return wachtwoordHash;
    }

    public GebruikersRol getRol() {
        return rol;
    }

    public boolean isActief() {
        return actief;
    }

    public Afdeling getAfdeling() {
        return afdeling;
    }

    public void setAfdeling(
            Afdeling afdeling
    ) {
        this.afdeling = afdeling;
    }
}