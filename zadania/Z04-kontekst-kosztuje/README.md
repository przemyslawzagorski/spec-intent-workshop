# Z04 · Kontekst kosztuje

**40 min** · petclinic · bez bramki — to zadanie omawiamy

## O co chodzi

Wszystko, co wkładasz do promptu, kosztuje dwa razy: pieniędzmi i uwagą modelu.
To zadanie pokazuje jedno i drugie na liczbach.

Po drodze dwa mechanizmy, które warto rozumieć, bo zmieniają sposób, w jaki
układasz prompty: **cache** i **lost in the middle**.

## Jak zwykle to robimy

„Na wszelki wypadek" wrzucamy więcej. Cały katalog, całą dokumentację, długi
`AGENTS.md` z regułami na każdą okazję. Intuicja mówi: im więcej kontekstu, tym
lepsza odpowiedź.

Boli, bo:

- **Płacisz za każdy token przy każdym zapytaniu.** Nie raz — za każdym razem.
- **Więcej kontekstu to nie lepsza odpowiedź.** Powyżej pewnego progu jest gorsza.
- **To, co ważne, tonie.** Instrukcja w środku długiego promptu bywa pomijana.

Konkretnie na petclinicu: całe repo to ~357 000 tokenów, z czego **67% to fonty,
CSS i obrazki**. Największy pojedynczy plik to webfont w SVG na 378 KB — czyli
jakieś 95 000 tokenów ścieżek wektorowych. Java to 9,7% całości.

## Kontekst to nie tylko pliki z kodem

To jest część, o której się nie myśli, bo jej nie widać. W promptcie siedzi
znacznie więcej niż to, co świadomie wkleiłeś:

| Co | Ile mniej więcej | Czy o tym pamiętasz |
|---|---|---|
| **Definicje narzędzi** — każde narzędzie agenta ma schemat JSON | 100–400 tokenów **za narzędzie** | prawie nigdy |
| **Serwery MCP** — każdy dokłada wszystkie swoje narzędzia | serwer z 20 narzędziami to kilka tysięcy tokenów | nie |
| **Plik reguł** — `AGENTS.md`, `CLAUDE.md`, `.cursorrules` | tyle, ile napisałeś | tak, ale rzadko liczysz |
| **Nagłówki skilli** — nazwa i opis każdego zainstalowanego | kilkadziesiąt za skill | nie |
| **Prompt systemowy narzędzia** | kilka tysięcy | nie masz na to wpływu |
| **Historia rozmowy** | rośnie z każdą turą | czujesz dopiero, gdy zwolni |
| **Pliki, które wkleiłeś** | to, co widzisz | tak |

**Najdroższa pozycja to zwykle ta, o której nie wiesz.** Podpinasz serwer MCP
do bazy, bo raz potrzebowałeś schematu — i od tej pory **każde** zapytanie,
także „popraw literówkę w README", niesie ze sobą opis dwudziestu narzędzi,
z których nie korzystasz.

### Efekt kuli śnieżnej

Te rzeczy się nie sumują — one się **mnożą przez liczbę tur**.

Model nie pamięta rozmowy; przy każdej turze dostaje ją całą od nowa. Więc
narzędzia, reguły i historia lecą **za każdym razem**:

```
tura 1:  narzędzia + reguły + pytanie
tura 2:  narzędzia + reguły + tura 1 + pytanie
tura 3:  narzędzia + reguły + tura 1 + tura 2 + pytanie
...
```

Stąd dwa wnioski, których nie widać z pojedynczego zapytania:

- **Kilkaset tokenów na starcie to kilkadziesiąt tysięcy po dwudziestu turach.**
  Cache to znacznie tanieje, ale **uwagi modelu nie da się scache'ować** —
  im dłuższy kontekst, tym mocniej działa *lost in the middle*.
- **Rozmowa, która „głupieje" po godzinie**, zwykle nie głupieje. Ona się zatkała.
  Dlatego `/clear` bywa skuteczniejszy od trzech kolejnych doprecyzowań.

### Co z tym zrobić — trzy rzeczy, które faktycznie działają

1. **Odłączaj serwery MCP, których nie używasz w tym zadaniu.** To jest
   najtańsza oszczędność, jaka istnieje: jedno kliknięcie, zero utraconej jakości.
   Wracamy do tego w bonusie [B3](../../bonusy/B3-mcp/).
2. **Trzymaj plik reguł mały** — Z01. Nie dlatego, że 2 KB to dużo, tylko
   dlatego, że mnoży się przez każdą turę każdego dnia.
