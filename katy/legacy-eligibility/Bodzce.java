import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bodzce dla zlotych wzorcow - JEDNO miejsce, w ktorym powstaja wejscia.
 *
 * <p>Nagraj i OdtworzWzorce buduja zamowienie i zgloszenie przez te sama metode.
 * Wczesniej kazdy robil to u siebie i rozjechalo sie po cichu: odtwarzacz na sztywno
 * wstawial ilosc 1 i powod CHANGED_MIND, wiec nie odtwarzal tego, co nagrywarka
 * zapisala. Siatka bezpieczenstwa, ktora sama nie jest odporna na rozjazd,
 * jest gorsza niz brak siatki - bo jej ufasz.
 */
final class Bodzce {

	static final Instant DOSTAWA = Instant.parse("2026-06-01T10:00:00Z");

	/**
	 * Jedno wywolanie do nagrania.
	 *
	 * <p>{@code wariant} opisuje ksztalt wejscia, a nie jego wartosci - dzieki temu
	 * do TSV daje sie zapisac takze przypadki, ktorych nie da sie wyrazic kolumnami:
	 * brakujace obiekty, puste listy, niedopasowane SKU.
	 */
	record Bodziec(String id, String opis, String wariant, String kategoria, long dni,
			BigDecimal cena, int ilosc, double abuse, String powod) {
	}

	static final String NAGLOWEK =
			"caseId\topis\twariant\tkategoria\tdniOdDostawy\tkwota\tilosc\tpowod\tabuseScore"
			+ "\tdecision\treasons\trefund";

	/**
	 * Siatka celowo NIEPELNA - i to jest cwiczenie.
	 *
	 * <p>Progi sa pokryte z obu stron, przypadki niekompletne tez. Czego tu nie ma,
	 * dopisujesz sam: patrz prompt-zlote-wzorce.md, krok 2.
	 */
	static List<Bodziec> wszystkie() {
		return List.of(
			b("L01", "default: dzien przed koncem okna", "inne", 13, "100.00"),
			b("L02", "default: OSTATNI dzien okna", "inne", 14, "100.00"),
			b("L03", "default: pierwszy dzien PO oknie", "inne", 15, "100.00"),
			b("L04", "elektronika: ostatni dzien okna", "elektronika", 30, "100.00"),
			b("L05", "elektronika: dzien po oknie", "elektronika", 31, "100.00"),
			new Bodziec("L06", "kategoria wykluczona", "ok", "oprogramowanie-cyfrowe", 1,
					new BigDecimal("100.00"), 1, 0.0, "DAMAGED"),
			b("L07", "dokladnie prog kwotowy", "inne", 1, "2000.00"),
			b("L08", "tuz powyzej progu kwotowego", "inne", 1, "2001.00"),
			new Bodziec("L09", "prog przegladu naduzyc", "ok", "inne", 1,
					new BigDecimal("100.00"), 1, 0.30, "CHANGED_MIND"),
			new Bodziec("L10", "prog odrzucenia naduzyc", "ok", "inne", 1,
					new BigDecimal("100.00"), 1, 0.60, "CHANGED_MIND"),
			b("L11", "po oknie ORAZ powyzej progu", "inne", 30, "3000.00"),
			new Bodziec("L12", "dzien zero - zgloszenie w dniu dostawy", "ok", "inne", 0,
					new BigDecimal("100.00"), 1, 0.0, "DAMAGED"),
			// Ilosc wchodzi do kwoty, wiec prog kwotowy da sie przekroczyc iloscia,
			// nie cena. Bez tego przypadku kolumna ilosc nigdy nie jest sprawdzana.
			new Bodziec("L13", "prog kwotowy przekroczony iloscia, nie cena", "ok", "inne", 1,
					new BigDecimal("700.00"), 3, 0.0, "CHANGED_MIND"),
			// Dane niekompletne - wymagane przez prompt, krok 2.
			new Bodziec("L14", "brak zamowienia (null)", "brak-zamowienia", "inne", 1,
					new BigDecimal("100.00"), 1, 0.0, "CHANGED_MIND"),
			new Bodziec("L15", "brak zgloszenia (null)", "brak-zgloszenia", "inne", 1,
					new BigDecimal("100.00"), 1, 0.0, "CHANGED_MIND"),
			new Bodziec("L16", "zgloszenie bez pozycji (pusta lista)", "pusta-lista", "inne", 1,
					new BigDecimal("100.00"), 1, 0.0, "CHANGED_MIND"),
			new Bodziec("L17", "SKU ze zgloszenia nie ma w zamowieniu", "inne-sku", "inne", 1,
					new BigDecimal("100.00"), 1, 0.0, "CHANGED_MIND")
		);
	}

