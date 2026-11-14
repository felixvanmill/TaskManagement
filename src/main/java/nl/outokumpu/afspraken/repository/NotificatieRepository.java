
package nl.outokumpu.afspraken.repository;

import nl.outokumpu.afspraken.entity.Notificatie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificatieRepository
        extends JpaRepository<Notificatie, UUID> {

    List<Notificatie>
    findByOntvanger_IdAndAfgehandeldOpIsNullOrderByAangemaaktOpDesc(
            UUID ontvangerId
    );

    long countByOntvanger_IdAndGelezenOpIsNullAndAfgehandeldOpIsNull(
            UUID ontvangerId
    );

    Optional<Notificatie> findByIdAndOntvanger_Id(
            UUID notificatieId,
            UUID ontvangerId
    );
}
