package workshop.rma.returns.entity;

import java.math.BigDecimal;

/**
 * Pozycja orders - stan po stronie sklepu, nie to, co przysyla customerId.
 *
 * @param category kategoria produktu; steruje oknem zwrotu i wykluczeniami
 */
public record OrderLine(String sku, String category, BigDecimal unitPrice, int quantity) {
}
