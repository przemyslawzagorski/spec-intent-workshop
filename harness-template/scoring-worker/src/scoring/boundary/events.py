"""Granica Kafki. Druga granica tego samego komponentu.

Zwroc uwage, czego tu NIE MA: liczenia wskaznika. To siedzi w control i jest
czysta funkcja. Ta warstwa tylko przenosi bajty i pilnuje ksztaltu.

Dzieki temu regula da sie przetestowac bez brokera, a broker da sie wymienic
bez dotykania reguly.
"""
from __future__ import annotations

import asyncio
import json
import logging
import os

from aiokafka import AIOKafkaConsumer, AIOKafkaProducer

from scoring.control.abuse_score import policz
from scoring.control.policy import ReturnPolicy
from scoring.entity.models import ReturnScored, ReturnSubmitted

LOG = logging.getLogger("scoring.events")

BOOTSTRAP = os.environ.get("KAFKA_BOOTSTRAP", "localhost:9092")
TEMAT_WEJSCIOWY = "return.submitted"
TEMAT_WYJSCIOWY = "return.scored"
GRUPA = "scoring-worker"


async def obsluguj() -> None:
    """Petla: konsumuj return.submitted, publikuj return.scored."""
    polityka = ReturnPolicy.wczytaj()
    consumer = AIOKafkaConsumer(
        TEMAT_WEJSCIOWY,
        bootstrap_servers=BOOTSTRAP,
        group_id=GRUPA,
        value_deserializer=lambda b: json.loads(b.decode("utf-8")),
        auto_offset_reset="earliest",
    )
    producer = AIOKafkaProducer(
        bootstrap_servers=BOOTSTRAP,
        value_serializer=lambda v: json.dumps(v).encode("utf-8"),
    )
    await consumer.start()
    await producer.start()
    LOG.info("nasluchuje %s na %s", TEMAT_WEJSCIOWY, BOOTSTRAP)
    try:
        async for wiadomosc in consumer:
            try:
                zdarzenie = ReturnSubmitted.model_validate(wiadomosc.value)
            except Exception:
                # Zdarzenie niezgodne z kontraktem. Logujemy i idziemy dalej -
                # jedna zla wiadomosc nie moze zatrzymac calego strumienia.
                LOG.exception("odrzucam zdarzenie niezgodne z kontraktem")
                continue

            wynik = ReturnScored(
                returnId=zdarzenie.returnId,
                abuseScore=policz(
                    requested_at=zdarzenie.requestedAt,
                    history=zdarzenie.history,
                    orders_in_window=zdarzenie.ordersInWindow,
                    okno_dni=polityka.abuse_window_days,
                ),
            )
            await producer.send_and_wait(TEMAT_WYJSCIOWY, json.loads(wynik.model_dump_json()))
            LOG.info("%s -> %.4f", zdarzenie.returnId, wynik.abuseScore)
    finally:
        await consumer.stop()
        await producer.stop()


def main() -> None:
    logging.basicConfig(level=logging.INFO)
    asyncio.run(obsluguj())


if __name__ == "__main__":
    main()
