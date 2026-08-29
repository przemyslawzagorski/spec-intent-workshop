# B4 · Subagenty i równoległość

**30 min** · dowolne repo · bonus

## O co chodzi

Kiedy warto rozbić zadanie na kilku agentów, a kiedy to tylko mnoży koszt
koordynacji i produkuje sprzeczne zmiany.

## Jak zwykle to robimy

Albo wszystko jednym agentem w jednej sesji — aż kontekst się zapcha i zaczyna
gubić ustalenia z początku. Albo odruchowo rozbijamy na pięciu agentów, bo
„równolegle będzie szybciej", i dostajemy pięć wzajemnie sprzecznych zmian.

Boli, bo:

- **Jeden długi kontekst gubi środek.** To jest lost in the middle z Z04,
  tylko rozłożone w czasie.
- **Równoległość bez wspólnego punktu odniesienia produkuje konflikty.**
  Każdy agent podejmuje własne decyzje projektowe.
- **Koordynacja jest kosztem, o którym nikt nie pamięta**, dopóki nie trzeba
  scalić pięciu gałęzi.

## Kiedy równoległość działa

Skala, na której to sprawdzono: przy przepisaniu Buna z Ziga na Rusta
działało **około 64 agentów naraz** — cztery workspace'y, w każdym szesnaście
agentów — przez jedenaście dni, w około pięćdziesięciu pętlach.

Ale warto zobaczyć **kształt** tych pętli, bo to on decyduje:

> *„16 loops across 4 worktrees, each one Claude fixing, two reviewing,
> one applying."*

Czyli w jednej pętli: **jeden pisze, dwóch recenzuje, jeden scala.**
Trzy czwarte mocy idzie na sprawdzanie i integrację, nie na pisanie.

Do tego dwa warunki, bez których to by nie zadziałało:

1. **Każda pętla miała jedno wąskie zadanie.** Nie „przepisz moduł", tylko
   „napraw te błędy kompilacji w tej skrzyni".
2. **Wspólny kontekst żył w plikach, nie w głowach agentów.** Przewodnik po
   portowaniu i tabela decyzji o czasach życia — zapisane, wersjonowane,
   czytane przez wszystkich.

Bez punktu drugiego równoległość produkuje sprzeczne rozwiązania tego samego
problemu.

## Jak zrobić dobrze

**Rozbijaj po granicach, nie po wielkości.** Dobry podział to taki, w którym
dwa zadania nie dotykają tych samych plików. Zły to taki, w którym „każdy
bierze po trzy klasy".

**Wspólne ustalenia zapisz do pliku, zanim rozdzielisz pracę.** `CONTEXT.md`
z Z03, `SPEC.md` z Z05, `AGENTS.md` z Z01. To jest ten wspólny kontekst.

**Rozdziel role, nie tylko pracę.** Piszący i recenzujący to dwie różne role
— i to jest Z08. Równoległość, w której wszyscy piszą, jest najsłabszym
wariantem.

**Miej bramkę, zanim zaczniesz.** Przy jednym agencie możesz przeczytać diff.
Przy pięciu nie przeczytasz — musisz mieć coś, co sprawdzi za ciebie.

## Zrób to

**1 · Jednym agentem** (10 min). Zadanie o trzech niezależnych częściach.
Na przykład w petclinicu: dołóż walidację do trzech różnych formularzy.
Zmierz czas i policz, ile rzeczy poprawiłeś ręcznie.

**2 · Trzema agentami** (15 min). To samo zadanie, trzy osobne sesje,
każda z jedną częścią. **Zanim zaczniesz — napisz jednostronicowy plik
z ustaleniami** (konwencje walidacji, gdzie komunikaty, jak testujemy)
i daj go każdemu.

Mierz to samo, plus: ile konfliktów przy scalaniu i ile decyzji rozjechało się
mimo wspólnego pliku.

**3 · Porównaj** (5 min). Czy było szybciej? O ile? Co kosztowało najwięcej —
pisanie czy scalanie?

## Do omówienia

- **Że przy trzech częściach równoległość zwykle się nie opłaca.** Koszt
  napisania pliku z ustaleniami i scalenia trzech gałęzi zjada zysk.
  To jest właściwy wniosek, nie porażka ćwiczenia.
- **Gdzie leży próg.** Z grubsza tam, gdzie sekwencyjne wykonanie przestaje
  mieścić się w jednej sesji, a części naprawdę nie dotykają tych samych plików.
- **Że proporcja 1 : 2 : 1 zaskakuje.** Intuicja podpowiada, żeby wszystkich
  posadzić do pisania. W Bunie trzy czwarte szło na recenzję i integrację —
  bo wąskim gardłem nie było wytwarzanie kodu, tylko pewność, że można go
  scalić.
- **Że wspólny plik z ustaleniami to nie biurokracja.** To jedyny sposób,
  żeby pięciu agentów podjęło te same decyzje projektowe. Bez niego każdy
  wymyśli własne i będą poprawne osobno, a sprzeczne razem.
