#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.returns.entity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Zamowienie w stanie, w jakim zna je sklep.
 *
 * @param deliveredAt moment dostawy - to OD NIEGO liczymy okno zwrotu, nie od zakupu.
 *                    To jest decision podjeta w specyfikacji, nie oczywistosc:
 *                    specyfikacja jej nie rozstrzyga.
 */
public record Order(UUID orderId, UUID customerId, Instant deliveredAt, List<OrderLine> lines) {
}
