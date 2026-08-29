# Prompt: wydziel port i adapter

Wklej agentowi w katalogu `praca/Z09/spring-petclinic`.

---

W `OwnerController.processUpdateOwnerForm` reguła biznesowa jest przemieszana
z obsługą HTTP. Chcę ją stamtąd wydzielić.

Przeczytaj najpierw:

- `src/main/java/.../owner/OwnerController.java` — metoda `processUpdateOwnerForm`
- `src/main/java/.../owner/OwnerRepository.java`
- `src/test/java/.../owner/OwnerControllerTests.java`

Zrób cztery rzeczy:

**1 · Port.** Interfejs opisujący, czego reguła potrzebuje od składowania
właścicieli. **Tylko te metody, których naprawdę używa** — nie kopiuj
`JpaRepository`. Port definiuje ten, kto go używa, a nie ten, kto go implementuje.

**2 · Adapter.** Klasa implementująca port przy pomocy `OwnerRepository`.
Cała wiedza o Spring Data ma kończyć się w tej jednej klasie.

**3 · Przypadek użycia.** Klasa z regułą „identyfikator w formularzu musi zgadzać
się z tym w adresie". Ma **zwracać wynik**, nie nazwę widoku. Bez importów
z `org.springframework.web`, bez `BindingResult`, bez `RedirectAttributes`.
Co zrobić z wynikiem, decyduje ten, kto ją woła.

**4 · Test bez frameworka.** Testy tej klasy z atrapą portu napisaną ręcznie.
**Bez `@SpringBootTest`, bez `@WebMvcTest`, bez `@DataJpaTest`, bez Mockito.**
Jeśli potrzebujesz kontekstu Springa, żeby przetestować regułę — wydzielenie
się nie udało.

Potem przepnij kontroler tak, żeby wołał nową klasę.

Twarde zasady:

- **Nie zmieniaj zachowania.** Te same przekierowania, te same komunikaty,
  ta sama obsługa błędów walidacji. To jest refaktor.
- **Projekt celuje w Javę 17.** Rekordy i typy zapieczętowane są dostępne,
  wzorce w `switch` nie.
- **Po zmianach uruchom `mvn spring-javaformat:apply`.** Build wywala się
  na formatowaniu.
- **Uruchom pełne `mvn test`.** Wszystko ma być zielone.

Jeśli któryś istniejący test przestanie się kompilować albo przechodzić —
**nie osłabiaj go i nie usuwaj**. Napraw przyczynę, a jeśli naprawdę trzeba
zmienić test, powiedz mi najpierw, co zmieniasz i dlaczego zmiana kontraktu
jest uzasadniona.

Na koniec podaj mi czas wykonania nowych testów i czas `OwnerControllerTests`.
Chcę porównać.
