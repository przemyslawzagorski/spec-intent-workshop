#!/usr/bin/env -S uv run --script
# /// script
# requires-python = ">=3.11"
# dependencies = []
# ///
"""
repo-policy - zamienia reguly z pliku regul w skrypt.

Regula, ktora zyje tylko w AGENTS.md, jest sugestia. Agent moze ja przeczytac
i zignorowac - i czasem to robi, zwlaszcza gdy jest pod presja zielonych testow.
Regula, ktora ma tu swoja funkcje, jest regula.

    "Compiler errors are a better feedback loop than a style guide."
        -- Jarred Sumner, o przepisaniu Buna na Rusta

Trzy reguly, celowo niezalezne od projektu. Zadzialaja na petclinicu, na twoim
repo firmowym i na czymkolwiek zbudowanym Mavenem:

  [1] projekt ma niepuste testy         - inaczej zielona bramka nic nie znaczy
  [2] testy nie zmienily sie od odcisku - "nie zmieniaj testu, zeby przeszedl"
  [3] nowa zaleznosc wymaga ADR         - pom.xml kontra docs/adr/

Regula [2] i [3] potrzebuja punktu odniesienia. Zapisujesz go raz, na poczatku:

    uv run tools/repo_policy.py <katalog> --zapisz-odcisk

Odcisk trafia do <katalog>/.odcisk-bramki. Jesli plik juz istnieje, komenda
GO NIE NADPISZE - bez tego caly mechanizm bylby fikcja, bo wystarczyloby
zapisac odcisk jeszcze raz, zeby "zatwierdzic" oslabione testy.

    uv run tools/repo_policy.py <katalog> --nadpisz-odcisk

To jest swiadome przebazowanie. Uzyj TYLKO wtedy, gdy zmienila sie specyfikacja
i testy powstaly od nowa. Nie uzywaj, zeby "naprawic" czerwona bramke.

Uzycie:
    uv run tools/repo_policy.py praca/Z07/spring-petclinic
"""
import hashlib
import pathlib
import re
import sys

ODCISK = ".odcisk-bramki"

MIN_KLAS_TESTOWYCH = 2
MIN_ASERCJI = 5

# Klasy testowe rozpoznajemy po nazwie. Rozne projekty maja rozne konwencje,
# wiec bierzemy trzy najczestsze zamiast zakladac jedna.
WZORCE_TESTOW = ("*Test.java", "*Tests.java", "*IT.java")

WZORZEC_ASERCJI = re.compile(
    r"\bassert[A-Z]\w*\(|\bassertThat\(|\bverify\(|\.andExpect\(|\.body\(|\.statusCode\("
)


def klasy_testowe(projekt: pathlib.Path) -> list[pathlib.Path]:
    testy = projekt / "src" / "test" / "java"
    if not testy.is_dir():
        return []
    znalezione: set[pathlib.Path] = set()
    for wzorzec in WZORCE_TESTOW:
        znalezione.update(testy.rglob(wzorzec))
    return sorted(znalezione)


def odcisk_testow(projekt: pathlib.Path) -> dict[str, str]:
    testy = projekt / "src" / "test" / "java"
    if not testy.is_dir():
        return {}
    return {
        str(f.relative_to(projekt)).replace("\\", "/"):
            hashlib.sha256(f.read_bytes()).hexdigest()[:16]
        for f in sorted(testy.rglob("*.java"))
    }


def zaleznosci(projekt: pathlib.Path) -> set[str]:
    pom = projekt / "pom.xml"
    if not pom.is_file():
        return set()
    return set(re.findall(r"<artifactId>([^<]+)</artifactId>",
                          pom.read_text(encoding="utf-8")))


# --------------------------------------------------------------------- reguly


def regula_1_puste_testy(projekt: pathlib.Path) -> list[str]:
    """Projekt bez testow przechodzi `mvn test` na zielono.

    To jest najgrozniejsza dziura, jaka moze miec bramka uczaca o bramkach:
    widzisz BUILD SUCCESS i wierzysz, ze masz harness, a masz strukture katalogow.
    """
    if not (projekt / "src" / "test" / "java").is_dir():
        return ["[1] brak katalogu src/test/java - projekt nie ma zadnych testow"]

    klasy = klasy_testowe(projekt)
    if len(klasy) < MIN_KLAS_TESTOWYCH:
        return [
            f"[1] znaleziono {len(klasy)} klas testowych, oczekiwano co najmniej "
            f"{MIN_KLAS_TESTOWYCH}.\n"
            f"      Zielony `mvn test` bez testow to BUILD SUCCESS, ktore nic nie znaczy."
        ]

    asercje = sum(len(WZORZEC_ASERCJI.findall(f.read_text(encoding="utf-8")))
                  for f in klasy)
    if asercje < MIN_ASERCJI:
        return [
            f"[1] w klasach testowych jest {asercje} asercji, oczekiwano co najmniej "
            f"{MIN_ASERCJI}.\n"
            f"      Test bez asercji przechodzi zawsze - to dekoracja, nie bramka."
        ]
    return []


