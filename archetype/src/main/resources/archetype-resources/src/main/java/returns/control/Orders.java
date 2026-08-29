#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.returns.control;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import ${package}.returns.entity.Order;
import ${package}.returns.entity.OrderLine;

/** Dostep do zamowien. Zamowienia sa danymi wejsciowymi - ten serwis ich nie tworzy. */
@ApplicationScoped
public class Orders {

    static final String FIND = """
            select o.customer_id, o.delivered_at,
                   l.sku, l.category, l.unit_price, l.quantity
              from orders o
              join order_lines l on l.order_id = o.order_id
             where o.order_id = ?
            """;

    static final String SAVE_ORDER =
            "insert into orders(order_id, customer_id, delivered_at) values (?,?,?)";

    static final String SAVE_LINES = """
            insert into order_lines(order_id, sku, category, unit_price, quantity)
            values (?,?,?,?,?)
            """;

    @Inject
    DataSource dataSource;

    public Optional<Order> find(UUID orderId) {
        try (var connection = this.dataSource.getConnection();
                var statement = connection.prepareStatement(FIND)) {
            statement.setObject(1, orderId);
            try (var result = statement.executeQuery()) {
                UUID customerId = null;
                Instant deliveredAt = null;
                List<OrderLine> lines = new ArrayList<>();
                while (result.next()) {
                    customerId = result.getObject("customer_id", UUID.class);
                    deliveredAt = result.getTimestamp("delivered_at").toInstant();
                    lines.add(new OrderLine(
                            result.getString("sku"),
                            result.getString("category"),
                            result.getBigDecimal("unit_price"),
                            result.getInt("quantity")));
                }
                return lines.isEmpty() ? Optional.empty()
                        : Optional.of(new Order(orderId, customerId, deliveredAt, List.copyOf(lines)));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("nie da sie odczytac orders " + orderId, e);
        }
    }

    /** Zasiew danych. Uzywane przez tryb dev i testy - nie przez sciezke produkcyjna. */
    public void save(Order order) {
        try (var connection = this.dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (var p = connection.prepareStatement(SAVE_ORDER)) {
                p.setObject(1, order.orderId());
                p.setObject(2, order.customerId());
                p.setTimestamp(3, java.sql.Timestamp.from(order.deliveredAt()));
                p.executeUpdate();
            }
            try (var p = connection.prepareStatement(SAVE_LINES)) {
                for (var line : order.lines()) {
                    p.setObject(1, order.orderId());
                    p.setString(2, line.sku());
                    p.setString(3, line.category());
                    p.setBigDecimal(4, line.unitPrice());
                    p.setInt(5, line.quantity());
                    p.addBatch();
                }
                p.executeBatch();
            }
            connection.commit();
        } catch (SQLException e) {
            throw new IllegalStateException("nie da sie zapisac orders " + order.orderId(), e);
        }
    }
}