3. **Zaczynaj nową sesję do nowego zadania.** Historia poprzedniego zadania
   nie pomaga w następnym, a kosztuje przy każdej turze.

## Dwa mechanizmy, które trzeba znać

### Cache — płacisz raz za to, co się nie zmienia

Cache działa na **prefiks**. Dostawca zapamiętuje początek promptu i przy
kolejnym zapytaniu nie przetwarza go od nowa — pod warunkiem, że ten początek
jest **bajt w bajt identyczny**.

Trzy rzeczy, które z tego wynikają:

1. **Odczyt z cache'u kosztuje około dziesiątej części normalnego wejścia.**
   Zapis kosztuje trochę więcej niż normalne wejście. Więc opłaca się od
   drugiego użycia.
2. **Prefiks musi się zgadzać dokładnie.** Jedna zmieniona spacja na początku
   unieważnia wszystko, co po niej.
3. **Kolejność ma znaczenie ekonomiczne.** Stabilne rzeczy na początek, zmienne
   na koniec. Jeśli wstawisz aktualną datę na górze promptu, kasujesz sobie cache
   przy każdym wywołaniu.

Skala, na której to widać: przy przepisywaniu Buna z Ziga na Rusta zużyto
**5,9 mld tokenów wejścia niecache'owanego i 72 mld odczytów z cache'u**.
Czyli **92% wejścia to były trafienia w cache**. Przy dziesięciokrotnie niższej
cenie za odczyt oznacza to, że rachunek za tę część był mniej więcej taki, jak
za 7 mld tokenów zamiast 72 mld.

### Lost in the middle — środek to najgorsze miejsce

Badanie *Lost in the Middle* (Liu i in., 2023) pokazało coś, co potem wiele razy
potwierdzano: skuteczność modelu w znajdowaniu informacji w długim kontekście
układa się w **kształt litery U**. Najlepiej na początku, prawie tak dobrze na
końcu, wyraźnie gorzej w środku. Powiększenie okna kontekstowego tego nie naprawia.

Praktycznie: **instrukcję dawaj na końcu**, tuż przed miejscem, w którym model
ma zacząć pisać. Materiał referencyjny na początek.

I tu ładnie się spina: **cache chce, żeby stabilne było na początku. Uwaga chce,
żeby ważne było na końcu. Oba zgadzają się, że środek to najgorsze miejsce na
cokolwiek istotnego.**

## Jak zrobić dobrze

**Wkładaj to, co potrzebne do tego zadania.** Nie „na wszelki wypadek".

**Układaj prompt w trzech warstwach:**

1. **Stałe** — reguły, słownik domeny. Rzadko się zmieniają, więc siedzą w cache'u.
2. **Materiał** — pliki potrzebne akurat teraz.
3. **Polecenie** — na samym końcu, konkretne.

**Mierz, nie zgaduj.** Poniżej robisz dokładnie to.

## „Ale moje narzędzie i tak samo sobie bierze, co chce"

To jest najczęstsze i najsłuszniejsze zastrzeżenie do tego tematu. Claude Code,
Copilot i Augment nie czekają, aż wkleisz plik — mają wyszukiwanie po repo
i wciągają to, co uznają za potrzebne.

Więc po co w ogóle o tym rozmawiać? Cztery powody:

**1 · Nie kontrolujesz, co przeczyta. Kontrolujesz, od czego zacznie.**
Wyszukiwanie agenta jest sterowane twoim promptem. Pytanie, które nazywa pliki,
zbiega się w jednym kroku. Pytanie ogólne każe mu grepować po repo i wciągać
wszystko, co wygląda podobnie.

**2 · To, co wkleisz, jest przeczytane na pewno. To, co znajdzie sam, jest loterią.**
Jeśli coś jest niezbędne — nazwij to. Nie licz, że trafi.

**3 · `AGENTS.md` jest jedynym miejscem, gdzie budżet masz w pełni pod kontrolą.**
Bo siedzi w każdym promptcie, zawsze, niezależnie od tego, co narzędzie wyszuka.

**4 · Musisz wiedzieć, co ile waży, żeby poznać, kiedy narzędzie robi coś głupiego.**
„Wrzuć całe repo" na petclinicu znaczy: dwie trzecie kontekstu to fonty i CSS.
Jeśli tego nie wiesz, nie zauważysz, że coś jest nie tak.

**Dlatego to ćwiczenie nie polega na ręcznym kurowaniu każdego promptu.**
Polega na zobaczeniu, **jak bardzo sposób pytania zmienia to, co agent wciągnie**.

