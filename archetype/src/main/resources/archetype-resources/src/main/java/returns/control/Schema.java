#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.returns.control;

import java.sql.SQLException;

import javax.sql.DataSource;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

/**
 * Tworzy schemat przy starcie.
 *
 * <p>ADR-001 mowi "bez ORM", wiec nie ma tez automatycznego tworzenia tabel.
 * DDL stoi tu jawnie, bo agent czytajacy ten plik widzi kontrakt bazy wprost,
 * zamiast rekonstruowac go z adnotacji rozsianych po encjach.
 *
 * <p>Na produkcji zastapilaby to migracja (Flyway/Liquibase). Na warsztacie
 * zalezy nam na tym, zeby caly stan dalo sie zobaczyc w jednym miejscu.
 */
@ApplicationScoped
public class Schema {

    static System.Logger LOG = System.getLogger(Schema.class.getName());

    static final String DDL = """
            create table if not exists orders (
                order_id     uuid primary key,
                customer_id  uuid        not null,
                delivered_at timestamptz not null
            );
            create table if not exists order_lines (
                order_id   uuid           not null references orders(order_id),
                sku        text           not null,
                category   text           not null,
                unit_price numeric(12,2)  not null,
                quantity   int            not null,
                primary key (order_id, sku)
            );
            create table if not exists returns (
                return_id        uuid primary key,
                order_id         uuid           not null,
                submitted_at     timestamptz    not null,
                decision         text           not null,
                reason_codes     text           not null,
                shipping_paid_by text,
                refund_amount    numeric(12,2)  not null
            );
            """;

    @Inject
    DataSource dataSource;

    void onStartup(@Observes StartupEvent event) {
        try (var connection = this.dataSource.getConnection();
                var statement = connection.createStatement()) {
            statement.execute(DDL);
            LOG.log(System.Logger.Level.INFO, "schemat gotowy");
        } catch (SQLException e) {
            throw new IllegalStateException("nie da sie utworzyc schematu", e);
        }
    }
}
