import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import legacy.ReturnEligibilityService;

/** Nagrywa zachowanie FAKTYCZNE legacy jako zlote wzorce. Nie ocenia go. */
public class Nagraj {
    static final Instant DOSTAWA = Instant.parse("2026-06-01T10:00:00Z");

    record Bodziec(String id, String opis, String kategoria, long dni,
                   BigDecimal cena, int ilosc, double abuse, String powod) {}

    public static void main(String[] args) {
        var s = new ReturnEligibilityService();
        var bodzce = List.of(
            new Bodziec("L01","default: dzien przed koncem okna","inne",13,new BigDecimal("100.00"),1,0.0,"CHANGED_MIND"),
            new Bodziec("L02","default: OSTATNI dzien okna","inne",14,new BigDecimal("100.00"),1,0.0,"CHANGED_MIND"),
            new Bodziec("L03","default: pierwszy dzien PO oknie","inne",15,new BigDecimal("100.00"),1,0.0,"CHANGED_MIND"),
            new Bodziec("L04","elektronika: ostatni dzien okna","elektronika",30,new BigDecimal("100.00"),1,0.0,"CHANGED_MIND"),
            new Bodziec("L05","elektronika: dzien po oknie","elektronika",31,new BigDecimal("100.00"),1,0.0,"CHANGED_MIND"),
            new Bodziec("L06","kategoria wykluczona","oprogramowanie-cyfrowe",1,new BigDecimal("100.00"),1,0.0,"DAMAGED"),
            new Bodziec("L07","dokladnie prog kwotowy","inne",1,new BigDecimal("2000.00"),1,0.0,"CHANGED_MIND"),
            new Bodziec("L08","tuz powyzej progu kwotowego","inne",1,new BigDecimal("2001.00"),1,0.0,"CHANGED_MIND"),
            new Bodziec("L09","prog przegladu naduzyc","inne",1,new BigDecimal("100.00"),1,0.30,"CHANGED_MIND"),
            new Bodziec("L10","prog odrzucenia naduzyc","inne",1,new BigDecimal("100.00"),1,0.60,"CHANGED_MIND"),
            new Bodziec("L11","po oknie ORAZ powyzej progu","inne",30,new BigDecimal("3000.00"),1,0.0,"CHANGED_MIND"),
            new Bodziec("L12","dzien zero - zgloszenie w dniu dostawy","inne",0,new BigDecimal("100.00"),1,0.0,"DAMAGED")
        );

        System.out.println("caseId\topis\tkategoria\tdniOdDostawy\tkwota\tabuseScore\tdecision\treasons\trefund");
        for (var b : bodzce) {
            Map<String,Object> linia = new HashMap<>();
            linia.put("sku","SKU-1"); linia.put("category",b.kategoria());
            linia.put("unitPrice",b.cena()); linia.put("quantity",b.ilosc());
            Map<String,Object> zam = new HashMap<>();
            zam.put("deliveredAt",DOSTAWA); zam.put("lines",List.of(linia));
            Map<String,Object> poz = new HashMap<>();
            poz.put("sku","SKU-1"); poz.put("quantity",b.ilosc()); poz.put("reason",b.powod());
            Map<String,Object> zgl = new HashMap<>();
            zgl.put("requestedAt",DOSTAWA.plus(b.dni(),ChronoUnit.DAYS));
            zgl.put("items",List.of(poz));

            var wynik = s.check(zam,zgl,b.abuse());
            @SuppressWarnings("unchecked") var powody = (List<String>) wynik.get("reasons");
            var posortowane = new ArrayList<>(powody); Collections.sort(posortowane);
            System.out.printf(java.util.Locale.ROOT,"%s\t%s\t%s\t%d\t%s\t%.2f\t%s\t%s\t%s%n",
                b.id(), b.opis(), b.kategoria(), b.dni(), b.cena(), b.abuse(),
                wynik.get("decision"), String.join(",",posortowane), wynik.get("refundAmount"));
        }
    }
}
