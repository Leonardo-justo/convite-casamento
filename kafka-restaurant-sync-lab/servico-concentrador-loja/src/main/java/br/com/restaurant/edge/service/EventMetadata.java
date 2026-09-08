/* Metadados Kafka guardados na inbox para auditoria e diagnóstico. */
package br.com.restaurant.edge.service;

public record EventMetadata(String topic, int partition, long offset) {
}
