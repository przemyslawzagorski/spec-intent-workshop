# Podejście A — konwencje opisane prozą

Wklej to agentowi w katalogu `praca/Z02/returns-service`.

Zmierz: **ile iteracji** do skompilowania i **ile rzeczy** musiałeś poprawić ręcznie.

---

Dodaj do tego projektu nowy komponent `shipments` — obsługę wysyłek.

Konwencje, których używamy w tym projekcie:

Kod dzielimy na trzy warstwy w osobnych pakietach. Pakiet `boundary` zawiera
klasy, które wystawiają HTTP — mają adnotację `@Path`, `@ApplicationScoped`,
`@Produces(MediaType.APPLICATION_JSON)`, a metody mają `@GET` albo `@POST`
i zwracają `Response`. Pakiet `control` zawiera logikę biznesową i dostęp do
bazy; te klasy nie mogą importować niczego z `jakarta.ws.rs`. Pakiet `entity`
zawiera rekordy Javy — wyłącznie dane, bez adnotacji frameworkowych i bez
logiki poza walidacją w konstruktorze kanonicznym.

Nazewnictwo: klasa graniczna nazywa się od komponentu w liczbie mnogiej plus
`Resource`. Klasa dostępu do danych nazywa się od komponentu w liczbie mnogiej.
Rekordy nazywają się w liczbie pojedynczej. Wyjątki biznesowe zwracamy jako
`Problem` zgodnie z RFC 7807, nie jako rzucane wyjątki.

Dostęp do bazy: używamy `javax.sql.DataSource` wstrzykiwanego przez `@Inject`,
piszemy zapytania SQL ręcznie, nie używamy ORM-a. Schemat tworzymy w klasie
`Schema` metodą oznaczoną `@PostConstruct`. Kolumny nazywamy `snake_case`,
klasy i pola `camelCase`.

Komentarze i javadoc piszemy po polsku, identyfikatory po angielsku. Każdy
pakiet ma `package-info.java` ze specyfikacją komponentu w notacji EARS.

Wysyłka ma: identyfikator, numer zamówienia, przewoźnika, numer listu
przewozowego, status (`PREPARING`, `IN_TRANSIT`, `DELIVERED`, `LOST`) i moment
nadania. Potrzebuję endpointu do utworzenia wysyłki i do pobrania jej po
identyfikatorze.

Na koniec uruchom `mvn test-compile` i napraw błędy, jeśli będą.
