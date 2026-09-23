package br.gov.crateus.bcm.sicim.application.port;

/** X-Correlation-Id da intenção de negócio atual (gravado no histórico, não é auditoria em si). */
public interface CorrelationIdProvider {

	String currentCorrelationId();
}
