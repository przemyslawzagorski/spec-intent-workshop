# Petclinic

Aplikacja webowa lecznicy weterynaryjnej: właściciele, ich zwierzęta, wizyty
i weterynarze. Spring Boot 4.1, Thymeleaf, baza H2 w pamięci
(`pom.xml`, `src/main/resources/application.properties`).

Ta dokumentacja jest dla kogoś, kto ma tu **coś zmienić**. Nie tłumaczy,
czym jest Spring.

## Co warto przeczytać najpierw

| Plik | Po co |
|---|---|
| `src/main/java/.../owner/OwnerController.java` | wzorzec kontrolera w tym projekcie |
| `src/main/java/.../owner/Owner.java` | wzorzec encji |
| `src/main/resources/db/h2/schema.sql` | prawda o modelu danych |
| `src/test/java/.../owner/OwnerControllerTests.java` | wzorzec testu |

## Zanim coś zmienisz

Przeczytaj [Decyzje](decyzje.md). Jest tam pięć rzeczy, które zaskakują
i o które łatwo się potknąć.
