# Z10 · Docker i uruchomienie

**50 min** · petclinic · bramka: `./sprawdz Z10`

> **Uwaga na czas:** same buildy Dockera to około **8 minut czystego czekania**.
> Nie da się tego skrócić — planuj lekturę na czas builda.

## O co chodzi

**To nie jest zadanie o Dockerze. To zadanie o tym, jak prosić agenta o rzecz,
przy której „działa" jest bardzo niskim progiem.**

Agent napisze ci Dockerfile w dwie minuty. Będzie działał. Będzie też ważył
715 MB i przebudowywał się dwie minuty po zmianie jednej linii kodu. Nikt cię
o tym nie poinformuje, bo spełnił, o co prosiłeś.

Docker jest tu tylko dobrym poligonem, bo różnica między „działa" a „działa
dobrze" jest **mierzalna w sekundach i megabajtach**. Ta sama technika działa
przy zapytaniu SQL, przy konfiguracji CI i przy każdej rzeczy, którą przyjmujesz
na słowo, bo nie chce ci się sprawdzać.

## Jak zwykle to robimy

```dockerfile
FROM eclipse-temurin:25-jdk
COPY . .
RUN ./mvnw package -DskipTests
CMD ["java", "-jar", "target/app.jar"]
```

Cztery linie, działa, idzie na produkcję.

Boli, bo:

- **Każda zmiana kodu unieważnia wszystko.** `COPY . .` jest przed `RUN mvnw`,
  więc zmiana jednego znaku w komentarzu każe pobrać 151 MB zależności od nowa.
- **W obrazie zostaje wszystko.** JDK, Maven, źródła, cały `~/.m2`, katalog
  `target` z klasami. Do uruchomienia potrzebny jest jeden plik jar i JRE.
- **Działa jako root.** Bo nikt nie powiedział, żeby nie.
- **Nikt nie wie, czy wstało.** Kontener „działa" od chwili startu procesu.
  Aplikacja Springowa potrzebuje kilkunastu sekund. W tym oknie ruch leci
  do czegoś, co jeszcze nie odpowiada.

## Jakie są opcje

**Jeden etap, obraz z JDK.** Najprostsze, najwolniejsze, największe.
Do lokalnego eksperymentu w porządku.

**Build wieloetapowy.** Budujesz w obrazie z JDK i Mavenem, kopiujesz jar
do obrazu z samym JRE. Kilka linii więcej, dużo mniejszy obraz.

**Build wieloetapowy z rozdzieloną warstwą zależności.** Do tego kopiujesz
`pom.xml` przed źródłami. To jedna linia różnicy i **największy pojedynczy zysk
w całym zadaniu**.

**Obraz warstwowy Spring Boota** (`layertools`) albo obraz natywny GraalVM.
Jeszcze mniejsze i szybciej wstają. Wychodzi poza to, co zdążymy dziś.

## Jak zrobić dobrze — cztery techniki, które przenoszą się wszędzie

**1 · Nie proś o artefakt. Podaj kryteria.**

„Napisz Dockerfile" to prośba o cokolwiek, co się zbuduje. Agent nie wie, że
zależy ci na czasie przebudowy — bo mu tego nie powiedziałeś. Wymień wymagania:
build wieloetapowy, warstwa zależności osobno, nie jako root, healthcheck.
**Cztery zdania zamiast jednego, i dostajesz inny rezultat.**

**2 · Każ mu zmierzyć, nie ocenić.**

Nie „czy to jest wydajne", tylko: *„zbuduj, podaj rozmiar obrazu; zmień jedną
linię w kodzie, przebuduj, podaj czas"*. Liczba jest sprawdzalna, opinia nie.
Przy okazji **agent sam zobaczy, że pierwsza wersja była zła**, i poprawi ją
bez twojego udziału.

**3 · Każ mu sprawdzić założenia o środowisku, zanim na nich zbuduje.**

To jest technika, która ratuje najwięcej czasu i najrzadziej się o niej pamięta.
Zamiast pozwolić mu założyć, że w obrazie bazowym jest `curl`:

> *Zanim napiszesz HEALTHCHECK, sprawdź, czy w obrazie bazowym jest czym odpytać.
> Nie zakładaj — uruchom obraz i zobacz.*

**Ja tego nie zrobiłem i mnie to kosztowało.** Szczegóły w omówieniu.

**4 · Żądaj dowodu wykonania, nie deklaracji.**

*„Pokaż mi, że `docker inspect` mówi `healthy`"* zamiast „dodaj healthcheck".
Różnica jest taka, że w pierwszym przypadku agent musi to uruchomić — a wtedy
sam znajdzie to, co nie działa.

---

### Krótko o samym Dockerze — tyle, ile potrzeba

**Kolejność `COPY` to kolejność zmienności.** Co zmienia się najrzadziej,
kopiuj najwcześniej:

