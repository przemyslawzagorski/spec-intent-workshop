package workshop.rma.returns.entity;

/** Ksztalt bledu wedlug kontraktu (docs/contract/openapi.yaml). */
public record Problem(String title, int status, String detail) {
}
