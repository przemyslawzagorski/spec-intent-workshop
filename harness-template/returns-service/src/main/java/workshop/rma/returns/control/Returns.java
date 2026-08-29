package workshop.rma.returns.control;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import workshop.rma.returns.entity.Decision;
import workshop.rma.returns.entity.PastReturn;
import workshop.rma.returns.entity.Payer;
import workshop.rma.returns.entity.ReasonCode;
import workshop.rma.returns.entity.ReturnDecision;

/** Utrwalenie i odczyt rozstrzygnietych zwrotow. */
@ApplicationScoped
public class Returns {

    static final String SAVE = """
            insert into returns(return_id, order_id, submitted_at, decision,
                                reason_codes, shipping_paid_by, refund_amount)
            values (?,?,?,?,?,?,?)
            """;

    static final String HISTORY = """
            select r.submitted_at, r.decision
              from returns r
              join orders o on o.order_id = r.order_id
             where o.customer_id = ?
               and r.submitted_at >= ?
               and r.submitted_at <= ?
               and r.return_id <> ?
             order by r.submitted_at desc
            """;

    static final String APPLY_SCORE = """
            update returns
               set decision = ?, reason_codes = ?, refund_amount = ?, shipping_paid_by = ?
             where return_id = ?
            """;

    static final String FIND = """
            select order_id, submitted_at, decision, reason_codes,
                   shipping_paid_by, refund_amount
              from returns
             where return_id = ?
            """;

    @Inject
    DataSource dataSource;

    public void save(ReturnDecision aReturn) {
        try (var connection = this.dataSource.getConnection();
                var p = connection.prepareStatement(SAVE)) {
            p.setObject(1, aReturn.returnId());
            p.setObject(2, aReturn.orderId());
            p.setTimestamp(3, java.sql.Timestamp.from(aReturn.submittedAt()));
            p.setString(4, aReturn.decision().name());
            p.setString(5, aReturn.reasonCodes().stream().map(Enum::name).sorted()
                    .collect(Collectors.joining(",")));
            p.setString(6, aReturn.shippingPaidBy() == null ? null : aReturn.shippingPaidBy().name());
            p.setBigDecimal(7, aReturn.refundAmount());
            p.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("nie da sie zapisac zwrotu " + aReturn.returnId(), e);
        }
    }

    /**
     * <b>Wczesniejsze</b> zwroty tego klienta - material dla scoring-workera.
     *
     * <p>Dwa warunki, ktore latwo przeoczyc, a bez ktorych wskaznik naduzyc jest zly:
     * <ul>
     *   <li>wykluczamy <b>biezacy</b> zwrot ({@code exclude}) - boundary zapisuje
     *       decyzje przed publikacja zdarzenia, wiec bez tego pierwszy zwrot klienta
     *       policzylby sam siebie jako historie;</li>
     *   <li>okno jest domkniete z <b>obu</b> stron - bez gornej granicy zwroty
     *       zapisane po {@code until} (przesuniety zegar, backdatowana dostawa)
     *       zmienialyby wynik wstecz.</li>
     * </ul>
     */
    public List<PastReturn> history(UUID customerId, Instant since, Instant until, UUID exclude) {
        try (var connection = this.dataSource.getConnection();
                var p = connection.prepareStatement(HISTORY)) {
            p.setObject(1, customerId);
            p.setTimestamp(2, java.sql.Timestamp.from(since));
            p.setTimestamp(3, java.sql.Timestamp.from(until));
            p.setObject(4, exclude);
            try (var result = p.executeQuery()) {
                var out = new ArrayList<PastReturn>();
                while (result.next()) {
                    out.add(new PastReturn(
                            result.getTimestamp("submitted_at").toInstant(),
                            Decision.valueOf(result.getString("decision"))));
                }
                return List.copyOf(out);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("nie da sie odczytac historii klienta " + customerId, e);
        }
    }

    /**
     * Nadpisuje decyzje po nadejsciu wyniku scoringu.
     *
     * <p>Nie przechowujemy oryginalnego zgloszenia - i nie musimy. Kody powodow
     * juz sa zapisane, wiec wystarczy wymienic te dotyczace naduzyc i rozstrzygnac
     * ponownie ta sama funkcja, ktora rozstrzygala za pierwszym razem.
     */
    public void applyScore(ReturnDecision aReturn, Set<ReasonCode> codes, Decision decision,
            BigDecimal refund, Payer payer) {
        try (var connection = this.dataSource.getConnection();
                var p = connection.prepareStatement(APPLY_SCORE)) {
            p.setString(1, decision.name());
            p.setString(2, codes.stream().map(Enum::name).sorted().collect(Collectors.joining(",")));
            p.setBigDecimal(3, refund);
            p.setString(4, payer == null ? null : payer.name());
            p.setObject(5, aReturn.returnId());
            p.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("nie da sie zaktualizowac zwrotu " + aReturn.returnId(), e);
        }
    }

    public Optional<ReturnDecision> find(UUID returnId) {
        try (var connection = this.dataSource.getConnection();
                var p = connection.prepareStatement(FIND)) {
            p.setObject(1, returnId);
            try (var result = p.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }
                var payer = result.getString("shipping_paid_by");
                return Optional.of(new ReturnDecision(
                        returnId,
                        result.getObject("order_id", UUID.class),
                        result.getTimestamp("submitted_at").toInstant(),
                        Decision.valueOf(result.getString("decision")),
                        Arrays.stream(result.getString("reason_codes").split(","))
                                .map(ReasonCode::valueOf)
                                .collect(Collectors.toUnmodifiableSet()),
                        payer == null ? null : Payer.valueOf(payer),
                        result.getBigDecimal("refund_amount")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("nie da sie odczytac zwrotu " + returnId, e);
        }
    }
}
