#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.returns.control;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import ${package}.returns.entity.Decision;
import ${package}.returns.entity.Payer;
import ${package}.returns.entity.ReasonCode;
import ${package}.returns.entity.ReturnDecision;

/** Utrwalenie i odczyt rozstrzygnietych zwrotow. */
@ApplicationScoped
public class Returns {

    static final String SAVE = """
            insert into returns(return_id, order_id, submitted_at, decision,
                                reason_codes, shipping_paid_by, refund_amount)
            values (?,?,?,?,?,?,?)
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
