package workshop.rma.returns.entity;

/**
 * Rozstrzygniecie zgloszenia zwrotu.
 *
 * <p>Precedencja jest czescia kontraktu i jest twarda:
 * {@code REJECTED} > {@code MANUAL_REVIEW} > {@code AUTO_APPROVED}.
 * Patrz docs/contract/decision-procedure.md.
 */
public enum Decision {
    AUTO_APPROVED,
    MANUAL_REVIEW,
    REJECTED
}
