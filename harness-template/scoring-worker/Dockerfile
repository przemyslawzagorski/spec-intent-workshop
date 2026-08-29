# Obraz scoring-workera.
#
# Uruchamia FastAPI *oraz* konsumenta Kafki - patrz boundary/api.py, sekcja
# lifespan. Bez tego serwis odpowiadalby na /score, ale nie konsumowalby
# zdarzen, czyli szew asynchroniczny bylby martwy.
FROM python:3.12-slim
WORKDIR /app

COPY --from=ghcr.io/astral-sh/uv:0.5 /uv /usr/local/bin/uv

COPY pyproject.toml ./
COPY src ./src
RUN uv sync --no-dev

# Nagrania odpowiedzi modelu. Bez nich /assistant/ask wywala sie w runtime na
# FileNotFoundError - obraz buduje sie poprawnie, a serwis nie odpowiada.
# Domyslna sciezka w kodzie jest wzgledna (tests/fixtures/...), wiec tutaj
# ustawiamy ja jawnie, zeby nie zalezala od katalogu roboczego.
COPY tests/fixtures/llm-recordings.json /app/recordings/llm-recordings.json

ENV PATH="/app/.venv/bin:$PATH" \
    KAFKA_ENABLED=true \
    POLICY_FILE=/work/return-policy.yaml \
    LLM_RECORDINGS=/app/recordings/llm-recordings.json

EXPOSE 8000
CMD ["uvicorn", "scoring.boundary.api:app", "--host", "0.0.0.0", "--port", "8000"]
