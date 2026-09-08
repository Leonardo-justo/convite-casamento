/* Resultado explícito para logs, testes e métricas do processamento. */
package br.com.restaurant.edge.service;

public enum EventProcessingResult {
    APPLIED,
    DUPLICATE,
    STALE_VERSION,
    OTHER_STORE
}