## Gdzie w ogóle zobaczyć te tokeny

Zależy od narzędzia i to jest część problemu.

| Gdzie | Jak |
|---|---|
| **Claude Code** | `/context` pokazuje, co siedzi w kontekście i ile zajmuje; `/cost` pokazuje zużycie sesji |
| **Copilot / inne** | zwykle nie pokazują wprost — sprawdź w ustawieniach albo w logach |
| **API bezpośrednio** | pole `usage` w odpowiedzi: wejście, wyjście, odczyty z cache'u |
| **Offline, na plikach** | `uv run tools/bench.py estimate <pliki>` — przybliżenie 4 znaki na token |

**Jeśli twoje narzędzie nie pokazuje tokenów — mierz to, co widać:** które pliki
agent otworzył i ile razy. Większość narzędzi to pokazuje, a to jest ta sama
informacja w innej walucie.

## Zrób to

```bash
./przygotuj Z04
cd praca/Z04/spring-petclinic
```

**1 · Zobacz rozkład kosztu** (5 min):

```bash
cd ../../..
uv run tools/bench.py estimate praca/Z04/spring-petclinic/src/main/java/org/springframework/samples/petclinic/owner/*.java
```

**Co zobaczysz:** tabelę plik → bajty → przybliżone tokeny, a pod nią sumę.
Cały pakiet `owner` to około **4 500 tokenów**.

Potem zerknij, co dominuje w całym repo:

```bash
cd praca/Z04/spring-petclinic
du -sh src/main/resources/static/resources/fonts src/main/java
```

**Co zobaczysz:** fonty ważą **więcej niż cały kod Javy**. To jest ta połowa
zdania „wrzuć całe repo", o której nikt nie myśli.

**2 · Podejście A — pytanie ogólne** (10 min). Nowa sesja. Nie wskazujesz
żadnego pliku, niech agent szuka sam:

> Chcę dodać do właściciela nowe pole: ubezpieczyciel. Co muszę zmienić?

**Co zobaczysz:** agent zacznie od szukania — grep, otwieranie plików —
i dopiero potem odpowie. **Notuj, które pliki otworzył i ile ich było.**
To jest właściwy pomiar tego zadania, ważniejszy niż liczba tokenów.

Odpowiedź prawie na pewno wymieni `Owner.java`, jeden schemat bazy i szablony.
**Pytanie brzmi, czy wymieni wszystkie trzy schematy.**

**3 · Podejście B — pytanie, które nazywa miejsca** (10 min). Nowa sesja,
pełny prompt z [prompty/pytanie.md](prompty/pytanie.md) — ten sam cel, ale mówi
agentowi, gdzie patrzeć i czego nie zakładać.

**4 · Porównaj — i to jest sedno** (10 min):

| Co porównujesz | Na co patrzysz |
|---|---|
| **liczba otwartych plików** | czy A wciągnęło rzeczy niepotrzebne |
| **czy padły wszystkie trzy schematy baz** | petclinic ma H2, MySQL i Postgres |
| **czy padły trzy pliki `data.sql`** | wstawki pozycyjne, dodanie kolumny je psuje |
| **odpowiedź na pytanie o `setAllowedFields`** | czy agent zgadł po nazwie, czy przeczytał |
| **tokeny albo czas**, jeśli twoje narzędzie pokazuje | |

**Uprzedzam: A może wypaść dobrze.** Nowoczesne narzędzia radzą sobie z takim
pytaniem. Wtedy wnioskiem jest: *„na repo tej wielkości nie muszę kurować
kontekstu ręcznie"* — i to też jest wynik warty wyciągnięcia.

**Sprawdź jednak jedną rzecz:** czy A wymieniła **wszystkie trzy** schematy.
Tu najczęściej wychodzi różnica.

```bash
cd ../../..
uv run tools/bench.py record --etykieta "Z04 ogolne"  --model <twój> --iteracje 1 --kto <imię>
uv run tools/bench.py record --etykieta "Z04 celowane" --model <twój> --iteracje 1 --kto <imię>
uv run tools/bench.py report
```

## Pytanie na czat

**Ile schematów baz wymienił każdy wariant i ile plików agent otworzył?**
Format: `A: 1 schemat / 12 plikow   B: 3 schematy / 6 plikow`.

## Omówienie

Poproszę o ekran kogoś, u kogo **A wymieniła tylko jeden schemat baz**,
i kogoś, u kogo A poradziła sobie w pełni. Obie sytuacje się zdarzają i różnica
między nimi jest ciekawsza niż sam wynik.

