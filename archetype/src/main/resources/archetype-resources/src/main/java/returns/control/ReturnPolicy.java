#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.returns.control;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.yaml.snakeyaml.Yaml;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import ${package}.returns.entity.Payer;
import ${package}.returns.entity.Reason;

/**
 * Polityka zwrotow wczytana z {@code return-policy.yaml}.
 *
 * <p>Ten plik jest DANYMI, nie konfiguracja aplikacji. Kontrakt
 * (docs/contract/decision-procedure.md) mowi, CO sie dzieje; policy mowi,
 * PRZY JAKICH WARTOSCIACH. Dzieki temu rozdzieleniu ten sam kod obsluguje
 * buduje inny system, a bramka pozostaje jedna.
 *
 * <p>Wczytujemy raz, na starcie. Zmiana polityki wymaga restartu - swiadomie,
 * bo policy jest czescia kontraktu, a nie pokretlem do krecenia na produkcji.
 */
@ApplicationScoped
public class ReturnPolicy {

    static System.Logger LOG = System.getLogger(ReturnPolicy.class.getName());

    @ConfigProperty(name = "policy.file", defaultValue = "../return-policy.yaml")
    String path;

    private Map<String, Integer> windows;
    private Set<String> excluded;
    private boolean partialReturnAllowed;
    private BigDecimal manualReviewAboveAmount;
    private int abuseWindowDays;
    private double abuseReviewAt;
    private double abuseRejectAt;
    private Map<String, String> shippingPaidBy;

    @PostConstruct
    @SuppressWarnings("unchecked")
    void load() {
        var yaml = new Yaml();
        Map<String, Object> p;
        var file = Path.of(path);
        try {
            if (Files.isRegularFile(file)) {
                try (InputStream in = Files.newInputStream(file)) {
                    p = yaml.load(in);
                }
                LOG.log(System.Logger.Level.INFO, "policy zwrotow z pliku: " + file.toAbsolutePath());
            } else {
                try (InputStream in = getClass().getResourceAsStream("/return-policy.yaml")) {
                    if (in == null) {
                        throw new IllegalStateException(
                                "nie znaleziono return-policy.yaml ani pod '" + path
                                        + "', ani na classpath");
                    }
                    p = yaml.load(in);
                }
                LOG.log(System.Logger.Level.INFO, "policy zwrotow z classpath");
            }
        } catch (IOException e) {
            throw new IllegalStateException("nie da sie wczytac polityki z " + path, e);
        }

        this.windows = (Map<String, Integer>) p.get("windows");
        this.excluded = Set.copyOf((List<String>) p.get("excludedCategories"));
        this.partialReturnAllowed = (boolean) p.get("partialReturnAllowed");
        this.manualReviewAboveAmount = new BigDecimal(String.valueOf(p.get("manualReviewAboveAmount")));

        var abuse = (Map<String, Object>) p.get("abuse");
        this.abuseWindowDays = (int) abuse.get("windowDays");
        this.abuseReviewAt = ((Number) abuse.get("reviewAt")).doubleValue();
        this.abuseRejectAt = ((Number) abuse.get("rejectAt")).doubleValue();

        this.shippingPaidBy = (Map<String, String>) ((Map<String, Object>) p.get("shipping")).get("paidBy");

        if (!this.windows.containsKey("default")) {
            throw new IllegalStateException("policy bez windows.default - uruchom tools/policy_check.py");
        }
    }

    /** Okno zwrotu dla kategorii; {@code default}, gdy kategoria nie ma wlasnego. */
    public int windowDays(String kategoria) {
        return this.windows.getOrDefault(kategoria, this.windows.get("default"));
    }

    public boolean isCategoryExcluded(String kategoria) {
        return this.excluded.contains(kategoria);
    }

    public boolean partialReturnAllowed() {
        return this.partialReturnAllowed;
    }

    public BigDecimal manualReviewAboveAmount() {
        return this.manualReviewAboveAmount;
    }

    public int abuseWindowDays() {
        return this.abuseWindowDays;
    }

    public double abuseReviewAt() {
        return this.abuseReviewAt;
    }

    public double abuseRejectAt() {
        return this.abuseRejectAt;
    }

    public Payer shippingPaidBy(Reason powod) {
        return Payer.valueOf(this.shippingPaidBy.get(powod.name()));
    }
}
