package br.gov.crateus.bcm.sicim.application.result;

import java.util.List;
import java.util.function.Function;

/** Mesmo formato paginado do backend NestJS: { data, total, page, pageSize }. */
public record PageResult<T>(List<T> data, long total, int page, int pageSize) {

	public PageResult {
		data = List.copyOf(data);
	}

	public <R> PageResult<R> map(Function<? super T, ? extends R> mapper) {
		return new PageResult<>(data.stream().<R>map(mapper).toList(), total, page, pageSize);
	}
}
