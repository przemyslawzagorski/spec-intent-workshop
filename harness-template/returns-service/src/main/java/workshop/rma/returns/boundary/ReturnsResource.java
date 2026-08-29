package workshop.rma.returns.boundary;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import workshop.rma.returns.control.EligibilityCheck;
import workshop.rma.returns.control.Orders;
import workshop.rma.returns.control.Returns;
import workshop.rma.returns.entity.Problem;
import workshop.rma.returns.entity.ReturnRequest;

/**
 * Granica komponentu zwrotow - jedyne miejsce, ktore wie o HTTP.
 *
 * <p>Kontrakt tego zasobu jest opisany w docs/contract/openapi.yaml i jest
 * WSPOLNY dla wszystkich uczestnikow warsztatu. To, przy jakich wartosciach
 * zapadaja decyzje, opisuje return-policy.yaml i jest INDYWIDUALNE.
 */
@Path("returns")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class ReturnsResource {

    /**
     * Wspolczynnik naduzyc liczy scoring-worker (M9). Do czasu jego wdrozenia
     * uzywamy 0.0 - jawne zalozenie zapisane w specyfikacji (R1.10), nie przeoczenie.
     */
    static final double NO_SCORING_YET = 0.0;

    @Inject
    Orders orders;

    @Inject
    Returns returns;

    @Inject
    EligibilityCheck eligibility;

    @Inject
    ReturnEvents events;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response submit(ReturnRequest request) {
        if (request == null || request.orderId() == null
                || request.requestedAt() == null
                || request.items() == null || request.items().isEmpty()) {
            return problem(Response.Status.BAD_REQUEST, "Niepoprawne request",
                    "orderId, requestedAt i co najmniej jedna pozycja sa wymagane");
        }

        var order = this.orders.find(request.orderId());
        if (order.isEmpty()) {
            return problem(Response.Status.NOT_FOUND, "Nie ma takiego orders",
                    request.orderId().toString());
        }

        var decision = this.eligibility.evaluate(order.get(), request, NO_SCORING_YET);
        this.returns.save(decision);
        // Decyzja wstepna juz zapisana - scoring dojdzie asynchronicznie
        // i ja przeliczy. Klient dostaje odpowiedz natychmiast.
        this.events.publishSubmitted(order.get(), decision);
        return Response.status(Response.Status.CREATED).entity(decision).build();
    }

    @GET
    @Path("{returnId}")
    public Response read(@PathParam("returnId") UUID returnId) {
        return this.returns.find(returnId)
                .map(z -> Response.ok(z).build())
                .orElseGet(() -> problem(Response.Status.NOT_FOUND, "Nie ma takiego zwrotu",
                        returnId.toString()));
    }

    private static Response problem(Response.Status status, String title, String detail) {
        return Response.status(status)
                .entity(new Problem(title, status.getStatusCode(), detail))
                .build();
    }
}
