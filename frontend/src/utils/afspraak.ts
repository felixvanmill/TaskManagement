import type {
    AfspraakDetail,
    Processtap,
    AfspraakStatus,
    AfspraakType,
    ProcesstapStatus,
} from '../types/afspraak'

export function statusLabel(
    status: AfspraakStatus,
) {
    switch (status) {
        case 'OPEN':
            return 'Open'

        case 'IN_BEHANDELING':
            return 'In behandeling'

        case 'AFGEROND':
            return 'Afgerond'
    }
}

export function typeLabel(
    type: AfspraakType,
) {
    switch (type) {
        case 'ALGEMEEN':
            return 'Algemeen'

        case 'CAPACITEITSWISSEL':
            return 'Capaciteitswissel'

        case 'ORDERVERPLAATSING':
            return 'Orderverplaatsing'
    }
}

export function processtapStatusLabel(
    status: ProcesstapStatus,
) {
    switch (status) {
        case 'NIET_GESTART':
            return 'Niet gestart'

        case 'IN_UITVOERING':
            return 'In uitvoering'

        case 'AFGEROND':
            return 'Afgerond'
    }
}

export function bepaalActieveProcesstap(
    afspraak: AfspraakDetail,
): Processtap | undefined {
    return afspraak.processtappen.find(
        (processtap) =>
            processtap.status !== 'AFGEROND',
    )
}

export function bepaalHuidigeProcesstap(
    afspraak: AfspraakDetail,
): Processtap | undefined {
    return bepaalActieveProcesstap(afspraak)
        ?? afspraak.processtappen[
            afspraak.processtappen.length - 1
        ]
}

export function bepaalHuidigeVerantwoordelijke(
    afspraak: AfspraakDetail,
) {
    if (afspraak.processtappen.length === 0) {
        return null
    }

    const actieveProcesstap =
        bepaalActieveProcesstap(
            afspraak,
        )

    if (actieveProcesstap !== undefined) {
        return actieveProcesstap.verantwoordelijke
    }

    return afspraak.processtappen[
    afspraak.processtappen.length - 1
        ].verantwoordelijke
}
