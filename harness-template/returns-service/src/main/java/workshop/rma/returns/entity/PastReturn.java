package workshop.rma.returns.entity;

import java.time.Instant;

/** Wczesniejszy zwrot klienta - element historii wysylanej do scoring-workera. */
public record PastReturn(Instant returnedAt, Decision decision) {
}
