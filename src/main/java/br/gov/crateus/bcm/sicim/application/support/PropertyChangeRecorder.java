package br.gov.crateus.bcm.sicim.application.support;

import br.gov.crateus.bcm.sicim.application.port.CorrelationIdProvider;
import br.gov.crateus.bcm.sicim.application.port.PropertyEventPublisher;
import br.gov.crateus.bcm.sicim.application.port.PropertyEventPublisher.PropertyEvent;
import br.gov.crateus.bcm.sicim.application.port.PropertyHistoryRepository;
import br.gov.crateus.bcm.sicim.domain.Property;
import br.gov.crateus.bcm.sicim.domain.PropertyHistoryAction;
import br.gov.crateus.bcm.sicim.domain.PropertyHistoryEntry;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Efeito colateral comum a toda escrita: grava o histórico e publica o evento.
 * Deve ser chamado dentro da transação do caso de uso (histórico + outbox atômicos).
 */
@Component
public class PropertyChangeRecorder {

	private final PropertyHistoryRepository history;
	private final PropertyEventPublisher events;
	private final CorrelationIdProvider correlationId;

	public PropertyChangeRecorder(PropertyHistoryRepository history, PropertyEventPublisher events,
			CorrelationIdProvider correlationId) {
		this.history = history;
		this.events = events;
		this.correlationId = correlationId;
	}

	public void record(Property saved, PropertyHistoryAction action, Map<String, Object> before, PropertyEvent event) {
		history.append(PropertyHistoryEntry.of(saved.id(), action, before, PropertySnapshot.of(saved),
				correlationId.currentCorrelationId()));
		events.publish(event, saved);
	}
}
