
package nl.outokumpu.afspraken.repository;
import nl.outokumpu.afspraken.enums.NotificatieType;

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

    List<Notificatie>
    findByOntvanger_IdAndAfspraak_IdAndTypeAndAfgehandeldOpIsNull(
            UUID ontvangerId,
            UUID afspraakId,
            NotificatieType type
    );

    long countByOntvanger_IdAndGelezenOpIsNullAndAfgehandeldOpIsNull(
            UUID ontvangerId
    );

    Optional<Notificatie> findByIdAndOntvanger_Id(
            UUID notificatieId,
            UUID ontvangerId
    );
}
