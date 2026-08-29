/**
 * Business component: <b>returns</b>.
 *
 * <p>Capability: przyjac zgloszenie zwrotu i rozstrzygnac, czy kwalifikuje sie
 * do automatycznej akceptacji, recznego przegladu, czy odrzucenia.
 *
 * <p><b>Boundary</b> {@link workshop.rma.returns.boundary.ReturnsResource} jest
 * kontraktem tego komponentu. Kszalt kontraktu: docs/contract/openapi.yaml.
 * Procedura decyzyjna: docs/contract/decision-procedure.md.
 * Wartosci progow: return-policy.yaml (indywidualne).
 *
 * <h2>Boundary operations</h2>
 * <ul>
 *   <li>{@code POST /returns} - submit a return request, receive a decision</li>
 *   <li>{@code GET /returns/{returnId}} - read a previously stored decision</li>
 * </ul>
 *
 * <h2>Requirements</h2>
 *
 * Zapisane w notacji EARS (alistairmavin.com/ears). Kazde ID ma dokladnie jeden
 * wiersz w tabeli testowej - patrz ReturnsResourceTest. Numer wymagania jest
 * jedynym lacznikiem miedzy specyfikacja a testem; nie zmieniaj go bez powodu.
 *
 * <p><b>Przyjmowanie zgloszen</b>
 * <ul>
 * <li><b>R1.1</b> WHEN a return request references an existing order, the service
 *     shall store a decision and respond with 201 and the decision body.</li>
 * <li><b>R1.2</b> IF the referenced order does not exist, THEN the service shall
 *     respond with 404 and shall not store a decision.</li>
 * <li><b>R1.3</b> IF the request has no items, no orderId or no requestedAt,
 *     THEN the service shall respond with 400.</li>
 * </ul>
 *
 * <p><b>Kwalifikacja</b>
 * <ul>
 * <li><b>R1.4</b> IF any returned item belongs to an excluded category, THEN the
 *     service shall add CATEGORY_EXCLUDED.</li>
 * <li><b>R1.5</b> IF the number of whole days between delivery and request is
 *     greater than the applicable return window, THEN the service shall add
 *     WINDOW_EXPIRED. A request on the last day of the window is still within it.</li>
 * <li><b>R1.6</b> IF the request returns only part of the order AND the policy
 *     forbids partial returns, THEN the service shall add PARTIAL_NOT_ALLOWED.</li>
 * <li><b>R1.7</b> IF the abuse score is greater than or equal to the reject
 *     threshold, THEN the service shall add ABUSE_SUSPECTED.</li>
 * <li><b>R1.8</b> IF the abuse score is greater than or equal to the review
 *     threshold AND below the reject threshold, THEN the service shall add
 *     ABUSE_BORDERLINE.</li>
 * <li><b>R1.9</b> IF the refund amount is strictly greater than the manual review
 *     threshold, THEN the service shall add AMOUNT_ABOVE_THRESHOLD. An amount
 *     equal to the threshold shall not trigger review.</li>
 * <li><b>R1.10</b> WHERE no other reason code applies, the service shall add
 *     WITHIN_POLICY.</li>
 * </ul>
 *
 * <p><b>Rozstrzygniecie</b>
 * <ul>
 * <li><b>R1.11</b> WHEN any rejecting reason code applies, the service shall
 *     decide REJECTED, regardless of any other code.</li>
 * <li><b>R1.12</b> WHEN no rejecting code applies AND a review code applies, the
 *     service shall decide MANUAL_REVIEW.</li>
 * <li><b>R1.13</b> WHILE a decision is REJECTED, the service shall report a refund
 *     amount of zero and shall omit the shipping payer.</li>
 * <li><b>R1.14</b> WHERE returned items carry different reasons, the service shall
 *     select the shipping payer most favourable to the customer.</li>
 * </ul>
 *
 * <p><b>Odczyt</b>
 * <ul>
 * <li><b>R1.15</b> WHEN a stored return is requested by its id, the service shall
 *     respond with 200 and the same decision it stored.</li>
 * <li><b>R1.16</b> IF no return exists for the given id, THEN the service shall
 *     respond with 404.</li>
 * </ul>
 *
 * <h2>Decisions and assumptions</h2>
 *
 * Rzeczy, ktorych specyfikacja NIE rozstrzyga, a kod musial rozstrzygnac.
 * Kazda z nich to swiadoma decyzja, nie przeoczenie - i kazda jest miejscem,
 * w ktorym inny uczestnik warsztatu moze zdecydowac inaczej.
 *
 * <ul>
 * <li><b>A1</b> Okno zwrotu liczymy od <i>dostawy</i>, nie od zakupu.</li>
 * <li><b>A2</b> Dzien to pelne 24h w UTC. Bez dni roboczych, bez stref lokalnych.</li>
 * <li><b>A3</b> Gdy zamowienie laczy kategorie o roznych oknach, obowiazuje okno
 *     <i>najkrotsze</i>. Alternatywa - okno per pozycja - byla odrzucona, bo
 *     rozbija jedno zgloszenie na kilka decyzji.</li>
 * <li><b>A4</b> Zwrot jest <i>czesciowy</i>, gdy ktorakolwiek pozycja zamowienia
 *     wraca w mniejszej ilosci niz zamowiona.</li>
 * <li><b>A5</b> abuseScore pochodzi ze scoring-workera (M9). Do czasu jego
 *     wdrozenia boundary podaje 0.0. Wartosc jest parametrem
 *     {@code EligibilityCheck.evaluate}, a nie polem - zeby wdrozenie workera
 *     nie wymagalo zmiany w logice kwalifikacji.</li>
 * </ul>
 */
package workshop.rma.returns;
