import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import legacy.ReturnEligibilityService;

/**
 * Odtwarza zlote wzorce przeciw legacy i porownuje z nagraniem.
 *
 * <p>To jest siatka bezpieczenstwa refaktoru: uruchamiasz ja PRZED zmiana kodu
 * (musi byc zielona), a potem po kazdym kroku refaktoru.
 *
 * <p><b>Porownujemy trzy rzeczy: decyzje, powody i KWOTE ZWROTU.</b> Kwota jest tu
 * nie bez powodu - wczesniejsza wersja tego pliku jej nie czytala, wiec refaktor
 * scinajacy kazdy zwrot o 10% przechodzil przez siatke na zielono. Wyjscie, ktorego
 * nie porownujesz, nie jest chronione, choćby stalo w pliku z nagraniem.
 *
 * <p><b>Uwaga:</b> nagranie zawiera znany defekt (wiersz L02 - zwrot w ostatnim
 * dniu okna jest odrzucany). To jest zamierzone. Testy charakteryzujace utrwalaja
 * zachowanie <b>faktyczne</b>, nie oczekiwane - patrz DECYZJA.md.
 *
 * <p>Uruchomienie (nie wymaga Mavena ani zadnych zaleznosci):
 * <pre>
 *   javac -d . legacy/ReturnEligibilityService.java Bodzce.java OdtworzWzorce.java
 *   java OdtworzWzorce wzorce/return-eligibility.tsv
 * </pre>
 */
public class OdtworzWzorce {

	public static void main(String[] args) throws Exception {
		var plik = Path.of(args.length > 0 ? args[0] : "wzorce/return-eligibility.tsv");
		var wiersze = Files.readAllLines(plik);
		var serwis = new ReturnEligibilityService();

		int sprawdzonych = 0;
		int rozjazdow = 0;

		for (var linia : wiersze.subList(1, wiersze.size())) {   // wiersz 0 to naglowek
			if (linia.isBlank()) {
				continue;
			}
			var k = linia.split("\t", -1);
			var bodziec = Bodzce.zTsv(k);
			var nagrany = new Bodzce.Wynik(k[9], k[10], k[11].trim());

			var faktyczny = Bodzce.wykonaj(serwis, bodziec);

			sprawdzonych++;
			if (!nagrany.equals(faktyczny)) {
				rozjazdow++;
				System.out.printf(Locale.ROOT,
						"ROZJAZD %s  (%s)%n  nagrane:   %s / %s / zwrot %s%n"
						+ "  faktyczne: %s / %s / zwrot %s%n",
						bodziec.id(), bodziec.opis(),
						nagrany.decyzja(), nagrany.powody(), nagrany.zwrot(),
						faktyczny.decyzja(), faktyczny.powody(), faktyczny.zwrot());
			}
		}

		if (rozjazdow == 0) {
			System.out.printf(Locale.ROOT,
					"OK    %d zlotych wzorcow odtworzonych bez rozjazdu%n", sprawdzonych);
		}
		else {
			System.out.printf(Locale.ROOT,
					"FAIL  %d z %d wzorcow sie rozjechalo.%n"
					+ "      Refaktor zmienil zachowanie. Cofnij zmiane - NIE poprawiaj nagrania.%n",
					rozjazdow, sprawdzonych);
			System.exit(1);
		}
	}
}
