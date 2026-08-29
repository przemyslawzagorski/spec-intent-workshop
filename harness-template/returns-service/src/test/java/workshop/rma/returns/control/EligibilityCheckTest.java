package workshop.rma.returns.control;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import workshop.rma.returns.Fixtures;
import workshop.rma.returns.PolicyCase;

/**
 * Tabela decyzyjna na poziomie komponentu.
 *
 * <p>Pokrywa <b>wszystkie</b> wiersze tabeli, lacznie z przypadkami naduzyc,
 * ktorych do wdrozenia scoring-workera (M9) nie da sie przepchnac przez HTTP -
 * boundary podaje wtedy 0.0 (zalozenie A5 w specyfikacji komponentu).
 *
 * <p>To jest swiadomy podzial warstw harnessu, nie obejscie: pytanie "co da sie
 * przetestowac przez publiczna powierzchnie, a co jeszcze nie" jest czescia
 * projektowania bramki.
 */
@QuarkusTest
class EligibilityCheckTest {

    @Inject
    EligibilityCheck eligibility;

    static List<PolicyCase> cases() {
        return PolicyCase.all();
    }

    /**
     * Pokrywa wymagania <b>R1.4-R1.14</b> - cala procedure kwalifikacji
     * i rozstrzygniecia. Jeden wiersz tabeli na jedno ID.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void decisionMatchesGeneratedTable(PolicyCase c) {
        var order = Fixtures.order(c);
        var request = Fixtures.request(c, order.orderId());

        var decision = this.eligibility.evaluate(order, request, c.abuseScore());

        Assertions.assertAll(
                () -> Assertions.assertEquals(c.expectedDecision(), decision.decision(),
                        () -> "decyzja dla " + c),
                () -> Assertions.assertEquals(c.expectedReasonCodes(), decision.reasonCodes(),
                        () -> "kody powodow dla " + c));
    }
}
