package workshop.rma.returns;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import workshop.rma.returns.entity.Order;
import workshop.rma.returns.entity.OrderLine;
import workshop.rma.returns.entity.ReturnItem;
import workshop.rma.returns.entity.ReturnRequest;

/**
 * Zamienia wiersz tabeli decyzyjnej na zamowienie i zgloszenie.
 *
 * <p>Konstrukcja jest celowo minimalna: jedna pozycja zwracana o cenie rownej
 * {@code amount}, zeby kwota zwrotu byla dokladnie ta z tabeli. Przy przypadku
 * czesciowym dokladamy DRUGA pozycje, ktorej nie zwracamy - to czyni zwrot
 * czesciowym, nie zmieniajac kwoty.
 */
public final class Fixtures {

    public static final String SKU_RETURNED = "SKU-RETURNED";
    public static final String SKU_KEPT = "SKU-KEPT";

    private Fixtures() {
    }

    public static Instant requestedAt() {
        return Instant.parse("2026-06-15T12:00:00Z");
    }

    public static Order order(PolicyCase c) {
        return order(c, UUID.randomUUID());
    }

    public static Order order(PolicyCase c, UUID orderId) {
        var delivered = requestedAt().minus(c.daysSinceDelivery(), ChronoUnit.DAYS);
        var lines = c.partial()
                ? List.of(line(c), new OrderLine(SKU_KEPT, c.category(), BigDecimal.ONE, 1))
                : List.of(line(c));
        return new Order(orderId, UUID.randomUUID(), delivered, lines);
    }

    private static OrderLine line(PolicyCase c) {
        return new OrderLine(SKU_RETURNED, c.category(), c.amount(), 1);
    }

    public static ReturnRequest request(PolicyCase c, UUID orderId) {
        return new ReturnRequest(orderId, requestedAt(),
                List.of(new ReturnItem(SKU_RETURNED, 1, c.reason())));
    }
}
