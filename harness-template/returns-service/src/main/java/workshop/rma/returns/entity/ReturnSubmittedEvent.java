package workshop.rma.returns.entity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Zdarzenie do scoring-workera. Kontrakt:
 * docs/contract/events/return-submitted.schema.json
 *
 * <p>Niesie WSZYSTKO, czego konsument potrzebuje - dzieki temu worker nie siega
 * do naszej bazy. Dwa serwisy na jednej bazie to sprzegniecie, ktore psuje oba.
 */
public record ReturnSubmittedEvent(
        UUID returnId,
        UUID customerId,
        Instant requestedAt,
        int ordersInWindow,
        List<PastReturn> history) {
}
