# Z05 · Od pomysłu do specyfikacji

**55 min** · petclinic · bez bramki — to zadanie omawiamy

## O co chodzi

Dostajesz dwa zdania od kogoś, kto wie, czego chce. Zanim powstanie kod, ktoś
musi rozstrzygnąć trzydzieści rzeczy, o których te dwa zdania milczą. Pytanie
brzmi: **kto to rozstrzygnie i czy się o tym dowiesz.**

To jedyne zadanie, którego agent za ciebie nie skróci. Przesłuchanie jest
rozmową i tempo wyznacza człowiek.

## Jak zwykle to robimy

„Zrób mi funkcję umawiania wizyt." Agent pisze kod. Kod się kompiluje, testy
przechodzą, wygląda dobrze. Trzy dni później okazuje się, że przyjmuje wizyty
w przeszłości, że dwie osoby mogą zająć ten sam termin, i że nikt nie wie,
co zrobić z wizytami, które już są w bazie.

Boli, bo:

- **Agent rozstrzygnął to sam, po cichu.** Nie skłamał — wybrał. Tylko nie
  powiedział, że wybiera, więc nie miałeś okazji się nie zgodzić.
- **Decyzje siedzą w kodzie.** Żeby dowiedzieć się, jak działa granica okna,
  trzeba przeczytać implementację.
- **Nie ma z czym porównać.** Skoro nie ma specyfikacji, to każde zachowanie
  kodu jest „zgodne z zamierzeniem".

## Jakie są opcje

**Napisz spec sam, potem daj agentowi.** Najlepsza kontrola, największy koszt
i tak zapomnisz o trzech przypadkach brzegowych.

**Niech agent napisze spec z twojego opisu.** Szybkie, ale dostaniesz ładnie
sformatowaną wersję swoich własnych luk. Agent wypełni je swoimi domysłami
i sformatuje tak, że będą wyglądać na ustalenia.

**Niech agent cię przesłucha, potem spisze.** Najwolniejsze i najlepsze.
Wychodzi to, czego nie wiedziałeś, że nie wiesz. Wada: wymaga twojego czasu
i uwagi — nie da się tego zrobić w tle.

## Jak zrobić dobrze

**Odwróć role.** Nie agent ma ci pomagać — ma cię przepytać. Zdanie
„nie pomagaj mi, nie pisz kodu" na początku promptu zmienia całą rozmowę.

**Wymuś rundy i rekomendacje.** Wszystkie pytania, na które da się dziś
odpowiedzieć, w jednej rundzie, ponumerowane, każde z rekomendowaną odpowiedzią.
Wtedy odpowiadasz „1-tak, 2-b, 3-twoja wersja" zamiast pisać wypracowania.
Pytanie, które zależy od odpowiedzi na inne pytanie z tej samej rundy, należy
do rundy następnej.

**Postaw limit trzech rund.** Bez limitu przesłuchanie nie ma końca — zawsze
znajdzie się kolejny przypadek brzegowy. Po trzeciej rundzie to, co zostało,
ląduje na liście „poza zakresem". **To jest cel, nie porażka.**

**Żądaj sekcji »Założenia« i nie pozwól jej być pustą.** To jest najważniejsza
sekcja w całym dokumencie. Zawiera rzeczy, które agent rozstrzygnął sam —
z tą różnicą, że teraz o nich wiesz.

**Pisz w EARS.** Cztery wzorce zdań, każde wymaganie sprawdzalne jednym testem.
Nie dlatego, że notacja jest magiczna, tylko dlatego, że **zmusza do domknięcia
granic**. Nie da się napisać zdania EARS z „w miarę możliwości".

Jeśli masz skill `grilling` albo `grill-me` — użyj go. Prompt niżej robi to samo
ręcznie.

## Zrób to

```bash
./przygotuj Z05
cd praca/Z05
```

**1 · Przeczytaj `INTENCJA.md`** (2 min). Leży w twoim katalogu `praca/Z05`.

**Co zobaczysz:** dwa zdania od kogoś z biznesu, a pod nimi cztery fakty o kodzie,
które się z nimi zderzają. Ta lista jest tam celowo — bez niej przesłuchanie
byłoby o niczym.

**2 · Przesłuchanie** (25 min). Prompt: [prompty/grill.md](prompty/grill.md),
albo skill `grilling`.

**Co zobaczysz:** ponumerowane pytania, przy każdym rekomendowaną odpowiedź,
i **agent się zatrzyma**. Nie napisze specyfikacji, nie pójdzie dalej — czeka
na ciebie. Odpowiadasz krótko: `1-tak, 2-b, 3-nie wiem`.

Jeśli agent zamiast pytań od razu produkuje dokument — przerwij i powtórz
prompt. Bez zatrzymania to nie jest przesłuchanie, tylko monolog.

