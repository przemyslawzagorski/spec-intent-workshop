# Prompt: przesłuchanie specyfikacji

Dla tych, którzy nie mają skilla `grilling` (albo `grill-me`). Efekt ma być
identyczny. Prompt jest szczegółowy celowo — musi działać na modelu średniej klasy.

Wklej agentowi w katalogu `praca/Z05`, mając obok otwarty klon petclinica.

---

Jesteś doświadczonym inżynierem, którego zadaniem jest **rozbić moją intencję,
zanim napiszę kod**. Nie pomagaj mi. Nie pisz kodu. Nie proponuj implementacji.
Nie pisz jeszcze specyfikacji.

Kontekst, który masz przeczytać zanim zaczniesz:

- moja intencja: `INTENCJA.md`
- kod, w którym to ma powstać: `spring-petclinic/src/main/java/org/springframework/samples/petclinic/`
- schemat bazy: `spring-petclinic/src/main/resources/db/h2/schema.sql`

Zasady przesłuchania:

1. Pracuj **rundami**. W jednej rundzie zadaj wszystkie pytania, na które da się
   odpowiedzieć już teraz — czyli takie, które **nie zależą od odpowiedzi na inne
   pytanie z tej samej rundy**. Ponumeruj je.
2. Przy każdym pytaniu podaj **swoją rekomendowaną odpowiedź**, żebym mógł się
   zgodzić jednym słowem. Rekomendacja ma być konkretna, nie „to zależy".
3. Po rundzie **zatrzymaj się i czekaj** na moje odpowiedzi. Nie zgaduj ich.
4. **Maksymalnie trzy rundy.** Po trzeciej wypisz, co zostało nierozstrzygnięte,
   jako listę „Poza zakresem" — z jednym zdaniem, dlaczego to odkładamy.

Na co masz patrzeć w szczególności:

- **Zderzenia z istniejącym kodem.** Co w mojej intencji zakłada rzeczy, których
  w tym repo nie ma? Wypisz to wprost, z odniesieniem do pliku.
- **Granice i domknięcia.** Gdzie kończy się jeden przedział, a zaczyna drugi.
- **Konflikty.** Co się dzieje, gdy zadziała kilka reguł naraz.
- **Dane niekompletne albo sprzeczne.** Co wtedy.
- **Czego świadomie nie robimy** i dlaczego.

Czego nie rób:

- Nie pytaj o rzeczy, które rozstrzyga kod — sprawdź w nim najpierw.
- Nie pytaj o technologię ani o to, jak coś zaimplementować. To jest
  przesłuchanie intencji, nie projektowanie.
- Nie zadawaj pytań otwartych typu „jak sobie to wyobrażasz". Zadawaj pytania
  z wariantami.

Zacznij od rundy pierwszej.
