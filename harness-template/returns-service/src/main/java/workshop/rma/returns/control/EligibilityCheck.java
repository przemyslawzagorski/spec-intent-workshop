package workshop.rma.returns.control;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import workshop.rma.returns.entity.Decision;
import workshop.rma.returns.entity.Order;
import workshop.rma.returns.entity.OrderLine;
import workshop.rma.returns.entity.Payer;
import workshop.rma.returns.entity.ReasonCode;
import workshop.rma.returns.entity.ReturnDecision;
import workshop.rma.returns.entity.ReturnItem;
import workshop.rma.returns.entity.ReturnRequest;

/**
 * Kwalifikacja zwrotu wedlug docs/contract/decision-procedure.md.
 *
 * <p><b>Znaki porownan sa czescia kontraktu, nie stylem.</b> Okno jest domkniete
 * ({@code >} wygasza), progi naduzyc sa domkniete ({@code >=} dziala), a prog
 * kwotowy jest ostry ({@code >} dziala). Ta niespojnosc jest w kontrakcie celowo -
 * spec ma byc czytana, nie zgadywana.
 *
 * <p>Ta klasa nie wie nic o HTTP ani o bazie. Dostaje order i request,
 * zwraca decyzje. Dzieki temu da sie ja przetestowac bez stawiania czegokolwiek.
 */
@ApplicationScoped
public class EligibilityCheck {

    @Inject
    ReturnPolicy policy;

    /**
     * @param abuseScore wspolczynnik naduzyc z zakresu [0,1], liczony przez
     *                   scoring-worker. Do czasu jego wdrozenia (M9) wolajacy
     *                   podaje {@code 0.0} - to jawne zalozenie, nie przeoczenie.
     */
    public ReturnDecision evaluate(Order order, ReturnRequest request, double abuseScore) {
        var codes = EnumSet.noneOf(ReasonCode.class);
        var orderLines = bySku(order);

        if (hasExcludedCategory(request, orderLines)) {
            codes.add(ReasonCode.CATEGORY_EXCLUDED);
        }
        if (isOutsideWindow(order, request, orderLines)) {
            codes.add(ReasonCode.WINDOW_EXPIRED);
        }
        if (isPartial(order, request) && !this.policy.partialReturnAllowed()) {
            codes.add(ReasonCode.PARTIAL_NOT_ALLOWED);
        }
        if (abuseScore >= this.policy.abuseRejectAt()) {
            codes.add(ReasonCode.ABUSE_SUSPECTED);
        } else if (abuseScore >= this.policy.abuseReviewAt()) {
            codes.add(ReasonCode.ABUSE_BORDERLINE);
        }

        var amount = refundFor(request, orderLines);
        if (amount.compareTo(this.policy.manualReviewAboveAmount()) > 0) {
            codes.add(ReasonCode.AMOUNT_ABOVE_THRESHOLD);
        }

        if (codes.isEmpty()) {
            codes.add(ReasonCode.WITHIN_POLICY);
        }

        var decision = resolve(codes);
        var rejected = decision == Decision.REJECTED;

        return new ReturnDecision(
                UUID.randomUUID(),
                order.orderId(),
                request.requestedAt(),
                decision,
                Set.copyOf(codes),
                rejected ? null : shippingPaidBy(request),
                rejected ? BigDecimal.ZERO : amount);
    }

    /**
     * Rozstrzygniecie z zestawu kodow powodow.
     *
     * <p>Publiczne, bo uzywa tego takze granica zdarzen przy przeliczaniu decyzji
     * po nadejsciu wyniku scoringu. <b>Musi byc jedna funkcja rozstrzygajaca</b> -
     * gdyby byly dwie, rozjechalyby sie przy pierwszej zmianie precedencji.
     */
    public static Decision resolve(Set<ReasonCode> codes) {
        if (codes.stream().anyMatch(ReasonCode.ODRZUCAJACE::contains)) {
            return Decision.REJECTED;
        }
        if (codes.stream().anyMatch(ReasonCode.DO_PRZEGLADU::contains)) {
            return Decision.MANUAL_REVIEW;
        }
        return Decision.AUTO_APPROVED;
    }

    private static Map<String, OrderLine> bySku(Order order) {
        return order.lines().stream()
                .collect(Collectors.toMap(OrderLine::sku, l -> l));
    }

    private boolean hasExcludedCategory(ReturnRequest request,
            Map<String, OrderLine> lines) {
        return request.items().stream()
                .map(i -> lines.get(i.sku()))
                .filter(Objects::nonNull)
                .anyMatch(l -> this.policy.isCategoryExcluded(l.category()));
    }

    /**
     * Zamowienie moze zawierac lines z roznych kategorii o roznych oknach.
     * <b>Wygrywa okno najkrotsze</b> - decision specyfikacji (R1.9), nie oczywistosc.
     * Karta domenowa tego nie rozstrzyga.
     */
    private boolean isOutsideWindow(Order order, ReturnRequest request,
            Map<String, OrderLine> lines) {
        var days = ChronoUnit.DAYS.between(order.deliveredAt(), request.requestedAt());
        var shortestWindow = request.items().stream()
                .map(i -> lines.get(i.sku()))
                .filter(Objects::nonNull)
                .mapToInt(l -> this.policy.windowDays(l.category()))
                .min()
                .orElseGet(() -> this.policy.windowDays("default"));
        return days > shortestWindow;
    }

    /** Czesciowy = nie wszystkie lines orders wracaja w pelnych ilosciach. */
    static boolean isPartial(Order order, ReturnRequest request) {
        var returning = request.items().stream()
                .collect(Collectors.toMap(ReturnItem::sku, ReturnItem::quantity,
                        Integer::sum));
        return order.lines().stream()
                .anyMatch(l -> returning.getOrDefault(l.sku(), 0) < l.quantity());
    }

    private static BigDecimal refundFor(ReturnRequest request, Map<String, OrderLine> lines) {
        return request.items().stream()
                .map(i -> {
                    var line = lines.get(i.sku());
                    return line == null ? BigDecimal.ZERO
                            : line.unitPrice().multiply(BigDecimal.valueOf(i.quantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Przy wielu powodach wygrywa ten korzystniejszy dla klienta. */
    private Payer shippingPaidBy(ReturnRequest request) {
        var payers = request.items().stream()
                .map(ReturnItem::reason)
                .map(this.policy::shippingPaidBy)
                .collect(Collectors.toSet());
        return payers.contains(Payer.MERCHANT) ? Payer.MERCHANT : Payer.CUSTOMER;
    }
}