	private static Bodziec b(String id, String opis, String kategoria, long dni, String cena) {
		return new Bodziec(id, opis, "ok", kategoria, dni, new BigDecimal(cena), 1, 0.0,
				"CHANGED_MIND");
	}

	/**
	 * Odtwarza bodziec z wiersza TSV - czyta WYLACZNIE kolumny wejsciowe.
	 *
	 * <pre>
	 *   0 caseId   1 opis    2 wariant  3 kategoria  4 dniOdDostawy  5 kwota
	 *   6 ilosc    7 powod   8 abuseScore
	 *   --- ponizej wyniki, nie wejscia: 9 decision  10 reasons  11 refund
	 * </pre>
	 */
	static Bodziec zTsv(String[] k) {
		return new Bodziec(k[0], k[1], k[2], k[3], Long.parseLong(k[4]), new BigDecimal(k[5]),
				Integer.parseInt(k[6]), Double.parseDouble(k[8]), k[7]);
	}

	/** Zamowienie albo null - zaleznie od wariantu. */
	static Map<String, Object> zamowienie(Bodziec b) {
		if (b.wariant().equals("brak-zamowienia")) {
			return null;
		}
		Map<String, Object> linia = new HashMap<>();
		linia.put("sku", "SKU-1");
		linia.put("category", b.kategoria());
		linia.put("unitPrice", b.cena());
		linia.put("quantity", b.ilosc());
		Map<String, Object> zam = new HashMap<>();
		zam.put("deliveredAt", DOSTAWA);
		zam.put("lines", List.of(linia));
		return zam;
	}

	/** Zgloszenie albo null - zaleznie od wariantu. */
	static Map<String, Object> zgloszenie(Bodziec b) {
		if (b.wariant().equals("brak-zgloszenia")) {
			return null;
		}
		Map<String, Object> pozycja = new HashMap<>();
		pozycja.put("sku", b.wariant().equals("inne-sku") ? "SKU-INNE" : "SKU-1");
		pozycja.put("quantity", b.ilosc());
		pozycja.put("reason", b.powod());
		Map<String, Object> zgl = new HashMap<>();
		zgl.put("requestedAt", DOSTAWA.plus(b.dni(), ChronoUnit.DAYS));
		zgl.put("items", b.wariant().equals("pusta-lista") ? List.of() : List.of(pozycja));
		return zgl;
	}

	/**
	 * Wynik wywolania, sprowadzony do trzech porownywalnych napisow.
	 *
	 * <p>Wyjatek tez jest wynikiem. Gdyby go tu nie bylo, refaktor usuwajacy straznika
	 * {@code null} wywracalby odtwarzacz zamiast zglaszac rozjazd.
	 */
	record Wynik(String decyzja, String powody, String zwrot) {
	}

	static Wynik wykonaj(legacy.ReturnEligibilityService serwis, Bodziec b) {
		try {
			var out = serwis.check(zamowienie(b), zgloszenie(b), b.abuse());
			@SuppressWarnings("unchecked")
			var powody = new java.util.ArrayList<>((List<String>) out.get("reasons"));
			java.util.Collections.sort(powody);
			return new Wynik(String.valueOf(out.get("decision")), String.join(",", powody),
					String.valueOf(out.get("refundAmount")));
		}
		catch (RuntimeException e) {
			return new Wynik("WYJATEK", e.getClass().getSimpleName(), "-");
		}
	}

	private Bodzce() {
	}
}
