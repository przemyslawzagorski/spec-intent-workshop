# AGENTS.md

Reguly tego repo. Czyta je Claude Code, Copilot, Augment i kazdy inny agent.
Siedza w **kazdym** promptcie, wiec sa krotkie.

## Wzoruj sie na kodzie, nie na moim opisie

Nie tlumacze tu konwencji - wskazuje pliki, ktore ja pokazuja:

- Kontroler: `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
- Encja: `.../owner/Owner.java`
- Test kontrolera: `src/test/java/.../owner/OwnerControllerTests.java`
- Widok: `src/main/resources/templates/owners/`

Dodajesz kontroler? Odtworz ksztalt `OwnerController`, nie wymyslaj wlasnego.

## Twarde reguly

1. **Nie zmieniaj testow, zeby przeszly.** Czerwony test to informacja o kodzie.
   Zmiana testu jest zmiana kontraktu - najpierw powiedz mi o niej.
2. **Format jest bramka.** `spring-javaformat` wywala build przy zlym formacie.
   Po zmianach w Javie uruchom `mvn spring-javaformat:apply`.
3. **Nowa zaleznosc wymaga zgody.** Nie dodawaj nic do `pom.xml` bez pytania.
4. **Nie commituj sekretow.** Hook w `.git/hooks/pre-commit` i tak zablokuje.

## Jak uruchamiac

```bash
mvn test -Dtest=OwnerControllerTests   # 23 s - do tego wracaj w petli
mvn test                               # 84 s, 76 testow - przed commitem
mvn spring-boot:run                    # localhost:8080, baza H2 w pamieci
```

**Nie puszczaj pelnego `mvn test` po kazdej zmianie.** Odpal klase, ktorej
dotyczy zmiana. Pelny zestaw przed commitem.

## Czego nie zakladac

- Baza domyslnie to H2 w pamieci, nie Postgres. Dane znikaja po restarcie.
- Kontrolery siegaja wprost do repozytoriow Spring Data. Tak tu jest i to nie
  jest bug - nie "naprawiaj" tego przy okazji innego zadania.
