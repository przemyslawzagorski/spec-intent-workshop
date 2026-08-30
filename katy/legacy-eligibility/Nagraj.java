import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import legacy.ReturnEligibilityService;

/**
 * Nagrywa zachowanie FAKTYCZNE legacy jako zlote wzorce. Nie ocenia go.
 *
 * <p>Bodzce bierze z {@link Bodzce} - z tego samego miejsca, z ktorego bierze je
 * odtwarzacz. To nie jest ozdobnik: dopoki nagrywarka i odtwarzacz buduja wejscia
 * osobno, moga sie rozjechac, a wtedy zielona siatka niczego nie dowodzi.
 *
 * <pre>
 *   javac -d . legacy/ReturnEligibilityService.java Bodzce.java Nagraj.java
 *   java Nagraj &gt; wzorce/return-eligibility.tsv
 * </pre>
 */
public class Nagraj {

	public static void main(String[] args) {
		var serwis = new ReturnEligibilityService();

		System.out.println(Bodzce.NAGLOWEK);
		for (var b : Bodzce.wszystkie()) {
			var wynik = serwis.check(Bodzce.zamowienie(b), Bodzce.zgloszenie(b), b.abuse());

			@SuppressWarnings("unchecked")
			var powody = new ArrayList<>((List<String>) wynik.get("reasons"));
			Collections.sort(powody);

			System.out.printf(Locale.ROOT, "%s\t%s\t%s\t%s\t%d\t%s\t%d\t%s\t%.2f\t%s\t%s\t%s%n",
					b.id(), b.opis(), b.wariant(), b.kategoria(), b.dni(), b.cena(), b.ilosc(),
					b.powod(), b.abuse(),
					wynik.get("decision"), String.join(",", powody), wynik.get("refundAmount"));
		}
	}
}
