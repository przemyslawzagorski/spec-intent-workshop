# Skille — co to właściwie jest

Krótko, bez hype'u, z przykładem który możesz otworzyć.

## Skill to katalog z jednym plikiem markdown

Naprawdę tyle.

```
grilling/
  SKILL.md
```

A w środku:

```markdown
---
name: grilling
description: Grill the user relentlessly about a plan, decision, or idea.
             Use when the user wants to stress-test their thinking.
---

Interview the user relentlessly until you reach a shared understanding.
Map this as a design tree: every decision branches into the decisions
that hang off it.

Work the tree in rounds. The frontier is every decision whose prerequisites
are already settled...
```

Nagłówek YAML z nazwą i opisem. Pod nim zwykły tekst — instrukcja dla modelu.
**Nie ma tu żadnego kodu, żadnego API, żadnego frameworka.**

## Jak to działa

Agent widzi na starcie **tylko nazwy i opisy** wszystkich zainstalowanych skilli.
To kosztuje kilkadziesiąt tokenów na skill. Treść `SKILL.md` ładuje się dopiero
wtedy, gdy agent uzna, że ten skill pasuje do tego, o co prosisz — albo gdy
poprosisz o niego wprost.

To się nazywa **progressive disclosure**: wskaźnik zamiast treści, treść na żądanie.
Ta sama zasada, którą stosujemy w `AGENTS.md` (Z01) i przy budowaniu kontekstu (Z04).

Konsekwencja praktyczna: **możesz mieć zainstalowanych trzydzieści skilli i nie
płacić za nie przy każdym zapytaniu.** Płacisz za nagłówki.

## Dlaczego zrobiło się o nich głośno

Cztery powody, w kolejności ważności:

**1 · Bo powtarzalna czynność przestaje być powtarzanym promptem.**
Jeśli za każdym razem piszesz agentowi „przepytaj mnie, nie pomagaj, rundami,
maksymalnie trzy" — to jest skill. Piszesz raz, wołasz nazwą.

**2 · Bo to jest tekst, więc działa wszędzie.**
`SKILL.md` to markdown. Instalator wprost wypisuje, na jakie narzędzia założył
skill: *„universal: GitHub Copilot, Amp, Antigravity, Cline +12 more"*.
A jeśli twojego nie ma na liście — otwierasz plik i wklejasz treść jako prompt.
**Efekt jest ten sam.** Stąd to szkolenie może być agnostyczne.

**3 · Bo da się je wersjonować i dzielić.**
Skill leży w repo, w code review, w git logu. Zespół ma jedną wersję
„jak u nas robimy przegląd kodu", a nie osiem wersji w ośmiu głowach.

**4 · Bo dobry skill zawiera wiedzę, której sam byś nie napisał w prompcie.**
Zobacz `grilling`: pojęcie *frontier* — pytania, na które da się odpowiedzieć
teraz, bo ich przesłanki są już rozstrzygnięte. Nikt tego nie wpisze do promptu
z głowy. A to jest różnica między „zadaj mi pytania" a przesłuchaniem, które
faktycznie coś wyciąga.

## Czego skille NIE robią

**Nie dają modelowi nowych możliwości.** To jest instrukcja, nie wtyczka.
Skill nie sięgnie do bazy ani nie wywoła twojego API — od tego są narzędzia
i MCP (bonus B3).

**Nie gwarantują wykonania.** Skill to prośba, jak wszystko w prompcie.
Jeśli coś ma być pewne — potrzebujesz bramki (Z07) albo hooka (Z01).

**Nie zastępują specyfikacji.** Skill mówi *jak* pracować. Co ma powstać,
nadal musisz rozstrzygnąć (Z05).

**Nie wszystkie są warte instalacji.** Skill, którego użyjesz raz, jest droższy
niż wklejony prompt.

---

## Instalacja — trzy rzeczy, o które ludzie się potykają

```bash
npx skills@latest add mattpocock/skills -s grilling -s domain-modeling \
  -s codebase-design -s tdd -s writing-for-agents
```

**1 · Instaluje do katalogu, w którym stoisz.** Pliki lądują w `.agents/skills/`,
a dla Claude Code powstają dowiązania w `.claude/skills/`. Jeśli uruchomisz to
w piaskownicy zadania — tam zostaną i znikną przy `./przygotuj … --od-nowa`.
**Rób to raz, w korzeniu warsztatu.**

**2 · Nie mieszaj dwóch dróg instalacji.** Jest wtyczka do Claude Code
(`claude plugins install mattpocock-skills`, zarządzana, aktualizuje się sama)
i jest `npx skills` (kopiuje pliki, które są twoje i możesz je edytować).
Autor ostrzega wprost: *„installing both leaves you with every skill twice"*.

**3 · Przecinki nie działają.** `-s a,b,c` przechodzi bez błędu i nie instaluje
nic sensownego. Każdy skill potrzebuje własnej flagi `-s`.

Na końcu instalator napisze: *„Review skills before use; they run with full agent
permissions."* **Potraktuj to poważnie.** To jest tekst, który wleci do twoich
promptów — przeczytaj go, zanim zaczniesz mu ufać.

---

## Które działają od ręki, a które chcą najpierw konfiguracji

**To jest rzecz, której nie ma w żadnym opisie, a decyduje o tym, czy skill
ci pomoże, czy zacznie od proszenia o setup.**

Część skilli inżynierskich Pococka zakłada, że repo ma **issue tracker
i słownik etykiet triage** — konfiguruje to jednorazowo
`setup-matt-pocock-skills`. Bez tego zaczną od odesłania cię do konfiguracji.

