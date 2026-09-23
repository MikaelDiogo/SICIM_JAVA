package br.gov.crateus.bcm.sicim.application.usecase;

import br.gov.crateus.bcm.sicim.application.command.PropertyFilter;
import br.gov.crateus.bcm.sicim.application.port.PropertyRepository;
import br.gov.crateus.bcm.sicim.application.result.PageResult;
import br.gov.crateus.bcm.sicim.application.result.PropertyResult;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListPropertiesUseCase {

	private final PropertyRepository properties;

	public ListPropertiesUseCase(PropertyRepository properties) {
		this.properties = properties;
	}

	@Transactional(readOnly = true)
	public PageResult<PropertyResult> execute(PropertyFilter filter) {
		return properties.findPage(filter).map(PropertyResult::from);
	}

	@Transactional(readOnly = true)
	public List<String> customCategoryNames() {
		return properties.findCustomCategoryNames();
	}
}
