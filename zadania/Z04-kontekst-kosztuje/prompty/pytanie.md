# Prompt do podejścia B — pytanie, które nazywa miejsca

W podejściu A pytasz krótko i pozwalasz agentowi szukać samemu:

> Chcę dodać do właściciela nowe pole: ubezpieczyciel. Co muszę zmienić?

Tutaj masz wersję B. **Ten sam cel, ale mówi agentowi, gdzie patrzeć i czego
nie zakładać.** Różnica między A i B jest treścią tego zadania.

Wklej w katalogu `praca/Z04/spring-petclinic`.

---

Chcę dodać do właściciela (`Owner`) nowe pole: **ubezpieczyciel** — nazwa firmy
ubezpieczeniowej, tekst, pole opcjonalne.

Wypisz mi **listę wszystkich plików, które trzeba zmienić**, żeby to działało
end-to-end: zapis przez formularz, odczyt na stronie właściciela, dane startowe
i testy.

Zanim odpowiesz, sprawdź konkretnie w tych miejscach:

- `src/main/java/.../owner/Owner.java` — jak wyglądają istniejące pola
- **`src/main/resources/db/`** — policz, ile jest podkatalogów i ile plików
  `schema.sql`. Nie zakładaj, że jest jeden.
- `src/main/resources/db/*/data.sql` — sprawdź, czy wstawki podają nazwy kolumn,
  czy są pozycyjne
- `src/main/resources/templates/owners/` — które szablony wyświetlają pola właściciela
- `src/test/java/.../owner/OwnerControllerTests.java` — czy testy formularza
  wymieniają pola

Przy każdym pliku napisz jednym zdaniem, co konkretnie się w nim zmienia.

Nie pisz jeszcze kodu. Chcę samą listę.

Na koniec odpowiedz osobno na dwa pytania:

1. **Czy coś się zepsuje, jeśli zmienię tylko klasę Javy i jeden schemat bazy?**
   Odpowiedz na podstawie tego, co zobaczyłeś w `data.sql`, nie z ogólnej wiedzy.
2. **Czy trzeba gdzieś dopisać nowe pole do listy dozwolonych pól formularza?**
   Zajrzyj do `OwnerController.setAllowedFields` i przeczytaj, co ta metoda
   naprawdę woła. Nie odpowiadaj na podstawie jej nazwy.
