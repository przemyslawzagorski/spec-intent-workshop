# Z01 · Ustaw sobie warsztat

**30 min** · petclinic · bramka: `./sprawdz Z01`

## O co chodzi

Zanim zaczniesz pracować z agentem na jakimkolwiek repo, musisz mu powiedzieć,
jak w tym repo się pracuje. Zrobisz to raz, plikiem, albo będziesz to powtarzał
w każdej rozmowie do końca projektu.

W tym zadaniu ustawiasz trzy rzeczy: plik z regułami, skille i hook, który
blokuje. Na prawdziwym repo, nie na przykładzie.

## Jak zwykle to robimy

Otwierasz nową sesję i piszesz: *„pamiętaj, używamy Javy 17, testy w JUnit 5,
nie dotykaj pom.xml, format sprawdza spring-javaformat"*. Za trzy dni piszesz
to samo, tylko krócej, bo się już nudzisz. Za tydzień zapominasz o formacie
i dostajesz czerwone CI.

Boli, bo:

- **Powtarzasz się.** Te same zdania, kilka razy dziennie, w każdej sesji.
- **Nikt inny tego nie ma.** Kolega ma swoją wersję tych ustaleń w głowie.
- **Ustalenia się gubią.** Po kompaktowaniu kontekstu albo po `/clear`.
- **Agent i tak czasem zignoruje.** Bo to była prośba w prozie, jedna z wielu.

## Jakie są opcje

**Wklejać za każdym razem.** Działa. Kosztuje twój czas i tokeny, i nie
przenosi się na resztę zespołu. Do jednorazowego skryptu — w porządku.

**Plik z regułami** (`AGENTS.md`, `CLAUDE.md`, `.cursorrules`,
`copilot-instructions.md`). Jedno miejsce, w repo, w code review, wspólne dla
zespołu. Wada: siedzi w każdym promptcie, więc każda linia kosztuje przy każdym
zapytaniu. Druga wada: to nadal **prośba**. Agent zwykle posłucha. Zwykle.

**Hook, który blokuje.** Nie prosi — odmawia. Wada: sprawdza tylko to, co da się
sprawdzić maszynowo, i tylko na commicie.

Nie wybierasz jednego. Reguły do pliku, rzeczy krytyczne do hooka.

## Jak zrobić dobrze

**Plik z regułami trzymaj mały i stały.** Do 2 KB. Nie dlatego, że to magiczna
liczba, tylko dlatego, że plik, który rośnie, przestaje być czytany — przez
agenta też. Jeśli reguła nie zmienia zachowania, wyrzuć ją.

**Wskazuj kod zamiast go opisywać.** Zamiast trzydziestu linii o tym, jak
wyglądają nasze kontrolery, jedna linia: *„kontroler: patrz `OwnerController.java`"*.
Agent przeczyta plik, gdy będzie potrzebował. Ty nie płacisz za te trzydzieści
linii w każdym zapytaniu. To jest **progressive disclosure** — wskaźnik zamiast
treści.

**Rzeczy krytyczne dawaj do hooka.** „Nie commituj kluczy API" w pliku reguł to
prośba. W `pre-commit` to blokada.

## Zrób to

```bash
./przygotuj Z01
cd praca/Z01/spring-petclinic
```

**Co zobaczysz:** kilka szarych linii z komendami, które skrypt wykonał,
i na końcu `Gotowe. Pracuj w praca/Z01/`. Trwa to 5 sekund.
Jeśli chcesz wiedzieć, co dokładnie robi — `./przygotuj Z01 --pokaz`.

**1 · Rozejrzyj się** (3 min). Zobacz `pom.xml`, jeden kontroler, jeden test.
Nie czytaj wszystkiego — potrzebujesz tylko tyle, żeby napisać sensowne reguły.

**2 · Napisz `AGENTS.md`.** Możesz poprosić agenta, ale przeczytaj, co napisał.
Prompt masz w [prompty/reguly.md](prompty/reguly.md).

Jeśli zainstalowałeś skille (krok 3 — możesz go zrobić najpierw), to jest
dokładnie zadanie dla **`writing-for-agents`**. Jego opis brzmi:
*„Use when creating or editing skills, or modifying AGENTS.md or CLAUDE.md"*.

Wymagania:

- mieści się w 2 KB,
- wskazuje **konkretne pliki** jako wzorce,
- ma sekcję „jak uruchamiać" z komendami, które naprawdę działają,
- ma sekcję „czego nie zakładać".

**3 · Zainstaluj skille** (3 min).

> **Zrób to w korzeniu warsztatu, nie w piaskownicy.** Instalator zapisuje
> skille do `.agents/skills/` **bieżącego katalogu**. Jeśli uruchomisz go
> w `praca/Z01/spring-petclinic`, wylądują w katalogu, który skasujesz przy
> `./przygotuj Z01 --od-nowa`, i nie będzie ich w pozostałych zadaniach.

```bash
cd ../../..     # do korzenia warsztatu
npx skills@latest add mattpocock/skills -s grilling -s domain-modeling -s codebase-design -s tdd -s writing-for-agents
cd praca/Z01/spring-petclinic
```

**Co zobaczysz:** listę zainstalowanych skilli i przy każdym, na jakie narzędzia
poszły — *„universal: GitHub Copilot, Amp, Cline…"*, a dla Claude Code
*„symlinked"*. Powstaną dwa katalogi: `.agents/skills/` z plikami
i `.claude/skills/` z dowiązaniami.

Na końcu instalator napisze: *„Review skills before use; they run with full
agent permissions."* **Potraktuj to poważnie** — to jest tekst, który wleci
do twoich promptów.

