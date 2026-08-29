#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.returns.control;

import ${package}.returns.entity.Order;
import ${package}.returns.entity.ReturnDecision;
import ${package}.returns.entity.ReturnRequest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Kwalifikacja zwrotu wedlug docs/contract/decision-procedure.md.
 *
 * <p><b>Stan po M4: szkielet.</b> Testy sa napisane i sie kompiluja, ale sa
 * czerwone - bo tej metody jeszcze nie ma. Tak ma byc.
 *
 * <p>W jezyku statycznie typowanym test nie skompiluje sie bez typu, ktory
 * testuje. Dlatego czerwien w M4 to nie "brak klasy", tylko "klasa bez
 * zachowania". To jest normalny TDD w Javie, nie obejscie.
 *
 * <p>W M5 delegujesz jej implementacje agentowi. Jedynym kryterium jest zielona
 * tabela - nie to, jak agent to napisal.
 */
@ApplicationScoped
public class EligibilityCheck {

    @Inject
    ReturnPolicy policy;

    /**
     * @param abuseScore wspolczynnik naduzyc [0,1] ze scoring-workera; do M9
     *                   wolajacy podaje 0.0 (zalozenie A5 w specyfikacji)
     */
    public ReturnDecision evaluate(Order order, ReturnRequest request, double abuseScore) {
        throw new UnsupportedOperationException(
                "R1.4-R1.14 do zaimplementowania w M5 - patrz package-info.java "
                        + "oraz docs/contract/decision-procedure.md");
    }
}
