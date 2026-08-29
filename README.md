# Warsztat: praca z agentami AI

Dwa dni, dwanaście niezależnych zadań. Każde pokazuje jedną nazwaną technikę
na prawdziwym kodzie i domyka się w jednej sesji.

**Zadania są niezależne.** Możesz zacząć od dowolnego. Nic nie dziedziczy po
niczym, nikt nie czeka na cudzy wynik, wszyscy pracują na tym samym stanie.

Na koniec dostajesz działający system z kompletnym harnessem — Quarkus, FastAPI,
Kafka, Postgres, bramka, CI — do zabrania i użycia u siebie.

---

## Zanim przyjdziesz

Zrób to w domu, nie na sali.

```bash
git clone <adres-repo>
cd spec-intent-workshop
bash docs/setup/bootstrap.sh
source .tooling/env.sh
./sprawdz env
```

**Co zobaczysz:** osiem linii zaczynających się od `jest`, a na końcu `Gotowe.`
Jeśli któraś mówi `BRAK` — napisz do mnie przed zajęciami, nie w dniu zajęć.

**Ile to trwa:** `bootstrap.sh` schodzi zwykle w **1–2 minuty** i ściąga
**około 350 MB** (JDK 25, Maven, archetyp, petclinic). Wszystko ląduje
w `.tooling/`, nic w systemie — kasujesz ten katalog i po śladzie.

**`source .tooling/env.sh` musisz zrobić w każdej nowej sesji terminala.**
Ustawia JDK, Mavena, wykrywa Dockera i ustawia `MAVEN_ARGS`, dzięki czemu
zwykłe `mvn` omija firmowy mirror.

Do tego obrazy i narzędzia, żeby nie czekać w trakcie zajęć. **To jest większe
pobranie niż bootstrap — około 900 MB** i najlepiej zrobić to wieczorem:

```bash
docker pull maven:3.9-eclipse-temurin-25
docker pull eclipse-temurin:25-jre
docker pull postgres:17-alpine
uvx --with 'mkdocs==1.6.*' --with 'mkdocs-material==9.*' mkdocs --version
```

**Co zobaczysz:** cztery razy `Downloaded ...` albo `Image is up to date`,
a na końcu numer wersji mkdocs. Ostatnia komenda przy pierwszym uruchomieniu
trwa **około dwóch minut** — potem cztery sekundy.

**Potrzebujesz też agenta.** Claude Code, Copilot, Augment — obojętnie który.
Warsztat jest agnostyczny: wszystko, czego używamy, to pliki, komendy i Maven.

**Potrzebujesz powłoki POSIX.** Na Windowsie: **Git Bash** (idzie z Git for Windows)
albo WSL. Skrypty używają `source`, `chmod` i `./` — w `cmd.exe` ani PowerShellu
nie zadziałają. Na macOS i Linuksie nic nie musisz robić.

Skille instalujemy w Z01 — a czym w ogóle są, wyjaśnia
[docs/skille.md](docs/skille.md).

---

## Jak to działa — trzy komendy i nic więcej

Każde zadanie wygląda tak samo:

```bash
./przygotuj Z03      # 1. stawia to, czego Z03 potrzebuje
cd praca/Z03         # 2. tu pracujesz
./sprawdz Z03        # 3. sprawdza, czy skończone
```

**Co zobaczysz przy `./przygotuj`:** kilka szarych linii z komendami, które
skrypt wykonał, i `Gotowe. Pracuj w praca/Z03/`. Trwa to kilka sekund.

**Co zobaczysz przy `./sprawdz`:** listę sprawdzeń, każde zaczyna się od
`jest`, `BRAK` albo `uwaga`, a na końcu `Gotowe.` albo `Jeszcze nie.`
`uwaga` **nie blokuje** — to podpowiedź, nie błąd.

**Chcesz wiedzieć, co dokładnie robi `przygotuj`?**

```bash
./przygotuj Z03 --pokaz
```

Wypisze co do znaku każdą komendę, którą by wykonał, i nie wykona żadnej.
Żaden krok w tym warsztacie nie jest magiczny.

`./przygotuj` i `./sprawdz` bez argumentu wypiszą pomoc.

**Nie wszystkie zadania mają bramkę.** Przy planowaniu, onboardingu czy rozmowie
o kontekście nie ma czego sprawdzać maszynowo — tam liczy się to, co zobaczysz
i o czym pogadamy.

---

## Zadania

### Dzień 1 — ustawić sobie robotę i wejść w kod

| | Zadanie | Czas | Bramka |
|---|---|---|---|
| **[Z01](zadania/Z01-init-workspace/)** | Ustaw sobie warsztat — reguły, skille, hook, który blokuje | 30 | tak |
| **[Z02](zadania/Z02-agenty-kochaja-przyklady/)** | Agenty kochają przykłady — proza kontra wzorzec, potem archetyp | 35 | tak |
| **[Z03](zadania/Z03-wejscie-w-nieznane-repo/)** | Wejście w nieznane repo — słownik domeny i to, co agent zmyślił | 35 | — |
| **[Z04](zadania/Z04-kontekst-kosztuje/)** | Kontekst kosztuje — cache, lost in the middle, pomiar | 40 | — |
| **[Z05](zadania/Z05-od-pomyslu-do-specyfikacji/)** | Od pomysłu do specyfikacji — przesłuchanie i EARS | 55 | — |
| **[Z06](zadania/Z06-dokumentacja-jako-kod/)** | Dokumentacja jako kod — mkdocs pod bramką | 30 | tak |

