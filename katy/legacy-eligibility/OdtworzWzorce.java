import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import legacy.ReturnEligibilityService;

/**
 * Odtwarza zlote wzorce przeciw legacy i porownuje z nagraniem.
 *
 * <p>To jest siatka bezpieczenstwa refaktoru: uruchamiasz ja PRZED
 * zmiana kodu (musi byc zielona), a potem po kazdym kroku refaktoru.
 *
 * <p><b>Uwaga:</b> nagranie zawiera znany defekt (wiersz L02 - zwrot w ostatnim
 * dniu okna jest odrzucany). To jest zamierzone. Testy charakteryzujace
 * utrwalaja zachowanie <b>faktyczne</b>, nie oczekiwane - patrz legacy-decision.md.
 *
 * <p>Uruchomienie (nie wymaga Mavena ani zadnych zaleznosci):
 * <pre>
 *   javac -d . legacy/ReturnEligibilityService.java OdtworzWzorce.java
 *   java OdtworzWzorce characterization/return-eligibility.tsv
 * </pre>
 */
public class OdtworzWzorce {

    static final Instant DOSTAWA = Instant.parse("2026-06-01T10:00:00Z");

    public static void main(String[] args) throws Exception {
        var plik = Path.of(args.length > 0 ? args[0] : "characterization/return-eligibility.tsv");
        var wiersze = Files.readAllLines(plik);
        var serwis = new ReturnEligibilityService();

        int sprawdzonych = 0;
        int rozjazdow = 0;

        for (var linia : wiersze.subList(1, wiersze.size())) {   // wiersz 0 to naglowek
            var k = linia.split("\t", -1);
            var id = k[0];
            var kategoria = k[2];
            var dni = Long.parseLong(k[3]);
            var kwota = new BigDecimal(k[4]);
            var abuse = Double.parseDouble(k[5]);
            var oczekiwanaDecyzja = k[6];
            var oczekiwanePowody = k[7];

            var wynik = serwis.check(zamowienie(kategoria, kwota), zgloszenie(dni), abuse);

            @SuppressWarnings("unchecked")
            var powody = new ArrayList<>((List<String>) wynik.get("reasons"));
            Collections.sort(powody);
            var faktyczneDecyzja = String.valueOf(wynik.get("decision"));
            var faktycznePowody = String.join(",", powody);

            sprawdzonych++;
            if (!oczekiwanaDecyzja.equals(faktyczneDecyzja)
                    || !oczekiwanePowody.equals(faktycznePowody)) {
                rozjazdow++;
                System.out.printf(Locale.ROOT,
                        "ROZJAZD %s%n  nagrane:  %s / %s%n  faktyczne: %s / %s%n",
                        id, oczekiwanaDecyzja, oczekiwanePowody,
                        faktyczneDecyzja, faktycznePowody);
            }
        }

        if (rozjazdow == 0) {
            System.out.printf(Locale.ROOT,
                    "OK    %d zlotych wzorcow odtworzonych bez rozjazdu%n", sprawdzonych);
        } else {
            System.out.printf(Locale.ROOT,
                    "FAIL  %d z %d wzorcow sie rozjechalo.%n"
                    + "      Refaktor zmienil zachowanie. Cofnij zmiane - NIE poprawiaj nagrania.%n",
                    rozjazdow, sprawdzonych);
            System.exit(1);
        }
    }

    static Map<String, Object> zamowienie(String kategoria, BigDecimal cena) {
        Map<String, Object> linia = new HashMap<>();
        linia.put("sku", "SKU-1");
        linia.put("category", kategoria);
        linia.put("unitPrice", cena);
        linia.put("quantity", 1);
        Map<String, Object> zam = new HashMap<>();
        zam.put("deliveredAt", DOSTAWA);
        zam.put("lines", List.of(linia));
        return zam;
    }

    static Map<String, Object> zgloszenie(long dni) {
        Map<String, Object> pozycja = new HashMap<>();
        pozycja.put("sku", "SKU-1");
        pozycja.put("quantity", 1);
        pozycja.put("reason", "CHANGED_MIND");
        Map<String, Object> zgl = new HashMap<>();
        zgl.put("requestedAt", DOSTAWA.plus(dni, ChronoUnit.DAYS));
        zgl.put("items", List.of(pozycja));
        return zgl;
    }
}
