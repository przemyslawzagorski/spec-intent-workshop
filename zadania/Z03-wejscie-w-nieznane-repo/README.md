# Z03 · Wejście w nieznane repo

**35 min** · petclinic · bez bramki — to zadanie omawiamy

## O co chodzi

Dostajesz repo, którego nie znasz, i masz w nim coś zmienić. Agent może ci w tym
pomóc, ale najpierw musi sam zrozumieć, o co chodzi — a to jest moment, w którym
najczęściej zaczyna zmyślać.

Produktem jest `CONTEXT.md`: słownik domeny, który potem doklejasz do promptów
zamiast tłumaczyć wszystko od nowa.

**Agent napisze ten plik w trzy minuty. Wartość tego zadania jest w tym, co
zrobisz przez pozostałe trzydzieści** — w sprawdzeniu, co zmyślił.

## Jak zwykle to robimy

Otwierasz repo w IDE, klikasz w kilka plików, czytasz README i po dwudziestu
minutach masz mgliste wrażenie. Albo: wrzucasz agentowi całe repo i pytasz
„co to robi", dostajesz stronę tekstu o warstwach i wzorcach, i nadal nie wiesz,
czy wizyta ma weterynarza.

Boli, bo:

- **Opis architektury nie pomaga w pracy.** „Aplikacja stosuje wzorzec MVC" —
  i co z tego. Potrzebujesz wiedzieć, gdzie dodać pole i co się wtedy zepsuje.
- **Agent uzupełnia luki zdrowym rozsądkiem.** Jeśli w kodzie czegoś nie ma,
  a „normalnie by było", napisze, że jest. Brzmi wiarygodnie i jest fałszywe.
- **Nie wiesz, czego nie wiesz.** Najgroźniejsze są rzeczy, których w kodzie
  nie ma, a wszyscy zakładają, że są.

## Jakie są opcje

**Wklej całe repo i zapytaj.** Przy petclinicu (132 pliki) to jeszcze przejdzie.
Przy repo firmowym nie zmieści się w kontekście, a to, co się zmieści, utonie
w środku — o tym jest Z04. Kosztuje najwięcej i daje najbardziej ogólne odpowiedzi.

**Plik po pliku.** Dokładne i bardzo wolne. Tracisz obraz całości, bo agent widzi
drzewa, nie las.

**Mapa, a potem celowane wejścia.** Najpierw struktura i nazwy, potem trzy–cztery
pliki, które naprawdę niosą domenę. Tak czyta człowiek i tak warto prowadzić agenta.

## Jak zrobić dobrze

**Pytaj o pojęcia, nie o architekturę.** „Zbuduj słownik domeny" daje coś, czego
użyjesz. „Opisz architekturę" daje esej.

**Wymuś sekcję »czego tu nie ma«.** To jest cały trik tego zadania. Agent proszony
o opis napisze, co znalazł. Agent proszony **osobno** o to, czego szukał i nie
znalazł, musi zajrzeć w konkretne miejsca i przyznać się do pustki.

**Żądaj dowodu przy każdym twierdzeniu.** Ścieżka pliku, numer linii, nazwa
kolumny. Zdanie bez odnośnika traktuj jako hipotezę.

**Każ oddzielić wiedzę od domysłu.** Na końcu promptu: „wypisz, co napisałeś
na podstawie kodu, a co na podstawie tego, że tak zwykle bywa". Modele robią to
zaskakująco uczciwie, jeśli je o to wprost poprosić.

Jeśli masz skill `domain-modeling` — użyj go tutaj. Jeśli nie, prompt niżej robi
to samo ręcznie.

## Zrób to

```bash
./przygotuj Z03
cd praca/Z03/spring-petclinic
```

**1 · Zerknij sam, zanim odpalisz agenta** (5 min). Nie czytaj wszystkiego.
Wystarczy `find src/main/java -name '*.java' | head -30` i dwa pliki na wyrywki.
Chodzi o to, żebyś miał własne zdanie do porównania.

**2 · Odpal onboarding.** Prompt: [prompty/onboarding.md](prompty/onboarding.md).

**Co zobaczysz:** agent najpierw wypisze listę pojęć i listę powiązań, których
szukał i nie znalazł — dopiero potem napisze plik. Jeśli od razu zacznie pisać
`CONTEXT.md`, zatrzymaj go i poproś o te dwie listy; one są ważniejsze.

**Efekt:** plik `CONTEXT.md`, maksymalnie 70 linii. Zajmuje 2–4 minuty.

**3 · Teraz najważniejsze — sprawdź go** (15 min). Weź każde twierdzenie
z sekcji „Pojęcia" i znajdź je w kodzie. Konkretnie:

