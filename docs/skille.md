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
`SKILL.md` to markdown. Jeśli twoje narzędzie nie zna skilli — otwierasz plik
i wklejasz treść jako prompt. **Efekt jest ten sam.** Stąd to szkolenie może być
agnostyczne: nie uczymy się narzędzia, uczymy się treści.

**3 · Bo da się je wersjonować i dzielić.**
Skill leży w repo, w code review, w git logu. Zespół ma jedną wersję
„jak u nas robimy przegląd kodu", a nie osiem wersji w ośmiu głowach.

**4 · Bo dobry skill zawiera wiedzę, której sam byś nie napisał w prompcie.**
Zobacz `grilling`: pojęcie *frontier* — pytania, na które da się odpowiedzieć
teraz, bo ich przesłanki są już rozstrzygnięte. Nikt tego nie wpisze do promptu
z głowy. A to jest właśnie różnica między „zadaj mi pytania" a przesłuchaniem,
które faktycznie coś wyciąga.

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

## Skille używane na tym warsztacie

Instalacja (wszystkie naraz):

```bash
npx skills add mattpocock/skills -s grilling -s tdd -s domain-modeling \
  -s codebase-design -s implement -s to-spec
```

Uwaga: forma `-s a,b,c` po przecinku **nie działa** — każdy skill potrzebuje
własnej flagi `-s`.

| Skill | Gdzie | Do czego |
|---|---|---|
| `grilling` | **Z05** | przesłuchanie intencji rundami, z rekomendacjami |
| `domain-modeling` | **Z03** | budowa słownika domeny z kodu |
| `codebase-design` | **Z09** | wydzielanie modułów, porty i adaptery |
| `tdd` | **Z11** | najpierw test, potem kod |
| `implement` | **Z11** | realizacja z gotowej specyfikacji |
| `to-spec` | **Z05 ★** | zamiana rozmowy w spec |

**Trzy skille napisaliśmy sami** i leżą w rozwiązaniach — są też przykładem,
jak wygląda własny skill:

| Skill | Gdzie leży | Do czego |
|---|---|---|
| `dokumentacja-repo` | `zadania/Z06-.../rozwiazanie/skill-dokumentacja/` | dokumentacja z kodu, bez zmyślania |
| `krytyk-adwersaryjny` | `zadania/Z08-.../rozwiazanie/skill-krytyk/` | przegląd kodu szukający powodów, dla których nie zadziała |
| `zlote-wzorce` | `zadania/Z09-.../rozwiazanie/skill-zlote-wzorce/` | siatka pod refaktor kodu bez testów |

Do tego dwa cudze, które warto znać, choć nie mają swojego zadania:

| Skill | Do czego |
|---|---|
| `grill-with-docs` | to co `grilling`, ale zostawia po sobie ADR-y i słownik |
| `setup-matt-pocock-skills` | konfiguruje repo pod resztę skilli |

**Nie musisz ich mieć, żeby przejść warsztat.** Przy każdym zadaniu jest prompt,
który robi to samo ręcznie. Skille są wygodą, nie warunkiem.

## Napisz własny — to jest ćwiczenie na dziesięć minut

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

## Gdzie szukać

- [skills.sh/mattpocock/skills](https://skills.sh/mattpocock/skills) — zestaw,
  z którego bierzemy większość
- [agentskills.io](https://agentskills.io) — format i katalog
- [github.com/AdamBien/airails](https://github.com/AdamBien/airails) — skille
  wokół BCE, testów systemowych i EARS

Zanim zainstalujesz cokolwiek — **otwórz `SKILL.md` i przeczytaj.** To jest
tekst, który wleci do twoich promptów. Zajmuje to minutę i oszczędza zdziwienie.
