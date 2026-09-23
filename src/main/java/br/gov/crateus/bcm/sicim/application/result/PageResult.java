package br.gov.crateus.bcm.sicim.application.result;

import java.util.List;

/** Mesmo formato paginado do backend NestJS: { data, total, page, pageSize }. */
public record PageResult<T>(List<T> data, long total, int page, int pageSize) {
}