- Czy klasa, na którą się powołuje, istnieje?
- Czy powiązanie, które opisuje, ma odpowiednik w polu albo w kolumnie?
- Czy nazwy, których używa, to nazwy z kodu, czy z jego wyobraźni?

**Jedno pytanie, na które musisz odpowiedzieć sam, patrząc w kod:**

> Czy wizyta (`Visit`) jest w tym systemie powiązana z weterynarzem (`Vet`)?

Sprawdź sam, w dwóch miejscach:

```bash
grep -n "Vet" src/main/java/org/springframework/samples/petclinic/owner/Visit.java
grep -A6 "CREATE TABLE visits" src/main/resources/db/h2/schema.sql
```

**Co zobaczysz:** pierwsza komenda **nic nie wypisze**. Druga pokaże trzy
kolumny — `pet_id`, `visit_date`, `description`. Żadnego `vet_id`.

Teraz sprawdź, co na ten temat napisał twój agent.

**4 · Popraw `CONTEXT.md`** o to, co znalazłeś.

## Pytanie na czat

**Czy wasz agent połączył wizytę z weterynarzem?** Odpowiedzcie `tak` albo `nie`.
Jeśli `tak` — wklejcie zdanie, w którym to zrobił.

## Omówienie

Poproszę o pokazanie ekranu kogoś, kto odpowiedział `tak`, i kogoś, kto `nie`.

Co jest w kodzie naprawdę: tabela `visits` ma trzy kolumny — `pet_id`,
`visit_date`, `description`. Żadnego `vet_id`. W pakiecie `owner` słowo `Vet`
nie pada ani razu, w pakiecie `vet` nie pada `Visit` ani `Owner`.
**Ta aplikacja to dwie rozłączne połówki.**

To jest bardzo dobra pułapka, bo:

- W prawdziwej lecznicy wizyta oczywiście ma lekarza. Model ma rację co do
  świata i nie ma racji co do kodu.
- Zdanie „wizyta jest przypisana do weterynarza" brzmi całkowicie wiarygodnie.
  Nikt go nie zakwestionuje na przeglądzie.
- Gdybyś na tej podstawie zaplanował zadanie „dodaj filtr wizyt po weterynarzu",
  odkryłbyś problem dopiero w implementacji.

Pogadamy też o:

- **Drugiej niespodziance:** `new Visit()` ustawia datę na **jutro**
  (`Visit.java`, konstruktor). Ilu waszych agentów to zauważyło?
- **Co robić z takim plikiem dalej.** `CONTEXT.md` trafia do repo i doklejasz go
  do promptów przy zadaniach domenowych. Nie do `AGENTS.md` — tam siedziałby
  w każdym zapytaniu, a potrzebujesz go tylko czasem.
- **Kiedy go odświeżać.** Gdy dodajecie pojęcie, nie gdy zmieniacie kod.

## Kiedy to NIE ma sensu

Repo, które znasz na pamięć. Repo, w którym masz zrobić jedną literówkę. I repo
tak duże, że słownik całości byłby bezużyteczny — tam robisz `CONTEXT.md` dla
jednego modułu, nie dla wszystkiego.

## ★ Jeśli skończyłeś wcześniej

| ★ | Co robisz | Min |
|---|---|---|
| **Drugie narzędzie** | Ten sam prompt w Copilocie albo innym agencie. Porównaj oba `CONTEXT.md` — różnice pokazują, co jest w kodzie, a co w modelu. | 15 |
| **Pytanie nawigacyjne** | Zamiast „opisz domenę" zapytaj „gdzie mam dodać pole »ubezpieczyciel« do właściciela i co jeszcze muszę zmienić". Porównaj przydatność odpowiedzi. | 15 |
| **Diagram, który kłamie** | Poproś o diagram zależności w mermaid. Sprawdź każdą strzałkę w kodzie. Policz fałszywe. | 20 |
| **Reguła zapobiegawcza** | Weź miejsce, w którym agent zgadł źle, i napisz linię do `AGENTS.md`, która by temu zapobiegła. Sprawdź, czy działa. | 15 |
| **Repo w obcym języku** | Zrób to samo na projekcie w języku, którego nie znasz. Zobacz, ile z twojej oceny „czy to prawda" nadal działa. | 20 |

## Rozwiązanie

[rozwiazanie/CONTEXT.md](rozwiazanie/CONTEXT.md) — mój wariant. Każde twierdzenie
w nim sprawdziłem w kodzie; sekcja „Niespodzianki" ma numery plików.

Nie zaglądaj przed próbą. Twój będzie inny i to jest w porządku — porównaj,
czego **on** nie ma, czego **twój** nie ma, i co z tego jest naprawdę ważne.