```dockerfile
COPY pom.xml .
RUN mvn -B -q dependency:go-offline     # ta warstwa przeżywa zmiany kodu
COPY src ./src
RUN mvn -B -q package -DskipTests
```

Do obrazu końcowego wpuszczaj **tylko to, co potrzebne w czasie działania** —
JRE, nie JDK; jar, nie źródła. **Nie uruchamiaj jako root.** I daj `HEALTHCHECK`
z `start-period` dobranym do czasu startu aplikacji.

To wszystko, co musisz wiedzieć o Dockerze na potrzeby tego zadania. **Reszta
to techniki proszenia agenta.**

## Zrób to

```bash
./przygotuj Z10
cd praca/Z10/spring-petclinic
```

!!! Warunek wstępny, nie porada
    **To zadanie mieści się w 50 minutach tylko z pobranymi obrazami bazowymi.**
    Same buildy to u mnie około sześciu minut czystego czekania, a bez cache'u
    dochodzi ~500 MB obrazów i ~151 MB zależności Mavena ściąganych **wewnątrz
    kontenera**. Na łączu domowym, w grupie, to potrafi zjeść cały blok.

    Zrób to **przed** zadaniem — jest w pre-worku, ale warto sprawdzić:

    ```bash
    docker pull maven:3.9-eclipse-temurin-25
    docker pull eclipse-temurin:25-jre
    ```

    Jeśli tego nie masz, a czas ucieka: **zrób sam wariant wieloetapowy**
    i porównaj z moimi liczbami z omówienia. Stracisz własny pomiar naiwnego
    builda, ale nie stracisz pointy.

**1 · Poproś agenta o Dockerfile** (2 min). Bez podpowiedzi:
[prompty/dockerfile.md](prompty/dockerfile.md), wariant A.
Zbuduj i zmierz:

```bash
docker build -f Dockerfile.naiwny -t warsztat-z10-naiwny .
docker images warsztat-z10-naiwny
```

**Co zobaczysz:** numerowane kroki builda, a na końcu tabelę z rozmiarem.
Powinno wyjść **około 715 MB**. Pierwszy build trwa **2–3 minuty**, bo Maven
ściąga zależności wewnątrz kontenera.

**2 · Zmierz to, co boli naprawdę** (10 min). Zmień jedną linię w dowolnym
pliku `.java` i przebuduj:

```bash
echo "// zmiana" >> src/main/java/org/springframework/samples/petclinic/vet/Vet.java
time docker build -f Dockerfile.naiwny -t warsztat-z10-naiwny .
```

**Co zobaczysz:** build leci **od nowa, całe 140 sekund** — mimo że zmieniłeś
jeden komentarz. To jest ten koszt, o który chodzi w tym zadaniu.
Zwróć uwagę, że **żaden krok nie mówi `CACHED`**.

**3 · Teraz wersja wieloetapowa** (10 min). Prompt: wariant B z tego samego
pliku. Zbuduj, zmierz rozmiar, zmień linię kodu i przebuduj ponownie.

**4 · Uruchom i sprawdź healthcheck** (8 min):

```bash
docker build -t warsztat-z10 .
docker run -d --name z10 -p 8080:8080 warsztat-z10
```

**Nie sprawdzaj stanu od razu.** Zaraz po `run` zobaczysz `starting` i to jest
poprawne — Docker czeka na `start-period`. Poczekaj na wynik, zamiast go
odczytywać raz:

```bash
for i in $(seq 1 30); do
  st=$(docker inspect --format '{{.State.Health.Status}}' z10)
  echo "$i: $st"
  [ "$st" = healthy ] || [ "$st" = unhealthy ] && break
  sleep 2
done
```

**Co zobaczysz:** kilkanaście linii `starting`, potem `healthy`. U mnie zajęło
to **około 15 sekund**.

To nie jest sztuczka na potrzeby ćwiczenia. **`healthy` to stan, na który się
czeka** — dokładnie tak samo robi `depends_on: condition: service_healthy`
w compose i każdy sensowny deploy. Jednorazowy odczyt mówi ci tylko, że
kontener istnieje.

Jeśli utknie na `unhealthy` — aplikacja może działać poprawnie, a zepsuty
być sam healthcheck. Powód jest w jego logu, nie w logu aplikacji:

```bash
docker inspect --format '{{json .State.Health.Log}}' z10
```

```bash
docker rm -f z10
cd ../../.. && ./sprawdz Z10
```

**Co robi ta bramka — bo nie ogląda plików.** Bierze **twój**
`praca/Z10/spring-petclinic/Dockerfile`, **buduje z niego obraz sama**, a potem
sprawdza w gotowym obrazie: czy `USER` to nie root, czy jest `HEALTHCHECK`,
i czy uruchomiony kontener **dochodzi do `healthy`** w ciągu 90 sekund.
Rozmiar poniżej 500 MB jest uwagą, nie warunkiem — próg jest celem, nie granicą
poprawności.

