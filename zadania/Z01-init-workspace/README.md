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

## Dwa różne hooki — i to nie jest to samo

Zanim zaczniesz: w świecie agentów słowo „hook" znaczy **dwie różne rzeczy**
i mylenie ich kosztuje.

| | **Hook gita** | **Hook agenta** |
|---|---|---|
| Gdzie mieszka | `.git/hooks/` w jednym repo | konfiguracja narzędzia (`settings.json`) |
| Kiedy się odpala | przy operacji gita — commit, push | przy **wywołaniu narzędzia** przez agenta |
| Co widzi | to, co idzie do commita | komendę, plik, argumenty — **zanim się wykonają** |
| Kogo obowiązuje | **każdego**, kto commituje w tym repo | tylko tego agenta |
| Jak łatwo obejść | `git commit --no-verify` | trudniej — agent nie kontroluje swojej konfiguracji |

**W tym zadaniu budujesz hook gita.** To jest właściwy wybór na start, bo:

- **działa na wszystkich naraz** — na tobie, na koledze i na agencie.
  Kiedy agent uruchomi `git commit`, dostanie dokładnie ten sam komunikat co ty;
- **jest przenośny** — nie zależy od tego, czy zespół używa Claude Code,
  Copilota czy niczego;
- **jedno miejsce, jedna reguła.**

**Ale ma dwie dziury, o których trzeba wiedzieć:**

**1 · Odpala się za późno.** Sekret jest już na dysku, już w katalogu roboczym,
a często już w poprzednim commicie. Hook gita zatrzymuje **publikację**,
nie **powstanie**.

**2 · `--no-verify` go wyłącza.** Jedna flaga. Agent, który utknie w pętli
naprawczej i zobaczy blokadę, ma spore szanse jej użyć — bo formalnie spełnia
polecenie „zrób commit".

**Tu wchodzą hooki agenta.** Odpalają się na **wywołaniu narzędzia**, czyli
zanim komenda w ogóle pójdzie do powłoki. W Claude Code konfiguruje się je
w `settings.json`, zdarzenie `PreToolUse`. Przykład, który domyka dziurę numer 2:

```json
{
  "hooks": {
    "PreToolUse": [{
      "matcher": "Bash",
      "hooks": [{
        "type": "command",
        "command": "grep -q -- '--no-verify' <<< \"$CLAUDE_TOOL_INPUT\" && { echo 'Nie obchodzimy hookow gita.' >&2; exit 2; } || exit 0"
      }]
    }]
  }
}
```

Kod wyjścia `2` blokuje wywołanie, a tekst ze `stderr` wraca do modelu jako
wyjaśnienie. Inne narzędzia mają własne mechanizmy — Copilot i Augment
konfiguruje się inaczej, ale **pomysł jest ten sam**.

**Wniosek, który warto zapamiętać:**

> Hook gita pilnuje **repozytorium**. Hook agenta pilnuje **agenta**.
> Potrzebujesz obu, jeśli agent commituje w twoim imieniu — a commituje.

Do tego jest trzeci poziom, który zobaczysz w Z07: **bramka**, czyli skrypt
uruchamiany świadomie przed commitem i w CI. Hook jest odruchem, bramka jest decyzją.

## Zrób to

> **Jedna rzecz, przez którą najwięcej osób się potyka:** w tym zadaniu
> pracujesz **w klonie petclinica**, nie w repo warsztatu. Instalacja skilli
> to jedyny wyjątek — dlatego robimy ją najpierw, zanim w ogóle wejdziesz
> do piaskownicy.
>
> Jeśli w którymś momencie zgubisz się, gdzie jesteś — `pwd`.

### Krok 1 · Zainstaluj skille (3 min) — **w korzeniu warsztatu**

Instalator zapisuje skille do `.agents/skills/` **bieżącego katalogu**.
Uruchomiony w piaskownicy zostawi je w katalogu, który zaraz skasujesz.

```bash
npx skills@latest add mattpocock/skills -s grilling -s domain-modeling -s codebase-design -s tdd -s writing-for-agents
```

**Co zobaczysz:** listę skilli i przy każdym, na jakie narzędzia poszły —
*„universal: GitHub Copilot, Amp, Cline…"*, a dla Claude Code *„symlinked"*.
Powstaną dwa katalogi: `.agents/skills/` z plikami i `.claude/skills/`
z dowiązaniami.

Na końcu: *„Review skills before use; they run with full agent permissions."*
**Potraktuj to poważnie** — to tekst, który wleci do twoich promptów.

