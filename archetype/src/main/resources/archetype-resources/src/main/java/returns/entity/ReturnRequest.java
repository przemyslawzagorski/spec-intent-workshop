#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.returns.entity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Zgloszenie zwrotu przyslane przez klienta.
 *
 * @param orderId     zamowienie, ktorego dotyczy zwrot
 * @param requestedAt moment zgloszenia (UTC) - od niego liczymy okno zwrotu
 * @param items       lines do zwrotu, co najmniej jedna
 */
public record ReturnRequest(UUID orderId, Instant requestedAt, List<ReturnItem> items) {
}
