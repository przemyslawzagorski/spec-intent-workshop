# Co agent zmyślił, a czego nie znalazł

Ten plik jest **produktem zadania**, nie efektem ubocznym. Bez niego nie wiesz,
ile z dokumentacji jest prawdą.

## Zdania napisane bez pokrycia w plikach

Poniżej to, co pojawiło się u mnie w pierwszym podejściu, a czego w kodzie nie ma.
Twoje będą inne — ale klasy błędów powtarzają się zaskakująco często.

| Co napisał | Jak jest naprawdę |
|---|---|
| „Wizyta jest przypisana do weterynarza, który ją przeprowadził" | `visits` ma `pet_id`, `visit_date`, `description`. Żadnego `vet_id`. |
| „Aplikacja stosuje warstwę serwisów między kontrolerem a repozytorium" | Nie ma żadnej klasy `*Service`. Kontroler woła repozytorium wprost. |
| „Weterynarza można dodać przez panel administracyjny" | Nie ma panelu. `VetRepository` ma wyłącznie odczyt. |
| „Nowe pole formularza trzeba dopisać do `setAllowedFields`" | To `setDisallowedFields`. Nowe pola są dopuszczone domyślnie. |
| „Wizyty mają godzinę" | `visit_date DATE`. Sama data. |

## Czego szukałem i nie znalazłem

- **Powiązania wizyty z weterynarzem** — w Javie, w schemacie i w danych
  startowych. Nie ma nigdzie.
- **Jakiejkolwiek dostępności lub grafiku weterynarza.** Pojęcie nie istnieje.
- **Statusu wizyty** (umówiona / odbyta / odwołana). Wizyta istnieje albo nie.
- **Ról i uprawnień.** Nie ma logowania ani pojęcia użytkownika.

## Dlaczego to najważniejszy plik w tym zadaniu

Wszystkie zmyślone zdania **brzmią wiarygodnie**. Cztery z pięciu opisują rzeczy,
które w prawdziwej lecznicy oczywiście istnieją. Model ma rację co do świata
i nie ma racji co do kodu.

Nikt tego nie zakwestionuje na przeglądzie dokumentacji. Zakwestionuje to
dopiero implementacja — trzy tygodnie później.

## Jak z tym walczyć

Trzy rzeczy, które zadziałały:

1. **„Nie wymyślaj, cytuj plik."** Wymóg podania ścieżki przy każdym zdaniu
   odsiewa większość. Agent, który musi wskazać plik, sam rezygnuje z części zdań.
2. **Osobne pytanie o to, czego nie znalazł.** Agent proszony o opis napisze,
   co znalazł. Proszony **osobno** o pustki — musi w nie zajrzeć.
3. **`mkdocs build --strict`.** Nie łapie kłamstw, ale łapie linki do stron,
   których nie ma. To zaskakująco dobry wskaźnik: agent, który wymyślił stronę,
   często wymyślił też jej zawartość.
