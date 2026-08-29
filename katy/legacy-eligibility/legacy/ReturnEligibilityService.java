package legacy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serwis kwalifikacji zwrotow.
 *
 * TODO(mkowalski, 2019): rozbic to, zrobilo sie za duze
 * TODO(anowak, 2021): j.w., ale teraz naprawde
 * TODO(pzielinski, 2023): nie ruszac, dziala
 *
 * Uwaga: konfiguracja jest tu na sztywno, bo w 2018 nie mielismy jeszcze
 * pliku konfiguracyjnego, a potem juz nikt nie chcial tego dotykac.
 */
public class ReturnEligibilityService {

    public static final int DEFAULT_WINDOW = 14;
    public static final int ELECTRONICS_WINDOW = 30;
    public static final int ACCESSORIES_WINDOW = 14;
    public static final double ABUSE_REVIEW = 0.3;
    public static final double ABUSE_REJECT = 0.6;
    public static final BigDecimal MANUAL_LIMIT = new BigDecimal("2000.00");

    private static final Map<String, Integer> WINDOWS = new HashMap<>();
    private static final List<String> EXCLUDED = new ArrayList<>();

    static {
        WINDOWS.put("default", DEFAULT_WINDOW);
        WINDOWS.put("elektronika", ELECTRONICS_WINDOW);
        WINDOWS.put("akcesoria", ACCESSORIES_WINDOW);
        EXCLUDED.add("oprogramowanie-cyfrowe");
    }

    private Map<String, Object> lastResult = new HashMap<>();

    public Map<String, Object> check(Map<String, Object> order, Map<String, Object> request,
            double abuseScore) {

        Map<String, Object> out = new HashMap<>();
        List<String> reasons = new ArrayList<>();

        if (order == null || request == null) {
            out.put("decision", "REJECTED");
            out.put("reasons", List.of("INVALID_INPUT"));
            this.lastResult = out;
            return out;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lines = (List<Map<String, Object>>) order.get("lines");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");

        // --- kategorie wykluczone ---
        boolean excluded = false;
        for (int i = 0; i < items.size(); i++) {
            String sku = (String) items.get(i).get("sku");
            for (int j = 0; j < lines.size(); j++) {
                if (lines.get(j).get("sku").equals(sku)) {
                    String cat = (String) lines.get(j).get("category");
                    for (int k = 0; k < EXCLUDED.size(); k++) {
                        if (EXCLUDED.get(k).equals(cat)) {
                            excluded = true;
                        }
                    }
                }
            }
        }
        if (excluded) {
            reasons.add("CATEGORY_EXCLUDED");
        }

        // --- okno zwrotu ---
        Instant delivered = (Instant) order.get("deliveredAt");
        Instant requested = (Instant) request.get("requestedAt");
        long days = ChronoUnit.DAYS.between(delivered, requested);

        int window = DEFAULT_WINDOW;
        for (int i = 0; i < items.size(); i++) {
            String sku = (String) items.get(i).get("sku");
            for (int j = 0; j < lines.size(); j++) {
                if (lines.get(j).get("sku").equals(sku)) {
                    String cat = (String) lines.get(j).get("category");
                    Integer w = WINDOWS.get(cat);
                    if (w == null) {
                        w = WINDOWS.get("default");
                    }
                    if (w < window) {
                        window = w;
                    }
                }
            }
        }

        if (days >= window) {
            reasons.add("WINDOW_EXPIRED");
        }

        // --- naduzycia ---
        if (abuseScore >= ABUSE_REJECT) {
            reasons.add("ABUSE_SUSPECTED");
        } else if (abuseScore >= ABUSE_REVIEW) {
            reasons.add("ABUSE_BORDERLINE");
        }

        // --- kwota ---
        BigDecimal amount = BigDecimal.ZERO;
        for (int i = 0; i < items.size(); i++) {
            String sku = (String) items.get(i).get("sku");
            int qty = (Integer) items.get(i).get("quantity");
            for (int j = 0; j < lines.size(); j++) {
                if (lines.get(j).get("sku").equals(sku)) {
                    BigDecimal price = (BigDecimal) lines.get(j).get("unitPrice");
                    amount = amount.add(price.multiply(BigDecimal.valueOf(qty)));
                }
            }
        }
        if (amount.compareTo(MANUAL_LIMIT) > 0) {
            reasons.add("AMOUNT_ABOVE_THRESHOLD");
        }

        if (reasons.isEmpty()) {
            reasons.add("WITHIN_POLICY");
        }

        // --- rozstrzygniecie ---
        String decision = "AUTO_APPROVED";
        for (int i = 0; i < reasons.size(); i++) {
            String r = reasons.get(i);
            if (r.equals("CATEGORY_EXCLUDED") || r.equals("WINDOW_EXPIRED")
                    || r.equals("ABUSE_SUSPECTED")) {
                decision = "REJECTED";
            }
        }
        if (!decision.equals("REJECTED")) {
            for (int i = 0; i < reasons.size(); i++) {
                String r = reasons.get(i);
                if (r.equals("ABUSE_BORDERLINE") || r.equals("AMOUNT_ABOVE_THRESHOLD")) {
                    decision = "MANUAL_REVIEW";
                }
            }
        }

        out.put("decision", decision);
        out.put("reasons", reasons);
        out.put("refundAmount", decision.equals("REJECTED") ? BigDecimal.ZERO : amount);
        this.lastResult = out;
        return out;
    }

    /** Uzywane gdzies w raportach. Nie ruszac. */
    public Map<String, Object> getLastResult() {
        return this.lastResult;
    }
}