**Otwórz któryś `SKILL.md`.** Zwykły markdown: nagłówek z nazwą i opisem,
pod nim instrukcja. Nic więcej — żadnego kodu, żadnego API.

Dwie pułapki:

- forma `-s a,b,c` po przecinku **nie działa** — każdy skill ma własną flagę `-s`,
- **nie instaluj jednocześnie wtyczką i przez `npx`** (`claude plugins install
  mattpocock-skills` to ta druga droga) — dostaniesz każdy skill podwójnie.

**Nie masz skilli w swoim narzędziu?** Otwierasz `SKILL.md` i wklejasz treść
jako prompt. Efekt ten sam. Więcej: [docs/skille.md](../../docs/skille.md).

### Krok 2 · Wejdź do piaskownicy i rozejrzyj się (3 min)

```bash
./przygotuj Z01
cd praca/Z01/spring-petclinic
```

**Co zobaczysz:** kilka szarych linii z komendami i `Gotowe. Pracuj w praca/Z01/`.
Trwa 5 sekund. Chcesz wiedzieć dokładnie, co robi — `./przygotuj Z01 --pokaz`.

**Od tego momentu wszystko dzieje się tutaj.** Zobacz `pom.xml`, jeden kontroler,
jeden test. Nie czytaj wszystkiego — tylko tyle, żeby napisać sensowne reguły.

### Krok 3 · Napisz `AGENTS.md` (10 min)

Możesz poprosić agenta, ale **przeczytaj, co napisał**.
Prompt: [prompty/reguly.md](prompty/reguly.md).

Masz skille? To jest dokładnie zadanie dla **`writing-for-agents`** — jego opis
brzmi *„Use when creating or editing skills, or modifying AGENTS.md or CLAUDE.md"*.

Wymagania:

- mieści się w 2 KB,
- wskazuje **konkretne pliki** jako wzorce,
- ma sekcję „jak uruchamiać" z komendami, które naprawdę działają,
- ma sekcję „czego nie zakładać".

### Krok 4 · Wstaw hook (5 min)

**Uwaga: hook trafia do klona petclinica, nie do repo warsztatu.** Każde repo
git ma własne `.git/hooks/` i nie dziedziczą po sobie.

Sprawdź, gdzie jesteś, zanim skopiujesz:

```bash
pwd          # ma się kończyć na praca/Z01/spring-petclinic
cp ../../../zadania/Z01-init-workspace/rozwiazanie/pre-commit .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

### Krok 5 · Sprawdź, że hook naprawdę blokuje (5 min)

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

**Co zobaczysz:** siedem linii, każda zaczyna się od `jest` albo `BRAK`,
a na końcu `Gotowe.` Linia `uwaga sa zainstalowane skille` jest w porządku,
jeśli nie instalowałeś skilli — to uwaga, nie błąd.

Jeśli dostaniesz `BRAK hook jest wykonywalny`, bramka powie ci, **gdzie szukała
i gdzie znalazła** twój hook. Najczęściej wylądował w korzeniu warsztatu.

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
- **Reguła kontra hook kontra hook agenta.** Która z waszych reguł powinna być
  hookiem gita, a która hookiem agenta? Podpowiedź: wszystko, co dotyczy
  **treści commita**, to git. Wszystko, co dotyczy **komend, które agent
  uruchamia**, to hook agenta — bo do gita nigdy nie dotrze.
- **Że agent też trafia na ten hook.** Kiedy uruchomi `git commit`, dostanie
  dokładnie ten sam komunikat co wy. To jest zaleta, nie efekt uboczny —
  jedna reguła, wszyscy jej podlegają. Ale ma flagę `--no-verify` i po paru
  nieudanych próbach potrafi po nią sięgnąć.
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
| **Hook agenta, nie gita** | Skonfiguruj w swoim narzędziu hook na wywołanie narzędzia (w Claude Code: `PreToolUse` w `settings.json`) i zablokuj `git commit --no-verify`. Potem **spróbuj namówić agenta, żeby to obszedł** — i zobacz, co zrobi. | 25 |
| **Porównaj zasięg** | Ten sam zakaz raz jako hook gita, raz jako hook agenta. Który złapie `rm -rf`? Który złapie zapis sekretu do pliku, którego nigdy nie zacommitujesz? | 20 |

## Rozwiązanie

[rozwiazanie/AGENTS.md](rozwiazanie/AGENTS.md) · [rozwiazanie/pre-commit](rozwiazanie/pre-commit)

Nie zaglądaj przed próbą. Zaglądaj po — żeby porównać decyzje, nie żeby skopiować.
