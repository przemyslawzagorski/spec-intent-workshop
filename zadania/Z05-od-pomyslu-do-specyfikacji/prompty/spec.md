# Prompt: napisz specyfikację po przesłuchaniu

Wklej **w tej samej sesji**, zaraz po zakończeniu grilla.

---

Teraz napisz specyfikację, w oparciu o moje odpowiedzi z przesłuchania.

Format: **EARS**. Każde wymaganie to jedno zdanie w jednym z czterech wzorców:

- `WHEN <zdarzenie>, the system shall <reakcja>` — reakcja na zdarzenie
- `IF <warunek>, THEN the system shall <reakcja>` — obsługa sytuacji niepożądanej
- `WHILE <stan>, the system shall <reakcja>` — zachowanie ciągłe w danym stanie
- `WHERE <cecha>, the system shall <reakcja>` — zachowanie zależne od wariantu

Układ dokumentu:

1. **Zdolność** — jedno zdanie: co system będzie umiał, czego dziś nie umie.
2. **Wymagania** — ponumerowane `R1.1`, `R1.2`, … pogrupowane w sekcje.
   Każde wymaganie musi dać się sprawdzić jednym testem.
3. **Założenia** — rzeczy, które przyjąłem, a których mi nie powiedziałeś.
   **Ta sekcja jest obowiązkowa i nie może być pusta.** Przy każdym założeniu
   napisz, co się stanie, jeśli okaże się fałszywe.
4. **Poza zakresem** — czego świadomie nie robimy, z powodem.
5. **Zmiany w istniejącym kodzie** — lista plików, które trzeba ruszyć,
   z jednym zdaniem przy każdym.

Twarde zasady:

- **Żadnego „powinien", „może", „w miarę możliwości".** Wymaganie albo jest
  sprawdzalne, albo nie jest wymaganiem.
- **Każda granica ma być domknięta jawnie.** Nie „w ciągu 30 dni", tylko
  „30 dni włącznie" albo „mniej niż 30 dni".
- Nie pisz o technologii. Żadnych klas, adnotacji, nazw frameworków — poza
  sekcją 5, gdzie wymieniasz pliki.
- Maksymalnie 80 linii.
