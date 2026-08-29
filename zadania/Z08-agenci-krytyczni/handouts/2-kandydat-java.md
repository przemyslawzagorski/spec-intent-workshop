# Handout 2 — kandydat do przeglądu

**To jest „PR", który agent zgłosił do twojego projektu.**

Kontekst dla ciebie (recenzent go **nie dostanie** — dostanie tylko kod niżej):
nowa klasa `PartialRefunds` dodaje obsługę zwrotu częściowego z proporcjonalnym
podziałem kosztu wysyłki oraz zapisem do dziennika audytowego.

Kod **kompiluje się czysto u autora** i jego testy jednostkowe przechodzą.
To jest wycinek z gałęzi — `AuditSink` to interfejs dodany w tym samym PR,
którego tu nie pokazuję, bo recenzent i tak by go nie dostał.

Recenzent widzi **dokładnie tyle, ile ty poniżej**. Tak samo było w Bunie:
*„its context: only the diff"*.

Zawiera **trzy błędy** — po jednym z każdej klasy, którą oglądałeś w handoucie 1,
tylko w Javie. Znajdź je.

---

```java
package workshop.rma.returns.control;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import workshop.rma.returns.entity.Payer;
import workshop.rma.returns.entity.ReturnItem;

/** Zwrot czesciowy: proporcjonalny podzial kosztu wysylki i audyt. */
@ApplicationScoped
public class PartialRefunds {

    @Inject
    ReturnPolicy policy;

    @Inject
    AuditSink auditSink;

    /**
     * Kto placi za przesylke. Jesli operator recznie wskazal platnika,
     * uzywamy jego decyzji; w przeciwnym razie wyprowadzamy ja z powodu
     * pierwszej zwracanej pozycji.
     */
    public Payer resolvePayer(List<ReturnItem> items, Optional<Payer> override) {
        return override.orElse(this.policy.shippingPaidBy(items.getFirst().reason()));
    }

    /**
     * Rozbija roznice czasu na pelne dni i reszte godzin - uzywane w raporcie
     * audytowym, ktory pokazuje "3 dni i 5 godzin od dostawy".
     */
    public record Elapsed(long days, long hours) {
    }

    public Elapsed elapsedSinceDelivery(long hoursSinceDelivery) {
        var days = hoursSinceDelivery / 24;
        var hours = hoursSinceDelivery - days * 24;
        return new Elapsed(days, hours);
    }

    /** Proporcjonalny koszt wysylki na jedna zwracana pozycje. */
    public BigDecimal shippingPerItem(BigDecimal totalShipping, int returnedItems) {
        return totalShipping.divide(BigDecimal.valueOf(returnedItems), 2, RoundingMode.HALF_UP);
    }

    /** Wysyla dokument audytowy do zewnetrznego magazynu, nie blokujac zadania. */
    public void archiveAudit(Path auditDocument) {
        try (InputStream content = Files.newInputStream(auditDocument)) {
            CompletableFuture.runAsync(() -> this.auditSink.store(content));
        } catch (Exception e) {
            throw new IllegalStateException("nie da sie zarchiwizowac " + auditDocument, e);
        }
    }
}
```

---

## Zadanie

1. **10 minut, sam.** Znajdź co się da. Zapisz numer linii i jednym zdaniem, co się stanie.
2. Potem wklej **tylko powyższy kod** — bez tego opisu — do **drugiego narzędzia**
   (jeśli pracujesz na Claude, użyj Copilota i odwrotnie) razem z promptem
   [`../prompty/krytyk.md`](../prompty/krytyk.md).
3. Porównaj: ile znalazłeś ty, ile agent-krytyk, a ile żaden z was.

> **Dlaczego drugie narzędzie, a nie nowa sesja tego samego?** Chodzi o brak
> wspólnego kontekstu. Nowa sesja tego samego narzędzia często ma jeszcze dostęp
> do plików projektu i twoich wcześniejszych ustaleń. Recenzent ma widzieć
> **wyłącznie kod**, tak jak w Bunie: *„its context: only the diff"*.