Budowanie zajmuje sekundy, bo cache masz rozgrzany po kroku 3. Jeśli healthcheck
nie przejdzie, bramka pokaże ci jego log — ten sam, którego szukałeś wyżej.

## Pytanie na czat

**Rozmiar waszego obrazu i czas przebudowy po zmianie jednej linii kodu.**
Format: `413MB 17s`.

## Omówienie

Poproszę o ekran kogoś z najmniejszym obrazem i kogoś z najszybszą przebudową.
Nie zawsze to ta sama osoba i to też jest ciekawe.

Moje pomiary, na tym samym petclinicu:

| | naiwny | wieloetapowy |
|---|---:|---:|
| pierwszy build | 183 s | **221 s** |
| **przebudowa po zmianie kodu** | **139 s** | **17 s** |
| rozmiar obrazu | 715 MB | **413 MB** |

> **To są moje liczby, nie oczekiwany wynik.** Czasy budowania zależą od łącza,
> dysku i tego, co masz w cache'u — u kogoś, kto powtórzył ten pomiar,
> wieloetapowy build wyszedł 174 s zamiast 221. **Rozmiary są powtarzalne
> (te same obrazy bazowe), czasy nie.** Porównuj proporcje w swoim przebiegu,
> nie swoje liczby z moimi.
>
> Rozmiary podaję w MB dziesiętnych, bo w takich pokazuje je `docker images`.
> `docker image inspect --format '{{.Size}}'` da ci to samo w bajtach.

Pogadamy o:

- **Że pierwszy build wieloetapowego jest WOLNIEJSZY.** 221 zamiast 183 sekund,
  bo `dependency:go-offline` ściąga więcej, niż faktycznie potrzeba do
  zbudowania. Optymalizujemy nie pierwszy build, tylko **setny**. Jeśli patrzysz
  na jedną liczbę, wybierzesz gorzej.
- **Że osiem razy szybsza przebudowa to nie jest oszczędność na rachunku za CI.**
  To jest **długość twojej pętli zwrotnej**. Dwie minuty czekania wybijają
  z pracy, siedemnaście sekund nie.
- **Czego agent nie zrobi bez proszenia.** W moich próbach: użytkownik inny niż
  root, healthcheck, rozdzielenie warstw. Wszystkie trzy trzeba wymienić wprost.
- **Pułapce, w którą sam wpadłem.** Napisałem healthcheck jako
  `CMD ["java", "-e", "System.exit(0)"]`. Obraz się zbudował, kontener wstał,
  aplikacja odpowiadała poprawnie z zewnątrz — a Docker uparcie meldował
  `unhealthy`. Powód: `java` nie ma flagi `-e`. **Zepsuty healthcheck wygląda
  dokładnie jak zepsuta aplikacja.** Logu healthchecka trzeba było poszukać
  (`docker inspect --format '{{json .State.Health.Log}}'`) — bez tego siedziałbym
  nad tym długo.
- **Że obraz JRE nie ma curla.** Ani wgeta, ani netcata — sprawdzone.
  Healthcheck trzeba czymś wykonać, więc albo dokładasz curla, albo szukasz
  innego sposobu.

## Kiedy to NIE ma sensu

Aplikacja, która nigdy nie pojedzie w kontenerze. Prototyp na jeden dzień.
I sytuacja, w której masz gotowy, sprawdzony obraz bazowy od platformy —
wtedy nie pisz własnego, użyj ich.

## ★ Jeśli skończyłeś wcześniej

| ★ | Co robisz | Min |
|---|---|---|
| **Zejdź niżej z rozmiarem** | `jlink` albo obraz distroless. Ile da się jeszcze urwać z 413 MB i czym za to płacisz? | 20 |
| **Warstwy Spring Boota** | `layertools` rozbija jar na warstwy: zależności, snapshoty, kod. Zmierz przebudowę. | 20 |
| **compose z zależnością od zdrowia** | Postgres plus aplikacja, `depends_on: condition: service_healthy`. Sprawdź, co się dzieje bez tego warunku. | 20 |
| **Ten sam build w CI** | Wepnij build obrazu do workflow z Z07, z cache warstw. Porównaj czas pierwszego i drugiego przebiegu. | 20 |
| **Jeden obraz dla dev i prod** | Co się zmienia między środowiskami, a co zostaje? Wypisz i zdecyduj, co idzie do zmiennych. | 15 |
| **Zepsuj healthcheck celowo** | Ustaw `start-period` na 1 s. Zobacz, jak wygląda kontener, który jest zdrowy, ale Docker o tym nie wie. | 10 |

## Rozwiązanie

[rozwiazanie/Dockerfile](rozwiazanie/Dockerfile) — wieloetapowy, 413 MB,
przebudowa 17 s, healthcheck sprawdzony (`healthy` po około 15 sekundach).

[rozwiazanie/Dockerfile.naiwny](rozwiazanie/Dockerfile.naiwny) — punkt odniesienia.
Też działa. O to chodzi.
