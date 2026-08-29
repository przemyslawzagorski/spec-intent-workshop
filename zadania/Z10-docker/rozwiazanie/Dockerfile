# Build wieloetapowy.
#
# Dwie rzeczy, ktore robia cala roznice:
#   1. pom.xml kopiujemy PRZED zrodlami - warstwa z zaleznosciami przezywa
#      kazda zmiane kodu. Bez tego kazdy build ciagnie 151 MB od nowa.
#   2. do obrazu koncowego trafia JRE i jeden jar, a nie JDK, Maven i zrodla.

FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build

# Warstwa zaleznosci. Zmienia sie tylko wtedy, gdy zmieni sie pom.xml.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Warstwa kodu. Zmienia sie przy kazdym commicie - i tylko ona.
COPY src ./src
RUN mvn -B -q package -DskipTests -Dspring-javaformat.skip=true

FROM eclipse-temurin:25-jre
WORKDIR /app

# Obraz JRE nie ma ani curla, ani wgeta, ani netcata - sprawdzone.
# Bez tego HEALTHCHECK nie ma czym odpytac aplikacji.
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/*

# Nie uruchamiamy jako root. Kosztuje dwie linie.
RUN useradd --system --uid 1001 petclinic
USER petclinic

COPY --from=build --chown=petclinic /build/target/*.jar app.jar

EXPOSE 8080

# Aktuator jest juz w zaleznosciach petclinica - nie trzeba nic dokladac.
#
# start-period jest dobrany do czasu startu aplikacji. Za krotki i Docker
# uzna zdrowy kontener za martwy, zanim ten zdazy wstac.
HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
