package nl.outokumpu.afspraken.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "gebruikers")
public class Gebruiker {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String naam;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "afdeling_id", nullable = false)
    private Afdeling afdeling;

    protected Gebruiker() {
        // Vereist door JPA
    }

    public Gebruiker(String naam, String email, Afdeling afdeling) {
        this.naam = naam;
        this.email = email;
        this.afdeling = afdeling;
    }

    public UUID getId() {
        return id;
    }

    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Afdeling getAfdeling() {
        return afdeling;
    }

    public void setAfdeling(Afdeling afdeling) {
        this.afdeling = afdeling;
    }
}
