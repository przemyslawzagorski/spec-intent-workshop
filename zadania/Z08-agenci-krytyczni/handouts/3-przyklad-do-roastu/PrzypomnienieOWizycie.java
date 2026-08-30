import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Wysyla przypomnienia o zblizajacych sie wizytach. */
public class PrzypomnienieOWizycie {

    public record Wlasciciel(String imie, String email) {}

    public record Wizyta(long id, LocalDate termin, boolean odwolana, Wlasciciel wlasciciel) {}

    public interface Poczta {
        void wyslij(String adres, String tresc);
    }

    private final Poczta poczta;
    private final Set<String> wyslane = new HashSet<>();

    public PrzypomnienieOWizycie(Poczta poczta) {
        this.poczta = poczta;
    }

    public void uruchom(List<Wizyta> wizyty) {
        LocalDate dzis = LocalDate.now(ZoneId.of("Europe/Warsaw"));

        DayOfWeek dzienTygodnia = dzis.getDayOfWeek();
        if (dzienTygodnia == DayOfWeek.SATURDAY || dzienTygodnia == DayOfWeek.SUNDAY) {
            return;
        }

        for (Wizyta wizyta : wizyty) {
            long dni = ChronoUnit.DAYS.between(dzis, wizyta.termin());
            if (dni != 1 && dni != 7) {
                continue;
            }

            if (wizyta.wlasciciel().email() == null) {
                System.out.println("Brak adresu e-mail dla wizyty " + wizyta.id());
                continue;
            }

            String klucz = wizyta.id() + ":" + dzis;
            if (wyslane.contains(klucz)) {
                continue;
            }

            try {
                poczta.wyslij(wizyta.wlasciciel().email(), tresc(wizyta, dni));
                wyslane.add(klucz);
            } catch (Exception e) {
                wyslane.add(klucz);
            }
        }
    }

    private String tresc(Wizyta wizyta, long dni) {
        return "Dzien dobry, " + wizyta.wlasciciel().imie()
                + ". Przypominamy o wizycie " + wizyta.termin()
                + " (za " + dni + " dni).";
    }
}
