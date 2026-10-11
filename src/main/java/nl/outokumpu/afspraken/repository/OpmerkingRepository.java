package nl.outokumpu.afspraken.repository;

import nl.outokumpu.afspraken.entity.Opmerking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OpmerkingRepository
        extends JpaRepository<Opmerking, UUID> {

    List<Opmerking>
    findByAfspraakIdOrderByAangemaaktOpAsc(
            UUID afspraakId
    );
}