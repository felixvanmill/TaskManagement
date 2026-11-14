
package nl.outokumpu.afspraken.dto.response;

import nl.outokumpu.afspraken.enums.NotificatieType;

import java.time.Instant;
import java.util.UUID;

public record NotificatieResponse(
        UUID id,
        UUID afspraakId,
        NotificatieType type,
        String bericht,
        Instant aangemaaktOp,
        Instant gelezenOp
) {
}
