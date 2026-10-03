package nl.outokumpu.afspraken.repository;

import nl.outokumpu.afspraken.entity.Gebruiker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GebruikerRepository extends JpaRepository<Gebruiker, UUID> {
}