**Otwórz któryś `SKILL.md`.** To zwykły markdown: nagłówek z nazwą i opisem,
pod nim instrukcja. Nic więcej. Żadnego kodu, żadnego API.

Dwie rzeczy, o które ludzie się potykają:

- forma `-s a,b,c` po przecinku **nie działa** — każdy skill ma własną flagę `-s`,
- **nie instaluj jednocześnie wtyczką i przez `npx`** (`claude plugins install
  mattpocock-skills` to ta druga droga) — dostaniesz każdy skill podwójnie.

**Jeśli twoje narzędzie nie ma skilli — nic nie szkodzi.** Otwierasz `SKILL.md`
i wklejasz treść jako prompt. Efekt jest ten sam.

Więcej — w tym które skille działają od ręki, a które najpierw chcą konfiguracji
repo: [docs/skille.md](../../docs/skille.md).

**4 · Wstaw hook.** Napisz sam albo weź gotowy:

```bash
cp ../../../zadania/Z01-init-workspace/rozwiazanie/pre-commit .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

**5 · Sprawdź, że hook naprawdę blokuje:**

```bash
echo 'AWS_SECRET=AKIAIOSFODNN7EXAMPLE' > test.txt
git add test.txt && git commit -m "proba"
```

**Co zobaczysz:** wypisany wzorzec, który się zapalił, i komunikat
`Commit zatrzymany: w zmianach jest cos, co wyglada na sekret`, a pod nim
trzy kroki, co z tym zrobić.

**Commit ma NIE przejść.** Jeśli przeszedł — hook jest atrapą, wróć do kroku 4.

Sprawdź też **drugi rodzaj sekretu** — tu najłatwiej o cichy błąd:

```bash
echo "-----BEGIN RSA PRIVATE KEY-----" > test2.txt
git add test2.txt && git commit -m "proba2"
```

Ten też ma zostać zablokowany. Jeśli przeszedł, a poprzedni nie — masz
w hooku klasyczną pułapkę i omówimy ją przy podsumowaniu.

```bash
git reset test.txt test2.txt && rm -f test.txt test2.txt
cd ../../.. && ./sprawdz Z01
```

**Co zobaczysz:** sześć linii, każda zaczyna się od `jest` albo `BRAK`,
a na końcu `Gotowe.` Linia `uwaga sa zainstalowane skille` jest w porządku,
jeśli nie instalowałeś skilli — to uwaga, nie błąd.

## Pytanie na czat

**Wklejcie rozmiar swojego `AGENTS.md` w bajtach** (`wc -c AGENTS.md`)
**i jedną regułę, którą wyrzuciliście jako niepotrzebną.**

## Omówienie

Poproszę dwie osoby o pokazanie ekranu — najmniejszy i największy plik.
Porównamy, co w nich jest.

Rzeczy, o których pogadamy:

- **Reguły, które nic nie robią.** „Pisz czysty kod", „stosuj dobre praktyki",
  „bądź dokładny". Kosztują tokeny przy każdym zapytaniu i nie zmieniają nic.
  Jak sprawdzić, czy reguła działa: usuń ją i zobacz, czy coś się zmieniło.
- **Reguła kontra hook.** Która z waszych reguł powinna być hookiem?
- **Format hooka.** Mój blokuje sekrety. Można też blokować commit z czerwonymi
  testami — ale wtedy commit trwa 84 sekundy i po tygodniu wszyscy dopiszą
  `--no-verify` do aliasu. **Hook, który za dużo blokuje, zostaje wyłączony.**
- **Przenośność.** `AGENTS.md` czyta Claude Code i Copilot. Cursor woli
  `.cursorrules`. Najprościej: jeden plik z treścią, reszta to symlink albo
  jednolinijkowy plik z odwołaniem.

## Kiedy to NIE ma sensu

Skrypt na 50 linii. Spike, który jutro wyrzucisz. Repo, do którego zaglądasz raz
na kwartał. Ustawianie reguł kosztuje pół godziny i zwraca się dopiero przy
dziesiątej sesji — poniżej tego progu po prostu wklej, co trzeba, do promptu.

## ★ Jeśli skończyłeś wcześniej

| ★ | Co robisz | Min |
|---|---|---|
| **Sprawdź, która reguła działa** | Usuwaj z `AGENTS.md` po jednej regule i za każdym razem zadawaj agentowi to samo zadanie. Która zmiana faktycznie zmienia wynik? Większość nie zmieni nic — i to jest wynik wart zapamiętania. | 20 |
| **Reguły per katalog** | `.github/instructions/*.instructions.md` z polem `applyTo`. Inne reguły dla `src/main`, inne dla `src/test`. | 15 |
| **Napisz własny skill** | Weź czynność, którą powtarzasz w swoim projekcie, i zapisz jako `SKILL.md`. Uruchom. Poznasz format od środka. | 25 |
| **Porównaj formaty** | `AGENTS.md`, `.cursorrules`, `copilot-instructions.md` — co przetrwa zmianę narzędzia, a co trzeba pisać od nowa. | 15 |
| **Twardszy hook** | Dołóż blokadę commita, który zmienia plik testowy i plik produkcyjny naraz. Zdecyduj świadomie, czy to ma być HARD czy SOFT. | 20 |

## Rozwiązanie

[rozwiazanie/AGENTS.md](rozwiazanie/AGENTS.md) · [rozwiazanie/pre-commit](rozwiazanie/pre-commit)

Nie zaglądaj przed próbą. Zaglądaj po — żeby porównać decyzje, nie żeby skopiować.
