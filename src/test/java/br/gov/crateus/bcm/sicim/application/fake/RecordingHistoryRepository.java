package br.gov.crateus.bcm.sicim.application.fake;

import br.gov.crateus.bcm.sicim.application.command.PropertyHistoryFilter;
import br.gov.crateus.bcm.sicim.application.port.PropertyHistoryRepository;
import br.gov.crateus.bcm.sicim.application.result.PageResult;
import br.gov.crateus.bcm.sicim.domain.PropertyHistoryEntry;
import java.util.ArrayList;
import java.util.List;

public class RecordingHistoryRepository implements PropertyHistoryRepository {

	public final List<PropertyHistoryEntry> entries = new ArrayList<>();

	@Override
	public void append(PropertyHistoryEntry entry) {
		entries.add(entry);
	}

	@Override
	public PageResult<PropertyHistoryEntry> findPage(PropertyHistoryFilter filter) {
		return new PageResult<>(entries, entries.size(), filter.page(), filter.pageSize());
	}
}
