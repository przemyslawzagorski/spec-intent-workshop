#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.returns.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Odpowiedz na request zwrotu - to, co widzi customerId.
 *
 * @param shippingPaidBy {@code null} dla {@link Decision${symbol_pound}REJECTED} - odrzuconego
 *                       zwrotu nikt nie wysyla, wiec nikt za niego nie placi
 * @param refundAmount   zawsze {@code 0} dla {@link Decision${symbol_pound}REJECTED}
 */
public record ReturnDecision(
        UUID returnId,
        UUID orderId,
        Instant submittedAt,
        Decision decision,
        Set<ReasonCode> reasonCodes,
        Payer shippingPaidBy,
        BigDecimal refundAmount) {
}
