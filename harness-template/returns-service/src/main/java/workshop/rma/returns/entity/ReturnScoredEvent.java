package workshop.rma.returns.entity;

import java.util.UUID;

/**
 * Odpowiedz scoring-workera. Kontrakt:
 * docs/contract/events/return-scored.schema.json
 */
public record ReturnScoredEvent(UUID returnId, double abuseScore) {
}