| Skill | Działa od ręki? | Gdzie u nas |
|---|---|---|
| `grilling` / `grill-me` | **tak** | Z05 — przesłuchanie intencji |
| `domain-modeling` | **tak** | Z03 — słownik domeny z kodu |
| `codebase-design` | **tak** | Z09 — granice modułów, porty |
| `tdd` | **tak** | Z11 — najpierw test |
| `writing-for-agents` | **tak** | **Z01** — pisanie `AGENTS.md` i skilli |
| `diagnosing-bugs` | **tak** | B1 — pętla diagnostyczna |
| `wait-what` | **tak** | Z05 ★ — „przepitchuj to, nie zrozumiałem" |
| `prototype` | **tak** | do rozmowy „kiedy to NIE ma sensu" |
| `to-spec` | **nie** — pisze do issue trackera | — |
| `to-tickets` | **nie** — jw. | Z05 ★, ale najpierw setup |
| `implement` | **nie** — działa „ze spec albo biletów" | — |
| `code-review` | **nie** — zakłada standardy i spec w repo | Z08 — wzięliśmy stamtąd **zasadę dwóch osi**, nie implementację |
| `wayfinder` | **nie** — planowanie na tablicy biletów | poza zakresem warsztatu |

**Dlatego w zadaniach polecamy tylko te z pierwszej grupy.** Reszta jest dobra,
ale w piaskownicy warsztatowej zacznie od pytania o konfigurację zamiast od roboty
— a mamy pięćdziesiąt minut, nie pięć godzin.

---

## Cztery skille napisaliśmy sami

Leżą w rozwiązaniach i są jednocześnie przykładem, jak wygląda własny skill:

| Skill | Gdzie leży | Do czego |
|---|---|---|
| `dokumentacja-repo` | `zadania/Z06-.../rozwiazanie/skill-dokumentacja/` | dokumentacja z kodu, bez zmyślania, wg Diátaxis |
| `krytyk-adwersaryjny` | `zadania/Z08-.../rozwiazanie/skill-krytyk/` | oś „co się zepsuje" — dostaje sam kod, szuka powodów, dla których nie zadziała |
| `roast-obietnicy` | `zadania/Z08-.../rozwiazanie/skill-roast-obietnicy/` | oś „czego tu nie ma" — dostaje kod **i** specyfikację, wypełnia tabelę wymaganie → linia → werdykt |
| `zlote-wzorce` | `zadania/Z09-.../rozwiazanie/skill-zlote-wzorce/` | siatka pod refaktor kodu bez testów |

**Nie musisz mieć żadnego skilla, żeby przejść warsztat.** Przy każdym zadaniu
jest prompt, który robi to samo ręcznie. Skille są wygodą, nie warunkiem.

## Napisz własny — ćwiczenie na dziesięć minut

Weź czynność, którą powtarzasz. Zapisz jako `SKILL.md`:

```markdown
---
name: przeglad-pr
description: Przegląd pull requesta wg naszych zasad. Użyj, gdy ktoś prosi
             o review albo wkleja diff.
---

Jesteś recenzentem. Załóż, że ten kod jest zły.

Sprawdź w kolejności:
1. Czy testy pokrywają przypadki brzegowe...
2. ...

Nie oceniaj stylu — od tego mamy formatter.
Przy każdej uwadze podaj plik i numer linii.
```

Wrzuć do `.claude/skills/przeglad-pr/` (albo tam, gdzie twoje narzędzie ich szuka)
i zawołaj. **Jeśli działa jako wklejony prompt — zadziała jako skill.**

Dwie rzeczy, które robią różnicę między dobrym a nijakim skillem:

- **Opis w nagłówku decyduje, czy agent go w ogóle użyje.** Napisz w nim,
  *kiedy* sięgnąć po ten skill, nie tylko co robi.
- **Treść ma być instrukcją, nie opisem.** „Zadaj wszystkie pytania z frontiera
  w jednej rundzie" działa. „Skill do przesłuchiwania" nie działa.

## Dwa zestawy warte poznania

**[mattpocock/skills](https://github.com/mattpocock/skills)** — 33 skille
w pięciu kategoriach. Mocne w planowaniu, przesłuchiwaniu i pisaniu dla agentów.
Część jest wyraźnie pod TypeScript i Node — na przykład `setup-pre-commit`
konfiguruje Husky, lint-staged i Prettiera, więc na repo w Javie się nie przyda.
**Stąd bierzemy większość tego, co polecamy.**

**[AdamBien/airails](https://github.com/AdamBien/airails)** — zestaw natywnie
javowy, bliższy naszemu stackowi. Warto zajrzeć zwłaszcza tutaj:

| Skill | Odpowiada naszemu |
|---|---|
| `migrations/characterization-tests` | **Z09** — to dokładnie technika złotych wzorców |
| `migrations/bc-carver` | Z09 — wykrawanie kontekstu z monolitu |
| `bce/ears-tests` | Z05 — wymagania w EARS |
| `bce/system-tests`, `bce/continuous-testing` | Z07, Z11 |
| `java/enterprisifier` | Z09 ★ — zdemoluj czysty kod nadmiarem warstw |
| `documentation/readme`, `documentation/mermaid` | Z06 |

**[agentskills.io](https://agentskills.io)** — format i szerszy katalog.

Zanim zainstalujesz cokolwiek — **otwórz `SKILL.md` i przeczytaj.** Minuta pracy,
oszczędza zdziwienie.
