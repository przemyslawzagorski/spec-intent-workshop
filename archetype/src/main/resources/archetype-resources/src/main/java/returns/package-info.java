/**
 * Business component: <b>returns</b>.
 *
 * <p><b>TU WKLEJASZ SWOJA SPECYFIKACJE Z M3.</b>
 *
 * <p>W M3 napisales ja jako {@code workshop/spec/returns.md}. Przenies ja tutaj -
 * do JavaDoca komponentu, ktorego dotyczy. Od tej chwili specyfikacja podrozuje
 * razem z kodem: nie da sie skasowac jednego bez drugiego, a agent czytajacy ten
 * pakiet widzi kontrakt bez szukania po repozytorium.
 *
 * <p>Zachowaj ten uklad:
 *
 * <h2>Boundary operations</h2>
 * <ul>
 *   <li>{@code POST /returns} - submit a return request, receive a decision</li>
 *   <li>{@code GET /returns/{returnId}} - read a previously stored decision</li>
 * </ul>
 *
 * <h2>Requirements</h2>
 *
 * Notacja EARS. <b>Kazde ID ma dokladnie jeden wiersz w tabeli testowej.</b>
 * Numer wymagania jest jedynym lacznikiem miedzy specyfikacja a testem.
 *
 * <ul>
 * <li><b>R1.1</b> WHEN ... , the service shall ...</li>
 * <li><b>R1.2</b> IF ... , THEN the service shall ...</li>
 * <li>... (celujesz w co najmniej 8; ponizej 8 prawie na pewno nie pokryles
 *     sytuacji niepozadanych)</li>
 * </ul>
 *
 * <h2>Decisions and assumptions</h2>
 *
 * <b>To jest najwazniejsza sekcja tej specyfikacji.</b> Wypisz tu kazda rzecz,
 * ktorej specyfikacja NIE rozstrzygala, a kod musial rozstrzygnac -
 * i napisz, dlaczego tak, a nie inaczej.
 *
 * <p>Za pol roku wrocisz tutaj, nie do listy wymagan. I to sa dokladnie te
 * miejsca, w ktorych twoj sasiad zdecydowal inaczej - a jego system nadal
 * przechodzi te sama bramke.
 *
 * <ul>
 * <li><b>A1</b> ...</li>
 * </ul>
 */
package ${package}.returns;