### Dzień 2 — jakość, refaktor, dostawa

| | Zadanie | Czas | Bramka |
|---|---|---|---|
| **[Z07](zadania/Z07-harness/)** | Harness — HARD i SOFT, odcisk testów, ta sama bramka w CI | 50 | tak |
| **[Z08](zadania/Z08-agenci-krytyczni/)** | Agenci krytyczni — trzy błędy z przepisania Buna | 50 | — |
| **[Z09](zadania/Z09-legacy-porty-adaptery/)** | Legacy za siatką → porty i adaptery | 55 | tak |
| **[Z10](zadania/Z10-docker/)** | Docker — warstwy, rozmiar, healthcheck | 50 | tak |
| **[Z11](zadania/Z11-final-template/)** | Finał: gotowy template, pełny cykl | 50 | tak |
| **[Z12](zadania/Z12-llm-w-aplikacji/)** | LLM w środku aplikacji — port, narzędzia, hook | 45 | — |

### Bonusy

Niezależne, do wstawienia gdziekolwiek albo do zabrania do domu.

| | Temat | Czas |
|---|---|---|
| **[B1](bonusy/B1-petla-naprawcza/)** | Pętla naprawcza i warunki stopu | 40 |
| **[B2](bonusy/B2-ktory-model/)** | Który model do czego | 35 |
| **[B3](bonusy/B3-mcp/)** | MCP: agent sięga po schemat zamiast zgadywać | 40 |
| **[B4](bonusy/B4-subagenty/)** | Subagenty i równoległość | 30 |

Każde zadanie ma na końcu tabelę **★** — rozszerzeń do dołożenia, gdy skończysz
wcześniej. Jest ich znacznie więcej niż czasu i to jest celowe: reszta zostaje
jako materiał do zabrania.

---

## Co jest w tym repo

```
zadania/            dwanaście zadań: opis, prompty, rozwiązania
bonusy/             cztery zagadnienia poza głównym torem
harness-template/   działający system z kompletnym harnessem — prezent na koniec
archetype/          archetyp Mavena z Z02
katy/               kata legacy z Z09, czysty javac, bez Mavena
repo/petclinic/     przypięcie i nakładka na spring-petclinic
docs/skille.md      czym jest skill i dlaczego się o nich mówi
docs/L5.md          rama Intent → Contract → … → Ship, zmapowana na zadania
docs/setup/         bootstrap, settings.xml, hook
tools/              repo_policy.py, bench.py, sprawdz_odnosniki.py
przygotuj           stawia piaskownicę pod zadanie
sprawdz             sprawdza, czy zadanie zrobione
```

Dwa repozytoria robocze:

**[spring-petclinic](https://github.com/spring-projects/spring-petclinic)** —
prawdziwe, cudze repo. 132 pliki, znajoma domena, Spring Boot 4.1. Używamy go
w siedmiu zadaniach, więc poznajesz je raz, a technik uczysz się wielu.

**[harness-template](harness-template/)** — nasz system z bramką, kontraktem,
generowanymi testami i CI. Dostajesz go na własność.

---

## Rozwiązania

Każde zadanie ma katalog `rozwiazanie/`. **Wszystkie są sprawdzone** — kod się
buduje, testy przechodzą, liczby w opisach są zmierzone, nie oszacowane.

Zaglądaj **po** próbie, nie przed. Twój wynik będzie inny i to jest w porządku
— porównuj decyzje, nie pliki.

---

## Gdy coś nie działa

```bash
./sprawdz env
```

Najczęstsze rzeczy:

| Objaw | Przyczyna |
|---|---|
| Maven ściąga z firmowego Nexusa i nie znajduje paczek | nie załadowałeś `env.sh` — ustawia `MAVEN_ARGS`, dzięki czemu zwykłe `mvn` omija mirror |
| Testcontainers nie widzi Dockera | `env.sh` wykrywa `DOCKER_HOST` sam; upewnij się, że go załadowałeś |
| `bash: ./sprawdz: /usr/bin/env: bad interpreter` | plik ma CRLF — `git config core.autocrlf` i przeczytaj `.gitattributes` |
| pierwszy build petclinica trwa 6 minut | tak ma być, ściąga 151 MB; kolejny to 84 s |
| `WARNING: sun.misc.Unsafe::staticFieldBase has been called...` | **normalne.** Maven 3.9 na JDK 25 tak gada przy każdym uruchomieniu. Nic nie jest zepsute — w PowerShellu wychodzi na czerwono i wygląda groźniej, niż jest |
| `mkdocs 2.0 is coming` przy budowaniu dokumentacji | też normalne, to zapowiedź nowej wersji; my jesteśmy przypięci na 1.6 |

Jeśli coś dalej nie działa — napisz do prowadzącego **przed zajęciami**,
nie w dniu zajęć.
