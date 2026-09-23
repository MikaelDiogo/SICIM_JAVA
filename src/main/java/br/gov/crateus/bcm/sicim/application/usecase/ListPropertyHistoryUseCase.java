package br.gov.crateus.bcm.sicim.application.usecase;

import br.gov.crateus.bcm.sicim.application.command.PropertyHistoryFilter;
import br.gov.crateus.bcm.sicim.application.port.PropertyHistoryRepository;
import br.gov.crateus.bcm.sicim.application.result.PageResult;
import br.gov.crateus.bcm.sicim.application.result.PropertyHistoryResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListPropertyHistoryUseCase {

	private final PropertyHistoryRepository history;

	public ListPropertyHistoryUseCase(PropertyHistoryRepository history) {
		this.history = history;
	}

	@Transactional(readOnly = true)
	public PageResult<PropertyHistoryResult> execute(PropertyHistoryFilter filter) {
		return history.findPage(filter).map(PropertyHistoryResult::from);
	}
}
