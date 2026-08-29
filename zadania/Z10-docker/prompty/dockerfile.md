# Prompty: Dockerfile w dwóch wariantach

Wklejasz w katalogu `praca/Z10/spring-petclinic`.

Wariant A najpierw, bez zaglądania do B. Chodzi o to, żebyś zobaczył, co
dostajesz, gdy nie powiesz nic poza „zrób mi Dockerfile".

---

## Wariant A — bez podpowiedzi

> Zapisz wynik jako `Dockerfile.naiwny`.

Napisz `Dockerfile` dla tej aplikacji. Ma się budować i uruchamiać.

---

## Wariant B — z wymaganiami

> Zapisz wynik jako `Dockerfile`.

Napisz `Dockerfile` dla tej aplikacji. Wymagania:

1. **Build wieloetapowy.** Kompilacja w obrazie z JDK i Mavenem, uruchomienie
   w obrazie z samym JRE.
2. **Warstwa zależności oddzielona od warstwy kodu.** `pom.xml` kopiujesz
   i pobierasz zależności **przed** skopiowaniem `src`. Zmiana jednej linii kodu
   nie może unieważniać pobranych zależności.
3. **Nie uruchamiaj jako root.** Załóż użytkownika systemowego.
4. **`HEALTHCHECK`**, który naprawdę odpytuje aplikację. Petclinic ma
   `spring-boot-starter-actuator` w zależnościach — sprawdź w `pom.xml`, pod
   jakim adresem jest health. Dobierz `start-period` do czasu startu aplikacji;
   zmierz go, nie zgaduj.
5. `EXPOSE` na porcie, na którym aplikacja naprawdę słucha.

Zanim napiszesz `HEALTHCHECK`, **sprawdź, czy w obrazie bazowym jest czym
odpytać**. Nie zakładaj, że jest `curl` albo `wget` — uruchom obraz i zobacz.
Jeśli nie ma, doinstaluj to, czego potrzebujesz, i posprzątaj po `apt`.

Po napisaniu:

- zbuduj obraz i podaj mi jego rozmiar,
- zmień jedną linię w dowolnym pliku `.java`, przebuduj i podaj czas,
- uruchom kontener i pokaż mi, że `docker inspect` mówi `healthy`,
  a nie `unhealthy`.

**Te trzy rzeczy sprawdź naprawdę, nie napisz, że powinny zadziałać.**
Jeśli healthcheck nie przechodzi, zajrzyj do
`docker inspect --format '{{json .State.Health.Log}}' <kontener>` — tam jest
powód, a nie w logach aplikacji.