**Trzy rundy. Nie więcej.** Jeśli nie wiesz — powiedz „nie wiem, zdecyduj
i wpisz do założeń". To jest poprawna odpowiedź, nie unik.

**3 · Specyfikacja** (20 min). W tej samej sesji:
[prompty/spec.md](prompty/spec.md).

**Efekt:** plik `SPEC.md` z pięcioma sekcjami — zdolność, wymagania w EARS,
**założenia**, poza zakresem, zmiany w kodzie. Maksymalnie 80 linii.

**4 · Sprawdź go** (8 min):

- Czy sekcja **Założenia** nie jest pusta? Jeśli jest — agent udaje, że wszystko
  wiedział. Każ mu ją wypełnić.
- Czy każda granica jest domknięta? Poszukaj „w ciągu", „do", „powyżej" bez
  słowa „włącznie".
- Czy jest choć jedno „powinien" albo „w miarę możliwości"? Wyrzuć.
- Czy sekcja **Zmiany w kodzie** wymienia **wszystkie trzy** schematy baz
  i **wszystkie trzy** pliki `data.sql`?

## Pytanie na czat

**Jedno pytanie z przesłuchania, którego się nie spodziewaliście.**
Jedno zdanie, dosłownie.

## Omówienie

Zbieram pytania z czatu i czytam kilka na głos. To zwykle najlepsza część dnia,
bo widać, że różne osoby zostały przyciśnięte w zupełnie różnych miejscach.

Pogadamy o:

- **Co znaczy dobre pytanie.** „Jak sobie to wyobrażasz" jest bezwartościowe.
  „Czy konflikt to równość momentu startu, czy nakładanie się przedziałów?"
  jest warte piętnastu minut późniejszego debugowania.
- **Sekcja Założenia jako produkt.** Pokażę wam swoją: sześć wierszy, przy
  każdym „co, jeśli to nieprawda". Ta tabela jest cenniejsza od wymagań, bo
  wymagania da się odtworzyć z kodu, a założenia nie.
- **Lista »poza zakresem« jako narzędzie polityczne.** Zapisane „nie robimy
  odwoływania wizyt, bo wymaga statusu, którego nie ma w modelu" ratuje przed
  rozmową „przecież to oczywiste, że miało być".
- **Ile z tego zrobiłby sam agent.** Poproszę kogoś, żeby po fakcie kazał
  agentowi napisać spec bez przesłuchania i porównał sekcję Założenia.

## Kiedy to NIE ma sensu

Zadanie, które sam rozumiesz w całości i możesz opisać jednym zdaniem bez
wyjątków. Prototyp, który sprawdza, czy coś w ogóle da się zrobić — tam
specyfikacja z góry zamraża odpowiedź na pytanie, które jeszcze zadajesz.
I sytuacja, gdy jesteś jedynym odbiorcą tej pracy i skończysz ją dziś.

## ★ Jeśli skończyłeś wcześniej

| ★ | Co robisz | Min |
|---|---|---|
| **Oddaj spec sąsiadowi** | Ktoś inny implementuje z twojej specyfikacji, bez rozmowy z tobą. Wszystko, o co musi zapytać, to dziura w spec. | 25 |
| **Spec dla czegoś, co już istnieje** | Napisz spec dla `OwnerController.processUpdateOwnerForm`, potem porównaj z kodem. Zobaczysz, ile reguł nie ma nigdzie zapisanych. | 20 |
| **Krytyk specyfikacji** | Nowa sesja, prompt „znajdź w tej specyfikacji sprzeczności i luki". Zobacz, ile znajdzie w dokumencie, który uznałeś za skończony. | 20 |
| **`to-tickets`** | Rozbij spec na bilety z zależnościami. **Uwaga:** ten skill pisze do issue trackera, więc najpierw trzeba uruchomić `setup-matt-pocock-skills` — albo zrobić to samo promptem. | 20 |
| **Grill na własnym backlogu** | Weź prawdziwe zadanie ze swojej pracy. Trzy rundy. To zwykle moment, w którym ludzie przestają traktować to jako ćwiczenie. | 20 |
| **Skill `wait-what`** | Gdy agent odpowie coś, czego nie rozumiesz, każ mu to wytłumaczyć od nowa, prościej. | 10 |

## Rozwiązanie

[rozwiazanie/SPEC.md](rozwiazanie/SPEC.md) — mój wynik. Zwróć uwagę na tabelę
założeń: sześć rzeczy, których nikt mi nie powiedział, a które musiałem
rozstrzygnąć, żeby dało się to zaimplementować.

Twoja specyfikacja będzie inna. Porównaj **założenia**, nie wymagania.
