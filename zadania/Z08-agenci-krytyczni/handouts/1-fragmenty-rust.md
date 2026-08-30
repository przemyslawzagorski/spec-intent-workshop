# Handout 1 — trzy fragmenty z przepisania Buna

**Masz 10 minut. Pracujesz sam. Nie używaj agenta.**

Poniższe trzy fragmenty pochodzą z prawdziwego przepisania Buna z Ziga na Rusta
(maj 2026, 535 496 linii Ziga, 11 dni). Wszystkie trzy **kompilują się czysto**.
Wszystkie trzy zostały napisane przez agenta i zaakceptowane przez kompilator.
Wszystkie trzy zawierają błąd, który trafiłby na produkcję.

> **Uczciwie o źródle:** autor artykułu pisze, że kod w nim jest *skondensowany*
> z cytowanych commitów — *„Code is condensed from the cited commits; same bugs,
> same fixes"*. Czyli: te błędy są prawdziwe i zostały naprawdę znalezione przez
> adwersaryjny przegląd, ale fragment nie jest dosłownym wycinkiem repozytorium.
> Dla tego ćwiczenia to bez znaczenia — dla uczciwości ma.

Nie znasz Rusta i **to jest zaleta** — nie możesz rozpoznać idiomu, więc musisz
przeczytać, co ten kod faktycznie robi.

Przy każdym fragmencie dostajesz **jedno zdanie faktu**. Bez niego błędu nie da się
znaleźć. Z nim — da się.

---

## Fragment A · obsługa procesu potomnego

> **Fakt:** `uv_close` z biblioteki libuv jest **asynchroniczne** — zapamiętuje
> wskaźnik i oddzwoni dopiero w następnym obrocie pętli zdarzeń.
> Funkcja `on_pipe_close` zwalnia alokację.

```rust
for stdio in [spawned_stdout, spawned_stderr] {
    match stdio {
        StdioResult::Buffer(mut pipe) => {
            // pipe ma typ Box<uv::Pipe> - czyli wskaznik do pamieci,
            // ktora Rust zwalnia automatycznie na koncu zakresu
            pipe.close(Subprocess::on_pipe_close)
        }
        StdioResult::Fd(fd) => fd.close(),
        StdioResult::Unavailable => {}
    }
}
```

**Co jest nie tak?**

---

## Fragment B · konwersja czasu pliku

> **Fakt:** pole `nsec` w strukturze `timespec` musi mieścić się w przedziale
> `[0, 1_000_000_000)`. Wartość ujemna jest nieprawidłowa.
> `trunc()` obcina w stronę zera, `floor()` w dół.

```rust
// rozbicie sekund (liczba zmiennoprzecinkowa) na {sekundy, nanosekundy}
let sec = t.trunc();
TimeLike {
    sec: sec as i64,
    nsec: ((t - sec) * 1e9) as i64,
}
```

**Co jest nie tak? Dla jakich danych wejściowych?**

---

## Fragment C · parser koloru CSS

> **Fakt:** w funkcji CSS `color-mix()` **każda ze stron może pominąć swój
> procent** — brakujący jest wtedy dopełnieniem drugiego.
> `unwrap()` panikuje, gdy wartości nie ma.

```rust
let p1 = first.percentage.unwrap_or(1.0 - second.percentage.unwrap());
```

**Co jest nie tak? Podaj wywołanie CSS, które to wywróci.**

---

## Po dziesięciu minutach

Nie sprawdzamy odpowiedzi od razu. Najpierw puścisz na te fragmenty agenta-krytyka
z promptem [`../prompty/krytyk.md`](../prompty/krytyk.md) i porównasz,
ile znalazł on, a ile ty.
