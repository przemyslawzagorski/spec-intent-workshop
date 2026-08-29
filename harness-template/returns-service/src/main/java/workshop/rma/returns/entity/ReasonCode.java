package workshop.rma.returns.entity;

import java.util.Set;

/**
 * Powody, ktore zadzialaly przy ocenie zwrotu.
 *
 * <p>Do odpowiedzi trafiaja WSZYSTKIE codes, ktore zadzialaly - nie tylko ten
 * rozstrzygajacy. Klient ma widziec pelny obraz, a nie pierwszy napotkany problem.
 */
public enum ReasonCode {
    CATEGORY_EXCLUDED,
    WINDOW_EXPIRED,
    ABUSE_SUSPECTED,
    ABUSE_BORDERLINE,
    AMOUNT_ABOVE_THRESHOLD,
    PARTIAL_NOT_ALLOWED,
    WITHIN_POLICY;

    /** Kody, ktorych wystapienie oznacza odrzucenie zwrotu. */
    public static final Set<ReasonCode> ODRZUCAJACE =
            Set.of(CATEGORY_EXCLUDED, WINDOW_EXPIRED, PARTIAL_NOT_ALLOWED, ABUSE_SUSPECTED);

    /** Kody, ktorych wystapienie kieruje zwrot do recznej akceptacji. */
    public static final Set<ReasonCode> DO_PRZEGLADU =
            Set.of(ABUSE_BORDERLINE, AMOUNT_ABOVE_THRESHOLD);
}
