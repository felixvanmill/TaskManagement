package nl.outokumpu.afspraken.repository;

import nl.outokumpu.afspraken.entity.Processtap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcesstapRepository extends JpaRepository<Processtap, UUID> {
}