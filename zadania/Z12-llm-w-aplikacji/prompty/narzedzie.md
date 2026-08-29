# Prompt: dołóż narzędzie asystenta wraz z regułą

Wklej agentowi w katalogu `praca/Z12/harness-template/scoring-worker`.

---

Przeczytaj najpierw:

- `src/scoring/control/tools.py` — rejestr narzędzi i hook blokujący
- `src/scoring/control/assistant.py` — jak asystent ich używa
- `tests/test_assistant.py` — jak to jest testowane bez wywołania modelu

Chcę dołożyć narzędzie `get_return_policy_window(category)`, które zwraca okno
zwrotu dla podanej kategorii. Wartości są w `return-policy.yaml`, wczytuje je
`src/scoring/control/policy.py`.

Zanim napiszesz kod, odpowiedz mi na trzy pytania. **Przy każdym podaj
rekomendację:**

1. Co ma się stać, gdy model poprosi o kategorię, której nie ma w polityce?
   Wyjątek, wartość domyślna z polityki, czy pusta odpowiedź? Co zobaczy
   użytkownik po drugiej stronie w każdym z tych wariantów?
2. Czy to narzędzie potrzebuje ograniczenia zakresu danych, tak jak
   `list_customer_returns` ma podmieniany `customer_id`? Uzasadnij.
3. Czy nazwa kategorii przychodząca od modelu wymaga jakiejkolwiek kontroli,
   zanim trafi do odczytu polityki?

**Zatrzymaj się i czekaj na moje odpowiedzi.**

---

Potem napisz:

1. **Funkcję narzędzia** — w stylu istniejących, z docstringiem mówiącym,
   skąd biorą się dane.
2. **Wpis w `DOZWOLONE`.**
3. **Regułę w hooku**, jeśli z odpowiedzi wyszło, że jest potrzebna.
4. **Testy**: przypadek poprawny, przypadek brzegowy z pytania 1, oraz test
   sprawdzający, że narzędzie **spoza** listy nadal jest blokowane.

Twarde zasady:

- **Narzędzie ma być tylko do odczytu.** Nic, co zmienia stan.
- **Nie ufaj argumentom z modelu.** Jeśli argument da się ograniczyć po stronie
  kodu — ogranicz go, zamiast sprawdzać warunkiem. Warunek ma ścieżkę, którą
  można ominąć.
- **Żadnego nowego pakietu.** Wszystko, czego potrzebujesz, już jest.
- Testy mają przechodzić offline, bez wywołania modelu.

Na koniec uruchom `POLICY_FILE=../return-policy.yaml uv run --group dev pytest -q`
i pokaż wynik.
