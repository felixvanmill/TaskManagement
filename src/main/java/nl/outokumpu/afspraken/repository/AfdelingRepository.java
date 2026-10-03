package nl.outokumpu.afspraken.repository;

import nl.outokumpu.afspraken.entity.Afdeling;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AfdelingRepository extends JpaRepository<Afdeling, UUID> {
}