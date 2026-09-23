package br.gov.crateus.bcm.sicim.application.fake;

import br.gov.crateus.bcm.sicim.application.command.PropertyFilter;
import br.gov.crateus.bcm.sicim.application.port.PropertyRepository;
import br.gov.crateus.bcm.sicim.application.result.PageResult;
import br.gov.crateus.bcm.sicim.domain.Property;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class InMemoryPropertyRepository implements PropertyRepository {

	private final Map<UUID, Property> store = new LinkedHashMap<>();

	@Override
	public Optional<Property> findById(UUID id) {
		return Optional.ofNullable(store.get(id));
	}

	@Override
	public boolean existsByRegistrationNumber(String registrationNumber) {
		return store.values().stream()
				.anyMatch(p -> p.state().registrationNumber().value().equals(registrationNumber));
	}

	@Override
	public Property save(Property property) {
		store.put(property.id(), property);
		return property;
	}

	@Override
	public PageResult<Property> findPage(PropertyFilter f) {
		List<Property> all = store.values().stream()
				.filter(p -> f.status() == null || p.state().status() == f.status())
				.toList();
		return new PageResult<>(all, all.size(), f.page(), f.pageSize());
	}

	@Override
	public List<String> findCustomCategoryNames() {
		return store.values().stream().map(p -> p.state().customCategoryName()).filter(Objects::nonNull)
				.distinct().sorted().toList();
	}

	public int size() {
		return store.size();
	}
}