def regula_2_zmienione_testy(zapisane: dict[str, str],
                             projekt: pathlib.Path) -> list[str]:
    """Czy pliki testowe zmienily sie od zapisania odcisku."""
    if not zapisane:
        return []
    biezace = odcisk_testow(projekt)
    bledy = []
    for sciezka, suma in sorted(zapisane.items()):
        if sciezka not in biezace:
            bledy.append(
                f"[2] plik testowy USUNIETY po zapisaniu odcisku: {sciezka}"
            )
        elif biezace[sciezka] != suma:
            bledy.append(
                f"[2] plik testowy ZMIENIONY po zapisaniu odcisku: {sciezka}\n"
                f"      Czerwony test to informacja o kodzie, nie problem do usuniecia.\n"
                f"      Jesli test naprawde jest zly - zmien specyfikacje, wygeneruj go\n"
                f"      od nowa i przebazuj odcisk swiadomie (--nadpisz-odcisk)."
            )
    return bledy


def regula_3_zaleznosci(bazowe: set[str], projekt: pathlib.Path) -> list[str]:
    """Kazda zaleznosc dodana po zapisaniu odcisku musi byc wymieniona w ADR."""
    if not bazowe:
        return []
    katalog_adr = projekt / "docs" / "adr"
    adr_tekst = " ".join(
        f.read_text(encoding="utf-8").lower() for f in katalog_adr.glob("*.md")
    ) if katalog_adr.is_dir() else ""

    nowe = zaleznosci(projekt) - bazowe
    return [
        f"[3] zaleznosc '{a}' pojawila sie po zapisaniu odcisku i nie ma dla niej ADR.\n"
        f"      Dodaj docs/adr/NNN-*.md z uzasadnieniem albo usun zaleznosc."
        for a in sorted(nowe)
        if a.lower() not in adr_tekst
    ]


# ----------------------------------------------------------------------- plik


def zapisz_odcisk(projekt: pathlib.Path) -> tuple[int, int]:
    testy = odcisk_testow(projekt)
    deps = zaleznosci(projekt)
    linie = ["# Odcisk bramki. Nie edytuj recznie - to punkt odniesienia,",
             "# wzgledem ktorego repo-policy ocenia zmiany w testach i zaleznosciach.",
             "[testy]"]
    linie += [f"{suma}  {sciezka}" for sciezka, suma in testy.items()]
    linie += ["[zaleznosci]"]
    linie += sorted(deps)
    (projekt / ODCISK).write_text("\n".join(linie) + "\n", encoding="utf-8")
    return len(testy), len(deps)


def wczytaj_odcisk(projekt: pathlib.Path) -> tuple[dict[str, str], set[str]]:
    plik = projekt / ODCISK
    if not plik.is_file():
        return {}, set()
    testy: dict[str, str] = {}
    deps: set[str] = set()
    sekcja = None
    for line in plik.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        if line in ("[testy]", "[zaleznosci]"):
            sekcja = line
            continue
        if sekcja == "[testy]" and "  " in line:
            suma, sciezka = line.split("  ", 1)
            testy[sciezka] = suma
        elif sekcja == "[zaleznosci]":
            deps.add(line)
    return testy, deps


def main() -> int:
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    flagi = {a for a in sys.argv[1:] if a.startswith("--")}
    if len(args) != 1:
        print(__doc__)
        return 2

    projekt = pathlib.Path(args[0]).resolve()
    if not projekt.is_dir():
        print(f"FAIL  nie ma katalogu: {projekt}")
        return 1

    if flagi & {"--zapisz-odcisk", "--nadpisz-odcisk"}:
        plik = projekt / ODCISK
        if plik.is_file() and "--nadpisz-odcisk" not in flagi:
            print(f"OK    odcisk juz istnieje - nie nadpisuje ({ODCISK})")
            print("      Swiadome przebazowanie: --nadpisz-odcisk")
            return 0
        if not odcisk_testow(projekt):
            print("FAIL  nie ma czego odcisnac - projekt nie zawiera plikow testowych")
            return 1
        ile_testow, ile_deps = zapisz_odcisk(projekt)
        print(f"OK    zapisano odcisk: {ile_testow} plikow testowych, "
              f"{ile_deps} zaleznosci")
        return 0

    zapisane_testy, bazowe_deps = wczytaj_odcisk(projekt)
    if not (projekt / ODCISK).is_file():
        print(f"UWAGA nie ma pliku {ODCISK} - reguly [2] i [3] sa wylaczone.")
        print(f"      Zapisz punkt odniesienia: --zapisz-odcisk")

    bledy = (
        regula_1_puste_testy(projekt)
        + regula_2_zmienione_testy(zapisane_testy, projekt)
        + regula_3_zaleznosci(bazowe_deps, projekt)
    )

    if bledy:
        print(f"FAIL  {len(bledy)} naruszen regul:")
        for b in bledy:
            print(f"      {b}")
        return 1

    print("OK    reguly dotrzymane")
    return 0


if __name__ == "__main__":
    sys.exit(main())
