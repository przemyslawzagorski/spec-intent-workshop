# Z06 · Dokumentacja jako kod

**30 min** · petclinic · bramka: `./sprawdz Z06`

## O co chodzi

Agent napisze ci dokumentację całego repo w trzy minuty. Będzie ładna, spójna
i częściowo nieprawdziwa. **Całe zadanie polega na tym, żeby ustalić która część.**

To najkrótsza praca agenta w całym warsztacie i najdłuższa ocena wyniku.

## Jak zwykle to robimy

README rośnie. Ktoś dopisuje akapit przy okazji swojej zmiany, ktoś inny nie
dopisuje nic. Po roku plik ma czterysta linii, połowa jest nieaktualna i nikt
nie wie która. Więc nikt go nie czyta — ani ludzie, ani agenci.

Boli, bo:

- **Nic tego nie sprawdza.** Kod ma testy i CI. Dokumentacja nie ma nic.
- **Nieaktualna dokumentacja jest gorsza niż żadna.** Żadna zmusza do przeczytania
  kodu. Nieaktualna daje fałszywą pewność.
- **Agent czyta ją tak samo jak ty** — i powiela błędy dalej, już we własnym kodzie.

## Jakie są opcje

**Nie pisać.** Uczciwe i częściej sensowne, niż się przyznajemy. Kod plus testy
plus `git log` to też dokumentacja. Wada: nowa osoba traci dwa tygodnie.

**Pisać ręcznie i pilnować na przeglądach.** Działa, dopóki ktoś tego pilnuje.
Przestaje działać dokładnie wtedy, gdy ta osoba idzie na urlop.

**Generować z kodu przy każdym buildzie** — javadoc, OpenAPI ze schematów.
Zawsze aktualne, ale opisuje wyłącznie sygnatury. Nie powie ci, dlaczego
weterynarz jest tylko do odczytu.

**Pisać ręcznie, ale postawić pod bramkę.** Dokumentacja jest w repo, buduje się
w CI, a build wywala się na złamanym linku. Wada: bramka sprawdza formę, nie prawdę.

## Jak zrobić dobrze

**Trzymaj dokumentację w repo, obok kodu.** W tym samym commicie, w tym samym
przeglądzie. Nie na wiki, nie w Confluence.

**Postaw ją pod build.** `mkdocs build --strict` zamienia ostrzeżenia w błędy.
Link do strony, której nie ma, przerywa build. To niewiele, ale to więcej niż zero
— i **jest zaskakująco dobrym wskaźnikiem**: agent, który wymyślił stronę, zwykle
wymyślił też jej zawartość.

**Postaw jedną twardą regułę: „nie wymyślaj, cytuj plik".** Każde zdanie o działaniu
systemu ma mieć w nawiasie ścieżkę. Nie ma ścieżki — nie ma zdania. To odsiewa
większość zmyśleń, bo agent, który musi wskazać plik, sam rezygnuje z części tez.

**Zapytaj osobno o to, czego nie znalazł.** Agent proszony o opis napisze, co
znalazł. Proszony **osobno** o pustki — musi w nie zajrzeć.

**Pisz dla kogoś, kto ma ten kod zmieniać**, nie uruchomić. Nie „czym jest Spring",
tylko „dlaczego kontrolery wołają repozytoria wprost i co z tego wynika".

## Jak wygląda dobra dokumentacja techniczna

To jest ta część, której zwykle brakuje: „napisz dokumentację" nie jest
poleceniem, dopóki nie wiesz, co znaczy dobra.

