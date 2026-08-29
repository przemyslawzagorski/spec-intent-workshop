package workshop.rma.returns.entity;

/**
 * Pozycja zgloszona do zwrotu.
 *
 * @param sku      identyfikator produktu
 * @param quantity ile sztuk, zawsze dodatnie
 * @param reason   powod zwrotu - decyduje, kto placi za przesylke
 */
public record ReturnItem(String sku, int quantity, Reason reason) {
}
