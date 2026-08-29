package workshop.rma.returns.boundary;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import workshop.rma.returns.Fixtures;
import workshop.rma.returns.PolicyCase;
import workshop.rma.returns.control.Orders;

/**
 * Bramka HARD: kontrakt przez publiczna powierzchnie.
 *
 * <p>Testujemy WYLACZNIE przez HTTP - zadnego zagladania do srodka. Dzieki temu
 * ten sam zestaw testow przechodzi niezaleznie od tego, jak ktos zbudowal
 * implementacje. To ta sama zasada, dzieki ktorej zestaw testow Bun (napisany
 * w TypeScripcie) przezyl przepisanie 535 tys. linii z Ziga na Rusta.
 *
 * <p>Jedyny wyjatek: zasiew zamowien idzie przez {@link Orders}, bo kontrakt nie
 * ma operacji tworzenia zamowien - one przychodza z innego systemu. Zasiew to
 * ustawienie sceny, nie asercja.
 */
@QuarkusTest
class ReturnsResourceTest {

    @Inject
    Orders orders;

    /** Przypadki bez naduzyc - reszte pokrywa EligibilityCheckTest (zalozenie A5). */
    static List<PolicyCase> httpCases() {
        return PolicyCase.withoutAbuse();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("httpCases")
    void decisionMatchesGeneratedTable(PolicyCase c) {
        var order = Fixtures.order(c);
        this.orders.save(order);

        given().contentType(ContentType.JSON)
                .body(body(order.orderId(), c))
                .when().post("/returns")
                .then()
                .statusCode(201)
                .body("decision", is(c.expectedDecision().name()))
                .body("reasonCodes", containsInAnyOrder(
                        c.expectedReasonCodes().stream().map(Enum::name).toArray()))
                .body("orderId", is(order.orderId().toString()))
                .body("returnId", notNullValue());
    }

    // ---- kontrakt: R1.2, R1.3, R1.13, R1.15, R1.16 ----

    @Test
    void unknownOrderIsRejectedWith404() {                                        // R1.2
        given().contentType(ContentType.JSON)
                .body(body(UUID.randomUUID(), PolicyCase.all().getFirst()))
                .when().post("/returns")
                .then().statusCode(404)
                .body("status", is(404));
    }

    @Test
    void requestWithoutItemsIsRejectedWith400() {                                 // R1.3
        given().contentType(ContentType.JSON)
                .body("""
                        {"orderId":"%s","requestedAt":"%s","items":[]}"""
                        .formatted(UUID.randomUUID(), Fixtures.requestedAt()))
                .when().post("/returns")
                .then().statusCode(400);
    }

    @Test
    void rejectedReturnHasZeroRefundAndNoPayer() {                                // R1.13
        var rejected = PolicyCase.withoutAbuse().stream()
                .filter(c -> c.expectedDecision().name().equals("REJECTED"))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "tabela nie zawiera przypadku REJECTED bez naduzyc - "
                                + "sprawdz swoja polityke"));
        var order = Fixtures.order(rejected);
        this.orders.save(order);

        given().contentType(ContentType.JSON)
                .body(body(order.orderId(), rejected))
                .when().post("/returns")
                .then().statusCode(201)
                .body("refundAmount", is(0))
                .body("shippingPaidBy", nullValue());
    }

    @Test
    void storedDecisionCanBeReadBack() {                                          // R1.15
        var approved = PolicyCase.withoutAbuse().stream()
                .filter(c -> c.expectedDecision().name().equals("AUTO_APPROVED"))
                .findFirst()
                .orElseThrow();
        var order = Fixtures.order(approved);
        this.orders.save(order);

        var returnId = given().contentType(ContentType.JSON)
                .body(body(order.orderId(), approved))
                .when().post("/returns")
                .then().statusCode(201)
                .extract().path("returnId").toString();

        given().when().get("/returns/{id}", returnId)
                .then().statusCode(200)
                .body("returnId", equalTo(returnId))
                .body("decision", is("AUTO_APPROVED"))
                .body("orderId", is(order.orderId().toString()));
    }

    @Test
    void unknownReturnIsNotFound() {                                              // R1.16
        given().when().get("/returns/{id}", UUID.randomUUID())
                .then().statusCode(404);
    }

    private static String body(UUID orderId, PolicyCase c) {
        return """
                {"orderId":"%s","requestedAt":"%s",
                 "items":[{"sku":"%s","quantity":1,"reason":"%s"}]}"""
                .formatted(orderId, Fixtures.requestedAt(), Fixtures.SKU_RETURNED, c.reason());
    }
}
