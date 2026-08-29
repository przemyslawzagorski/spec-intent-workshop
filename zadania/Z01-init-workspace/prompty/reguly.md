# Prompt: napisz AGENTS.md dla tego repo

Wklej agentowi, mając otwarty katalog `praca/Z01/spring-petclinic`.

Prompt jest szczegółowy celowo — ma działać także na modelu średniej klasy.
Im mniej zostawiasz do domysłu, tym mniej zależysz od tego, którego modelu użyjesz.

---

Rozejrzyj się po tym repozytorium i napisz plik `AGENTS.md` z regułami pracy
dla agenta. Będzie doklejany do każdego promptu, więc ma być krótki i konkretny.

Zanim zaczniesz pisać, sprawdź w kodzie i podaj mi:

1. jakie polecenie uruchamia testy i ile ich jest,
2. czy build ma jakąś bramkę formatowania lub statycznej analizy,
3. jak nazywa się przykładowy kontroler i jego test,
4. jaka baza działa domyślnie przy uruchomieniu lokalnym.

**Nie zgaduj żadnej z tych czterech rzeczy — sprawdź w plikach.** Jeśli czegoś
nie da się ustalić z repo, napisz „nie ustaliłem" i zapytaj mnie.

Potem napisz `AGENTS.md` w tym układzie:

1. **Wzoruj się na kodzie** — lista 3–5 ścieżek do plików, które pokazują
   konwencje. Sama ścieżka plus pół zdania, po co tam patrzeć. Nie streszczaj
   zawartości tych plików.
2. **Twarde reguły** — maksymalnie pięć, numerowane. Każda ma być sprawdzalna:
   po przeczytaniu musi być jasne, co znaczy jej złamanie.
3. **Jak uruchamiać** — komendy, które naprawdę działają w tym repo, z czasem
   wykonania przy każdej.
4. **Czego nie zakładać** — dwie, trzy rzeczy, które ktoś nieznający tego repo
   wziąłby za oczywiste, a są inaczej.

Twarde zasady dla ciebie:

- **Cały plik ma się zmieścić w 2000 bajtów.** Sprawdź to przed oddaniem.
- Nie pisz reguł typu „pisz czysty kod", „stosuj dobre praktyki", „bądź
  dokładny". Nic nie zmieniają, a zajmują miejsce.
- Nie opisuj architektury. To plik reguł, nie dokumentacja.
- Nie wymyślaj komend, których nie ma w `pom.xml` ani w `README.md`.

Na koniec wypisz mi osobno: **które reguły wpisałeś, mimo że nie miałeś na nie
dowodu w kodzie**. Chcę wiedzieć, co jest ustalone, a co jest twoim domysłem.
