
package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.entity.Notificatie;
import nl.outokumpu.afspraken.entity.OperationeleAfspraak;
import nl.outokumpu.afspraken.entity.Processtap;
import nl.outokumpu.afspraken.enums.NotificatieType;
import nl.outokumpu.afspraken.repository.NotificatieRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificatieServiceTest {

    private NotificatieRepository notificatieRepository;
    private NotificatieService notificatieService;

    @BeforeEach
    void setUp() {
        notificatieRepository = mock(NotificatieRepository.class);
        notificatieService = new NotificatieService(notificatieRepository);
    }

    @Test
    void taakNotificatieWordtAanJuisteProcesstapGekoppeld() {
        Gebruiker gebruiker = mock(Gebruiker.class);
        OperationeleAfspraak afspraak = mock(OperationeleAfspraak.class);
        Processtap processtap = mock(Processtap.class);

        when(notificatieRepository.save(any(Notificatie.class)))
                .thenAnswer(aanroep -> aanroep.getArgument(0));

        Notificatie notificatie = notificatieService.maakTaakNotificatie(
                gebruiker,
                afspraak,
                processtap,
                "Nieuwe taak toegewezen"
        );

        assertSame(gebruiker, notificatie.getOntvanger());
        assertSame(afspraak, notificatie.getAfspraak());
        assertSame(processtap, notificatie.getProcesstap());
        assertEquals(
                NotificatieType.TAAK_TOEGEWEZEN,
                notificatie.getType()
        );

        verify(notificatieRepository).save(notificatie);
    }

    @Test
    void notificatieLezenHandeltTaakNietAf() {
        UUID notificatieId = UUID.randomUUID();
        UUID gebruikerId = UUID.randomUUID();

        Notificatie notificatie = new Notificatie(
                mock(Gebruiker.class),
                mock(OperationeleAfspraak.class),
                NotificatieType.LEESVERZOEK,
                "Afspraak lezen"
        );

        when(notificatieRepository.findByIdAndOntvanger_Id(
                notificatieId,
                gebruikerId
        )).thenReturn(Optional.of(notificatie));

        notificatieService.markeerGelezen(notificatieId, gebruikerId);

        assertNotNull(notificatie.getGelezenOp());
        assertNull(notificatie.getAfgehandeldOp());
    }

    @Test
    void gebruikerKanAndermansNotificatieNietMarkerenAlsGelezen() {
        UUID notificatieId = UUID.randomUUID();
        UUID andereGebruikerId = UUID.randomUUID();

        when(notificatieRepository.findByIdAndOntvanger_Id(
                notificatieId,
                andereGebruikerId
        )).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> notificatieService.markeerGelezen(
                        notificatieId,
                        andereGebruikerId
                )
        );
    }

    @Test
    void afrondenHandeltAlleenNotificatiesVanJuisteProcesstapAf() {
        UUID gebruikerId = UUID.randomUUID();
        UUID processtapId = UUID.randomUUID();

        Gebruiker gebruiker = mock(Gebruiker.class);
        OperationeleAfspraak afspraak = mock(OperationeleAfspraak.class);

        Notificatie juisteTaak = new Notificatie(
                gebruiker,
                afspraak,
                mock(Processtap.class),
                NotificatieType.TAAK_TOEGEWEZEN,
                "Taak 1"
        );

        Notificatie andereTaak = new Notificatie(
                gebruiker,
                afspraak,
                mock(Processtap.class),
                NotificatieType.TAAK_TOEGEWEZEN,
                "Taak 2"
        );

        when(notificatieRepository
                .findByOntvanger_IdAndProcesstap_IdAndTypeAndAfgehandeldOpIsNull(
                        gebruikerId,
                        processtapId,
                        NotificatieType.TAAK_TOEGEWEZEN
                )
        ).thenReturn(List.of(juisteTaak));

        notificatieService.handelTaakNotificatiesAf(
                gebruikerId,
                processtapId
        );

        assertNotNull(juisteTaak.getAfgehandeldOp());
        assertNull(andereTaak.getAfgehandeldOp());
    }

    @Test
    void leesverzoekWordtNaBevestigingAfgehandeld() {
        UUID gebruikerId = UUID.randomUUID();
        UUID afspraakId = UUID.randomUUID();

        Notificatie notificatie = new Notificatie(
                mock(Gebruiker.class),
                mock(OperationeleAfspraak.class),
                NotificatieType.LEESVERZOEK,
                "Afspraak lezen"
        );

        when(notificatieRepository
                .findByOntvanger_IdAndAfspraak_IdAndTypeAndAfgehandeldOpIsNull(
                        gebruikerId,
                        afspraakId,
                        NotificatieType.LEESVERZOEK
                )
        ).thenReturn(List.of(notificatie));

        notificatieService.handelNotificatiesVoorAfspraakAf(
                gebruikerId,
                afspraakId,
                NotificatieType.LEESVERZOEK
        );

        assertNotNull(notificatie.getAfgehandeldOp());
    }

    @Test
    void ongelezenAantalKomtUitRepository() {
        UUID gebruikerId = UUID.randomUUID();

        when(notificatieRepository
                .countByOntvanger_IdAndGelezenOpIsNullAndAfgehandeldOpIsNull(
                        gebruikerId
                )
        ).thenReturn(3L);

        long aantal = notificatieService.telOngelezenNotificaties(
                gebruikerId
        );

        assertEquals(3L, aantal);
    }
}
