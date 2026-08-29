# Petclinic na warsztacie

To jest [spring-petclinic](https://github.com/spring-projects/spring-petclinic)
przypiety na jednym commicie (patrz `UPSTREAM` w repo warsztatowym). Nic tu nie
zmienialismy w kodzie - to ma byc prawdziwe, cudze repo, bo o to chodzi w
polowie zadan: wchodzisz w kod, ktorego nie pisales.

## Dlaczego akurat ten projekt

- **132 pliki, 30 klas produkcyjnych, 20 testowych.** Da sie ogarnac w kwadrans.
- **Domena jest oczywista** - wlasciciele, zwierzeta, wizyty, weterynarze.
  Nie tracisz uwagi na zrozumienie biznesu, cala idzie na technike.
- **Kontrolery siegaja wprost do repozytoriow Spring Data.** To nie jest wada
  tego projektu - to normalny Spring. Ale dzieki temu mamy podrecznikowy
  material na wydzielenie portu i adaptera.
- Spring Boot 4.1, wiec swiezy, a nie muzealny.

## Ile to trwa naprawde

Zmierzone na JDK 25, Maven 3.9.11, lacze domowe, `~/.m2` bez ani jednego
artefaktu Springa:

| Co | Czas | Uwagi |
|---|---|---|
| `mvn test` na zimnym `~/.m2` | **6 min 02 s** | ciagnie ~151 MB |
| `mvn test` na rozgrzanym | **84 s** | 76 testow |
| `mvn test -Dtest=OwnerControllerTests` | **23 s** | 15 testow |
| `mvn validate` (sam format) | **6 s** | |

Wniosek na warsztat: **nie kaz agentowi puszczac calego `mvn test` w petli.**
84 sekundy razy dziesiec iteracji to czternascie minut czekania. Jedna klasa
testowa to 23 sekundy. Szybkosc petli zwrotnej jest funkcja tego, co w niej
uruchamiasz.

## Pulapka, o ktora sie potkniesz

Petclinic ma wpiety `spring-javaformat-maven-plugin`. Zle sformatowany kod
**wywala build**, juz w fazie `validate`:

```
[ERROR] Formatting violations found in the following files:
[ERROR]  * .../vet/Vet.java
[ERROR] Run `spring-javaformat:apply` to fix.
```

Sprawdzone: 6 sekund od uruchomienia do bledu. To jest dokladnie taka bramka,
jaka chcemy - twarda, szybka i z komunikatem, ktory mowi, co zrobic. Agent
sam sobie z tym poradzi, jesli mu powiesz, ze ma uruchomic build. Jesli nie
powiesz - zostawi ci czerwone CI.

Naprawa: `mvn spring-javaformat:apply`.

## Komendy

Z katalogu warsztatowego masz `wsmvn` (Maven z naszym `settings.xml`, zeby nie
isc przez firmowego Nexusa, i z osobnym repozytorium lokalnym):

```bash
source ../../.tooling/env.sh   # sciezka zalezy od tego, gdzie jestes
wsmvn test
wsmvn test -Dtest=OwnerControllerTests
wsmvn spring-boot:run          # http://localhost:8080
```

Baza domyslnie H2 w pamieci - nic nie musisz stawiac. Postgres i MySQL sa w
`docker-compose.yml`, jesli beda potrzebne.
