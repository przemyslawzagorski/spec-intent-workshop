#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.returns;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import ${package}.returns.entity.Decision;
import ${package}.returns.entity.Reason;
import ${package}.returns.entity.ReasonCode;

/**
 * Jeden wiersz wygenerowanej tabeli decyzyjnej.
 *
 * <p><b>Ta tabela nie jest pisana recznie.</b> Generuje ja
 * {@code tools/policy_cases.py} z twojego {@code return-policy.yaml}, stosujac
 * wspolna procedure z docs/contract/decision-procedure.md. Dlatego kazdy
 * zmiana polityki przebudowuje cala tabele, a testy zostaja te same.
 *
 * <p>Tabela jest ORAKULEM. Jesli twoja implementacja sie z nia nie zgadza, to
 * albo kod jest zly, albo zle przeczytales procedure. Tabela nie jest zla.
 *
 * <p>TSV, nie proza - bo tabele da sie sprawdzic maszynowo i sa tansze w tokenach.
 */
public record PolicyCase(
        String caseId,
        String description,
        String category,
        int daysSinceDelivery,
        BigDecimal amount,
        double abuseScore,
        boolean partial,
        Reason reason,
        Decision expectedDecision,
        Set<ReasonCode> expectedReasonCodes) {

    static final String RESOURCE = "/policy-cases.tsv";

    public static List<PolicyCase> all() {
        try (var in = PolicyCase.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("""
                        Brak %s na classpath testow.
                        Wygeneruj tabele przed uruchomieniem testow:
                          uv run tools/policy_cases.py TWOJA-POLITYKE > src/test/resources/policy-cases.tsv
                        albo wygeneruj tabele: uv run tools/policy_cases.py return-policy.yaml""".formatted(RESOURCE));
            }
            var text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            var lines = text.strip().lines().toList();
            return lines.stream()
                    .skip(1)                                  // wiersz 0 to naglowek
                    .map(PolicyCase::parse)
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Przypadki, ktore da sie przepchnac przez HTTP przy abuseScore = 0 (zalozenie A5). */
    public static List<PolicyCase> withoutAbuse() {
        return all().stream().filter(c -> c.abuseScore() == 0.0).toList();
    }

    static PolicyCase parse(String line) {
        var c = line.split("${symbol_escape}t", -1);
        return new PolicyCase(
                c[0], c[1], c[2],
                Integer.parseInt(c[3]),
                new BigDecimal(c[4]),
                Double.parseDouble(c[5]),
                Boolean.parseBoolean(c[6]),
                Reason.valueOf(c[7]),
                Decision.valueOf(c[8]),
                Arrays.stream(c[9].split(",")).map(ReasonCode::valueOf)
                        .collect(java.util.stream.Collectors.toUnmodifiableSet()));
    }

    @Override
    public String toString() {
        return "%s  %s".formatted(this.caseId, this.description);
    }
}