**Nie próbuję was przekonać, że trzeba ręcznie kurować kontekst.** Przy repo
tej wielkości narzędzie zwykle sobie poradzi. Teza jest węższa i mocniejsza:

> **Sposób, w jaki pytasz, decyduje o tym, co agent wciągnie.**
> Nie masz kontroli nad jego wyszukiwaniem, ale masz nad punktem startu.

Pokażę wam jeszcze jedną rzecz, którą sprawdziłem, robiąc to zadanie. Dodałem
kolumnę do `db/h2/schema.sql` i **`OwnerControllerTests` przeszło — piętnaście
testów, zero błędów.** Bo to `@WebMvcTest` z mockiem repozytorium; nigdy nie
dotyka bazy. Aplikacja przy starcie wywala się na `data.sql`, ale ten zestaw
testów o tym nie wie. Wrócimy do tego w Z07.

Pogadamy o:

- **Kiedy więcej kontekstu naprawdę pomaga.** Gdy nie wiesz, gdzie szukać.
  Wtedy pierwszy przebieg robisz szeroko, znajdujesz miejsce, i **drugi przebieg
  robisz wąsko**. To nie jest wybór raz na zawsze, tylko dwie fazy.
- **Co się zmienia przy repo dziesięć razy większym.** Tam wyszukiwanie agenta
  przestaje trafiać, bo podobnych plików jest sto. Techniki z tego zadania
  zaczynają być konieczne dokładnie wtedy, gdy przestają być wygodą.
- **Że w narzędziu, które indeksuje, i tak masz dwa pewniki:** `AGENTS.md` jest
  w każdym promptcie, a to, co wkleisz jawnie, jest przeczytane na pewno.
  Cała reszta to prawdopodobieństwo.
- **Co siedzi w każdym waszym promptcie.** `AGENTS.md` z Z01 razy liczba waszych
  interakcji dziennie. Policzcie to sobie na własnym `AGENTS.md`.
- **`/clear` kontra `/compact`.** `/clear` wyrzuca wszystko i buduje cache od
  nowa. `/compact` streszcza — tanio w tokenach, ale streszczenie robi model
  i coś zgubi. Po `/compact` warto sprawdzić, czy nie zniknęło ustalenie,
  na którym ci zależy.
- **Dlaczego wielkie okno kontekstowe nie rozwiązuje problemu.** Bo problemem
  nie jest pojemność, tylko uwaga.

## Kiedy to NIE ma sensu

Jednorazowe pytanie. Zadanie eksploracyjne, gdzie naprawdę nie wiesz, czego
szukasz — tam szerokie wejście jest tańsze niż dziesięć wąskich prób.
I sytuacja, w której twój czas kosztuje więcej niż tokeny, a tak jest częściej,
niż się nam wydaje.

## ★ Jeśli skończyłeś wcześniej

| ★ | Co robisz | Min |
|---|---|---|
| **Lost in the middle na żywo** | Weź prompt z podejścia A. Przenieś polecenie z końca na sam środek, między pliki. Zadaj to samo pytanie. Porównaj odpowiedź. | 15 |
| **Zabij sobie cache** | Wstaw aktualny czas na początku promptu i zrób trzy zapytania. Potem to samo z czasem na końcu. Porównaj koszt wejścia — jeśli twoje narzędzie go pokazuje. | 15 |
| **Policz swój koszt** | Rozmiar `AGENTS.md` × liczba twoich interakcji dziennie × 250 dni roboczych. Czy któraś reguła jest tego warta? | 10 |
| **Zmierz koszt narzędzi** | Sprawdź w swoim narzędziu, ile tokenów zajmują same definicje narzędzi i serwery MCP (w Claude Code: `/context`). Odłącz serwer, którego dziś nie używasz, i zmierz jeszcze raz. | 15 |
| **Kula śnieżna na żywo** | Zadaj to samo pytanie w świeżej sesji i po dwudziestu turach rozmowy o czymś innym. Porównaj jakość odpowiedzi, nie tylko koszt. | 20 |
| **Trzy modele** | To samo pytanie, ten sam kontekst, trzy modele. Kiedy słabszy wystarcza? | 25 |
| **Repo, które się nie mieści** | Wymyśl trzy strategie dla repo na 5 mln tokenów i wypisz koszt każdej: mapa plików, wyszukiwanie, indeks wektorowy. | 20 |

## Rozwiązanie

[rozwiazanie/POMIAR.md](rozwiazanie/POMIAR.md) — moje liczby dla petclinica
i to, co z nich wynika.
