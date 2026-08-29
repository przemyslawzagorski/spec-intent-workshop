package workshop.rma.returns.boundary;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.Set;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import workshop.rma.returns.control.EligibilityCheck;
import workshop.rma.returns.control.Orders;
import workshop.rma.returns.control.ReturnPolicy;
import workshop.rma.returns.control.Returns;
import workshop.rma.returns.entity.Decision;
import workshop.rma.returns.entity.Order;
import workshop.rma.returns.entity.ReasonCode;
import workshop.rma.returns.entity.ReturnDecision;
import workshop.rma.returns.entity.ReturnScoredEvent;
import workshop.rma.returns.entity.ReturnSubmittedEvent;

/**
 * Druga granica komponentu: szew asynchroniczny.
 *
 * <p>Kontrakt zdarzen: docs/contract/events.md. Ta klasa tylko przenosi
 * zdarzenia i pilnuje ksztaltu - rozstrzyganie zostaje w
 * {@link EligibilityCheck}, dokladnie tam, gdzie bylo.
 *
 * <p><b>Dlaczego wskaznik naduzyc przychodzi asynchronicznie.</b> Wyliczenie go
 * wymaga przejrzenia historii klienta i nie moze blokowac odpowiedzi HTTP.
 * Konsekwencja jest zapisana w specyfikacji, a nie ukryta: pierwsza decyzja
 * zapada bez wskaznika (zalozenie A5), a po nadejsciu wyniku jest przeliczana.
 */
@ApplicationScoped
public class ReturnEvents {

    static System.Logger LOG = System.getLogger(ReturnEvents.class.getName());

    @Inject
    @Channel("return-submitted")
    Emitter<ReturnSubmittedEvent> submitted;

    @Inject
    Orders orders;

    @Inject
    Returns returns;

    @Inject
    ReturnPolicy policy;

    /** Wywolywane przez boundary HTTP zaraz po zapisaniu decyzji wstepnej. */
    public void publishSubmitted(Order order, ReturnDecision decision) {
        var until = decision.submittedAt();
        var since = until.minus(this.policy.abuseWindowDays(), ChronoUnit.DAYS);
        this.submitted.send(new ReturnSubmittedEvent(
                decision.returnId(),
                order.customerId(),
                until,
                this.orders.countInWindow(order.customerId(), since, until),
                // wykluczamy biezacy zwrot - zapisalismy go chwile temu
                this.returns.history(order.customerId(), since, until, decision.returnId())));
    }

    /**
     * Wynik scoringu. Przeliczamy decyzje ponownie.
     *
     * <p>Nie przechowujemy oryginalnego zgloszenia i nie musimy: kody powodow sa
     * zapisane, wiec wystarczy wymienic te dotyczace naduzyc i rozstrzygnac
     * <b>ta sama funkcja</b>, ktora rozstrzygala za pierwszym razem. Gdyby
     * istnialy dwie funkcje rozstrzygajace, rozjechalyby sie.
     */
    @Incoming("return-scored")
    public void onScored(ReturnScoredEvent scored) {
        var zapisany = this.returns.find(scored.returnId());
        if (zapisany.isEmpty()) {
            LOG.log(System.Logger.Level.WARNING,
                    "wynik scoringu dla nieznanego zwrotu " + scored.returnId());
            return;
        }
        var aReturn = zapisany.get();

        var codes = EnumSet.copyOf(aReturn.reasonCodes());
        codes.remove(ReasonCode.ABUSE_SUSPECTED);
        codes.remove(ReasonCode.ABUSE_BORDERLINE);
        if (scored.abuseScore() >= this.policy.abuseRejectAt()) {
            codes.add(ReasonCode.ABUSE_SUSPECTED);
        } else if (scored.abuseScore() >= this.policy.abuseReviewAt()) {
            codes.add(ReasonCode.ABUSE_BORDERLINE);
        }
        if (codes.size() > 1) {
            codes.remove(ReasonCode.WITHIN_POLICY);
        }
        if (codes.isEmpty()) {
            codes.add(ReasonCode.WITHIN_POLICY);
        }

        var decision = EligibilityCheck.resolve(codes);
        var rejected = decision == Decision.REJECTED;
        this.returns.applyScore(
                aReturn,
                Set.copyOf(codes),
                decision,
                rejected ? BigDecimal.ZERO : aReturn.refundAmount(),
                rejected ? null : aReturn.shippingPaidBy());

        LOG.log(System.Logger.Level.INFO,
                "zwrot %s: score %.4f -> %s".formatted(
                        scored.returnId(), scored.abuseScore(), decision));
    }

    /** Widoczne dla testow - granica okna naduzyc dla danego momentu. */
    Instant abuseWindowStart(Instant at) {
        return at.minus(this.policy.abuseWindowDays(), ChronoUnit.DAYS);
    }
}
