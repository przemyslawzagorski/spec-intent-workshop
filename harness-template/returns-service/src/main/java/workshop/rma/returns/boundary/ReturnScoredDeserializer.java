package workshop.rma.returns.boundary;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import workshop.rma.returns.entity.ReturnScoredEvent;

/** Deserializator zdarzen return.scored. Kontrakt: docs/contract/events.md */
public class ReturnScoredDeserializer extends ObjectMapperDeserializer<ReturnScoredEvent> {

    public ReturnScoredDeserializer() {
        super(ReturnScoredEvent.class);
    }
}
