# Prompt: dołóż endpoint pełnym cyklem

Wklej agentowi w katalogu `praca/Z11/harness-template`.

Prompt jest podzielony na kroki i **każdy kończy się zatrzymaniem**. To celowe:
chodzi o to, żebyś zobaczył wynik każdego kroku, zanim powstanie następny.
Jeśli wkleisz wszystko naraz, dostaniesz kod i nic więcej.

---

## Krok 1 — rozstrzygnij brzegi

Chcę dołożyć do serwisu `returns-service` endpoint zwracający listę zwrotów
klienta: `GET /returns?customerId={uuid}`.

Zanim cokolwiek napiszesz, przeczytaj:

- `returns-service/src/main/java/workshop/rma/returns/control/Schema.java`
- `returns-service/src/main/java/workshop/rma/returns/control/Returns.java`
- `docs/contract/openapi.yaml`

I odpowiedz mi na cztery pytania. **Przy każdym podaj rekomendację i uzasadnij
ją tym, co widzisz w kodzie — nie tym, jak się zwykle robi:**

1. Skąd ten serwis wie, do kogo należy zwrot? Sprawdź, jakie kolumny ma tabela
   `returns`.
2. Klient bez żadnych zwrotów — pusta lista czy 404? Czy ten serwis w ogóle
   wie, którzy klienci istnieją?
3. Brak parametru `customerId` — 400 czy lista wszystkich zwrotów?
4. W jakiej kolejności zwracamy wyniki?

**Zatrzymaj się i czekaj na moje odpowiedzi. Nie pisz kodu.**

---

## Krok 2 — specyfikacja i kontrakt

Na podstawie moich odpowiedzi:

1. Napisz 3–4 wymagania w notacji EARS, ponumerowane `R2.1`, `R2.2`, …
   Każde ma dać się sprawdzić jednym testem.
2. Dopisz operację do `docs/contract/openapi.yaml`. Parametr, odpowiedzi,
   schematy. Trzymaj się stylu, który już tam jest.

**Zatrzymaj się. Nadal nie piszemy kodu.**

---

## Krok 3 — testy

Napisz testy do `ReturnsResourceTest`. Zasady, które obowiązują w tym repo:

- **Tylko przez HTTP.** Żadnego zaglądania do środka poza zasiewem zamówień
  przez `Orders` — tak jak robią to istniejące testy.
- Każdy test ma w komentarzu identyfikator wymagania (`R2.1`).
- **Testy mają być czerwone.** Implementacji jeszcze nie ma i tak ma zostać.

Uruchom je i pokaż mi, że są czerwone z właściwego powodu — brak endpointu,
a nie błąd kompilacji testu.

**Zatrzymaj się.**

---

## Krok 4 — implementacja

Teraz napisz kod tak, żeby testy przeszły.

Twarde reguły tego repo:

- **Nie zmieniaj testów.** Jeśli test jest zły, powiedz mi, zamiast go poprawiać.
- **`control` nie może importować niczego z `jakarta.ws.rs`.** Wiedza o HTTP
  należy do `boundary`. Bramka to sprawdza.
- **Nie dodawaj zależności.** Wszystko, czego potrzebujesz, już jest.
- SQL piszesz ręcznie, parametryzowany. W tym projekcie nie ma ORM-a i to jest
  zapisana decyzja — patrz `docs/adr/001-postgres-bez-orm.md`.

Na koniec uruchom `./bramka` i pokaż mi wynik.

Jeśli bramka jest zamknięta — **napraw przyczynę, nie bramkę**.
