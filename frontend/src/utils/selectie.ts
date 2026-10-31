export function wijzigSelectie(
    huidigeIds: string[],
    id: string,
): string[] {
    if (huidigeIds.includes(id)) {
        return huidigeIds.filter(
            (huidigId) =>
                huidigId !== id,
        )
    }

    return [
        ...huidigeIds,
        id,
    ]
}