**Cztery rodzaje tekstu, których nie wolno mieszać** — podział z
[Diátaxis](https://diataxis.fr), najbardziej użytecznej ramy, jaką znam:

| Rodzaj | Odpowiada na | Czytelnik |
|---|---|---|
| **Tutorial** | „jak zacząć?" | nie zna niczego, chce sukcesu w kwadrans |
| **How-to** | „jak zrobić X?" | zna projekt, ma konkretne zadanie |
| **Reference** | „jakie są parametry Y?" | szuka faktu, nie chce narracji |
| **Explanation** | „dlaczego tak?" | rozumie jak, chce wiedzieć czemu |

Większość złej dokumentacji bierze się stąd, że jeden dokument próbuje robić
wszystkie cztery naraz. Strona „Uruchomienie" to **how-to** — nie tłumaczy,
czym jest Spring. Strona „Decyzje" to **explanation** — nie zawiera komend.

**Trzy rzeczy, które robią różnicę w praktyce:**

1. **Piszesz dla kogoś, kto ma ten kod ZMIENIĆ.** Nie uruchomić, nie ocenić.
   To zmienia wszystko: zamiast „aplikacja stosuje wzorzec MVC" piszesz
   „kontrolery wołają repozytoria wprost, więc reguła biznesowa siedzi
   w kontrolerze i nie da się jej przetestować bez warstwy webowej".
2. **Każde zdanie ma odnośnik do pliku.** To jedyna reguła, która realnie
   ogranicza zmyślanie.
3. **Sekcja „czego tu nie ma" jest osobna i obowiązkowa.** Bez niej agent
   napisze tylko to, co znalazł.

**Masz to spakowane jako skill.** Zamiast wklejać za każdym razem:

```bash
# z katalogu praca/Z06 — albo tam, gdzie twoje narzędzie szuka skilli
cp -r ../../zadania/Z06-dokumentacja-jako-kod/rozwiazanie/skill-dokumentacja ~/.claude/skills/
```

[rozwiazanie/skill-dokumentacja/SKILL.md](rozwiazanie/skill-dokumentacja/SKILL.md)
— przeczytaj go, zanim użyjesz. To jest też dobry przykład, jak wygląda
własnoręcznie napisany skill: nagłówek z opisem *kiedy* sięgnąć, potem instrukcja.

Jeśli twoje narzędzie nie ma skilli — otwórz ten plik i wklej treść jako prompt.
Efekt ten sam.

## Zrób to

```bash
./przygotuj Z06
cd praca/Z06
```

!!! Uwaga na pierwsze uruchomienie
    Pierwszy `uvx` **ciągnie paczki i trwa około dwóch minut**. Kolejne to
    4 sekundy. Odpal build raz, zanim zaczniesz pisać — żeby nie czekać
    w środku zadania.

    Wersje są przypięte (`mkdocs==1.6.*`). Jeśli zobaczysz komunikat
    o nadchodzącym mkdocs 2.0 — **to normalne**, to tylko zapowiedź.

**1 · Napisz dokumentację** (5 min pracy agenta).

Masz dwie drogi, obie dają ten sam efekt:

- **skill** `dokumentacja-repo` (skopiuj z rozwiązania, patrz wyżej),
- **prompt** [prompty/dokumentacja.md](prompty/dokumentacja.md) — do wklejenia.

Efekt: `mkdocs.yml`, cztery strony w `docs/` i `ZMYSLONE.md`.

**2 · Zbuduj** (5 min):

```bash
uvx --with 'mkdocs==1.6.*' --with 'mkdocs-material==9.*' mkdocs build --strict --site-dir .site
```

**Co zobaczysz, gdy jest dobrze:**

```
INFO    -  Documentation built in 1.55 seconds
```

**Co zobaczysz, gdy agent wymyślił stronę:**

```
WARNING -  Doc file 'index.md' contains a link 'nie-ma-mnie.md',
           but the target is not found among documentation files.
Aborted with 1 warnings in strict mode!
```

To jest dobra wiadomość — bramka działa. Napraw i powtórz.

Zobaczysz też komunikat o nadchodzącym **mkdocs 2.0**. To tylko zapowiedź,
nic nie jest zepsute — jesteśmy przypięci na wersji 1.6.

**3 · Teraz właściwa praca — sprawdź, co zmyślił** (10 min).

Weź stronę `docs/domena.md` i przy każdym zdaniu o powiązaniach znajdź plik.
Konkretnie sprawdź trzy rzeczy:

- Czy napisał, że **wizyta ma weterynarza**? (`db/h2/schema.sql`, `owner/Visit.java`)
- Czy napisał o **warstwie serwisów**? (poszukaj klasy `*Service` — nie ma żadnej)
- Czy napisał, że nowe pole trzeba dopisać do **listy dozwolonych pól**?
  (`OwnerController.setAllowedFields` — zajrzyj, co ta metoda naprawdę woła)

**4 · Uzupełnij `ZMYSLONE.md`** o to, co znalazłeś, i popraw dokumentację.

```bash
cd ../.. && ./sprawdz Z06
```

**Co zobaczysz:** cztery linie i `Gotowe.` Jeśli `ZMYSLONE.md` nie istnieje,
dostaniesz `uwaga` — to nie blokuje, ale ten plik jest tu najważniejszy.

## Pytanie na czat

**Wklejcie jedno zdanie, które agent napisał, a którego nie ma w kodzie.**
Dosłownie, jednym wierszem.

## Omówienie

Czytam kilka zdań z czatu na głos i za każdym razem pytam salę: *brzmi
wiarygodnie?* Zwykle brzmi. O to chodzi.

Pogadamy o:

- **Dlaczego akurat te zmyślenia.** Cztery z pięciu typowych opisują rzeczy,
  które w prawdziwej lecznicy oczywiście istnieją. **Model ma rację co do świata
  i nie ma racji co do kodu.** To jest najczęstszy tryb halucynacji przy
  dokumentacji — nie zmyślanie z powietrza, tylko wypełnianie luk zdrowym rozsądkiem.
- **Czego bramka nie łapie.** `--strict` sprawdza linki, nie prawdę. Bramka na
  formę jest tania i warto ją mieć, ale nie myl jej z weryfikacją.
- **Co można postawić pod bramkę naprawdę.** Test, że każdy publiczny endpoint
  ma wpis w dokumentacji. Test, że każda ścieżka wymieniona w dokumentacji
  istnieje na dysku. To drugie to dziesięć linii i łapie zaskakująco dużo.
- **Dokumentacja dla agenta kontra dla człowieka.** Dla człowieka: narracja,
  kontekst, „dlaczego". Dla agenta: ścieżki, nazwy, tabele. Częściowo to samo,
  ale nie do końca — i warto wiedzieć, dla kogo piszesz.

## Kiedy to NIE ma sensu

Projekt na trzy tygodnie. Kod, który sam siebie tłumaczy i ma dobre testy —
tam dokumentacja jest kolejnym miejscem do rozjechania się. I sytuacja, w której
nie masz kto by tego czytał: dokumentacja bez odbiorcy to praca dla archiwum.

## ★ Jeśli skończyłeś wcześniej

| ★ | Co robisz | Min |
|---|---|---|
| **Bramka na prawdę, nie na formę** | Napisz skrypt, który wyciąga z dokumentacji wszystkie ścieżki plików i sprawdza, że istnieją. Dołóż do bramki. | 20 |
| **Skąd się wzięło zmyślenie** | Weź jedno zmyślone zdanie i dojdź, na czym agent je oparł. Zwykle da się wskazać konkretny sygnał w kodzie. | 15 |
| **ADR dla decyzji, której nikt nie zapisał** | Napisz ADR dla „kontrolery wołają repozytoria wprost": kontekst, decyzja, konsekwencje, alternatywy. | 15 |
| **Diagram w mermaid** | Diagram modelu domeny, wersjonowany w repo. Sprawdź każdą strzałkę w schemacie bazy. Policz fałszywe. | 20 |
| **Dokumentacja dla agenta** | Przepisz `docs/domena.md` tak, żeby była optymalna dla agenta, nie dla człowieka. Porównaj rozmiar i zawartość. | 15 |
| **Endpoint bez wpisu** | Test, który przechodzi po wszystkich `@GetMapping`/`@PostMapping` i sprawdza, że każdy ma wzmiankę w `docs/`. | 20 |

## Rozwiązanie

[rozwiazanie/](rozwiazanie/) — cztery strony, `mkdocs.yml`, `ZMYSLONE.md`
**oraz gotowy skill** `skill-dokumentacja/`. Buduje się czysto pod `--strict`
(sprawdzone).

Najważniejszy plik to [rozwiazanie/ZMYSLONE.md](rozwiazanie/ZMYSLONE.md).
Zajrzyj tam nawet jeśli nie zaglądasz do reszty.
