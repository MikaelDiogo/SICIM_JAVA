package br.gov.crateus.bcm.sicim.application.port;

import br.gov.crateus.bcm.sicim.application.command.PropertyHistoryFilter;
import br.gov.crateus.bcm.sicim.application.result.PageResult;
import br.gov.crateus.bcm.sicim.domain.PropertyHistoryEntry;

/** Histórico append-only: só inclui e consulta. */
public interface PropertyHistoryRepository {

	void append(PropertyHistoryEntry entry);

	PageResult<PropertyHistoryEntry> findPage(PropertyHistoryFilter filter);
}
